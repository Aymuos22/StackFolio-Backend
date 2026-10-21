package com.portfolio.Stackfolio.service;

import com.openai.errors.RateLimitException;
import com.portfolio.Stackfolio.exception.AiServiceException;
import com.portfolio.Stackfolio.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortfolioSuggestServiceTest {

    private final ChatModel model = mock(ChatModel.class);

    @BeforeEach
    void setUp() {
        when(model.getOptions()).thenReturn(OpenAiChatOptions.builder().model("primary").build());
    }

    private PortfolioSuggestService service(String fallback) {
        return new PortfolioSuggestService(ChatClient.builder(model), fallback, 4096);
    }

    private ChatResponse response() {
        return new ChatResponse(List.of(new Generation(new AssistantMessage("""
                {"fullName":"Test User","projects":null,"experiences":null,
                 "certifications":null,"technicalSkills":null,"customLinks":null}
                """))));
    }

    @Test
    void successfulPrimaryDoesNotCallFallbackAndNormalizesArrays() {
        when(model.call(any(Prompt.class))).thenReturn(response());

        var result = service("openai/gpt-oss-20b").suggest("Test User");

        assertEquals("Test User", result.getFullName());
        assertTrue(result.getProjects().isEmpty());
        assertTrue(result.getExperiences().isEmpty());
        assertTrue(result.getCertifications().isEmpty());
        assertTrue(result.getTechnicalSkills().isEmpty());
        assertTrue(result.getCustomLinks().isEmpty());
        verify(model, times(1)).call(any(Prompt.class));
    }

    @Test
    void wrappedRateLimitUsesFallbackWithExplicitOutputBudget() {
        when(model.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("Provider failed", mock(RateLimitException.class)))
                .thenReturn(response());

        assertEquals("Test User", service("openai/gpt-oss-20b").suggest("Test User").getFullName());

        var prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(model, times(2)).call(prompts.capture());
        var options = (OpenAiChatOptions) prompts.getAllValues().get(1).getOptions();
        assertEquals("openai/gpt-oss-20b", options.getModel());
        assertEquals(4096, options.getMaxCompletionTokens());
        assertEquals(0, options.getMaxRetries());
    }

    @Test
    void bothModelsRateLimitedReturnRetryableError() {
        when(model.call(any(Prompt.class))).thenThrow(mock(RateLimitException.class));

        var error = assertThrows(TooManyRequestsException.class,
                () -> service("openai/gpt-oss-20b").suggest("Test User"));

        assertTrue(error.getMessage().contains("retry"));
        verify(model, times(2)).call(any(Prompt.class));
    }

    @Test
    void disabledFallbackReturnsRateLimitWithoutSecondCall() {
        when(model.call(any(Prompt.class))).thenThrow(mock(RateLimitException.class));

        assertThrows(TooManyRequestsException.class, () -> service("").suggest("Test User"));

        verify(model, times(1)).call(any(Prompt.class));
    }

    @Test
    void otherProviderErrorsDoNotTriggerFallback() {
        when(model.call(any(Prompt.class))).thenThrow(new IllegalStateException("Provider unavailable"));

        assertThrows(AiServiceException.class,
                () -> service("openai/gpt-oss-20b").suggest("Test User"));

        verify(model, times(1)).call(any(Prompt.class));
    }

    @Test
    void invalidFallbackOutputRemainsAnAiError() {
        when(model.call(any(Prompt.class)))
                .thenThrow(mock(RateLimitException.class))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("invalid JSON")))));

        assertThrows(AiServiceException.class,
                () -> service("openai/gpt-oss-20b").suggest("Test User"));

        verify(model, times(2)).call(any(Prompt.class));
    }
}
