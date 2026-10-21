package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.ai.PortfolioSuggestResponse;
import com.portfolio.Stackfolio.exception.AiServiceException;
import com.portfolio.Stackfolio.exception.TooManyRequestsException;
import com.openai.errors.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PortfolioSuggestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioSuggestService.class);

    private static final String SYSTEM_PROMPT = """
            You extract portfolio profile data from the user's text (resume, bio, or notes).
            Return only fields that are present or clearly implied. Do not invent employers, dates, URLs, or skills.
            Use null for unknown scalar fields and empty arrays for unknown lists.
            Dates must be ISO-8601 (YYYY-MM-DD) when known; otherwise null.
            For experiences still in progress, set currentlyWorking to true and endDate to null.
            Order list items with displayOrder starting at 0.
            Respond with JSON matching the schema exactly — no markdown, no commentary.
            """;

    private final ChatClient chatClient;
    private final ChatClient fallbackChatClient;

    public PortfolioSuggestService(
            ChatClient.Builder chatClientBuilder,
            @Value("${stackfolio.ai.fallback-model:openai/gpt-oss-20b}") String fallbackModel,
            @Value("${stackfolio.ai.fallback-max-completion-tokens:4096}") int fallbackMaxCompletionTokens
    ) {
        // Fail over immediately on a quota rejection instead of repeatedly sending
        // the same request that exceeds the primary model's token allowance.
        this.chatClient = chatClientBuilder.clone()
                .defaultOptions(OpenAiChatOptions.builder().maxRetries(0))
                .build();
        this.fallbackChatClient = fallbackModel == null || fallbackModel.isBlank()
                ? null
                : chatClientBuilder.clone()
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(fallbackModel)
                                .maxCompletionTokens(fallbackMaxCompletionTokens)
                                .maxRetries(0))
                        .build();
    }

    public PortfolioSuggestResponse suggest(String text) {
        try {
            return generate(chatClient, text);
        } catch (Exception exception) {
            if (!isRateLimited(exception)) {
                throw generationFailure(exception);
            }
            if (fallbackChatClient == null) {
                throw quotaExceeded();
            }
            LOGGER.warn("Primary portfolio AI model is rate limited; trying the configured fallback model");
        }

        try {
            return generate(fallbackChatClient, text);
        } catch (Exception exception) {
            if (isRateLimited(exception)) {
                throw quotaExceeded();
            }
            throw generationFailure(exception);
        }
    }

    private PortfolioSuggestResponse generate(ChatClient client, String text) {
        PortfolioSuggestResponse response = client.prompt()
                .system(SYSTEM_PROMPT)
                .user(text)
                .call()
                .entity(PortfolioSuggestResponse.class);

        if (response == null) {
            throw new AiServiceException("AI returned an empty portfolio suggestion");
        }

        return normalize(response);
    }

    private boolean isRateLimited(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof RateLimitException) {
                return true;
            }
        }
        return false;
    }

    private TooManyRequestsException quotaExceeded() {
        return new TooManyRequestsException(
                "AI provider quota is currently exhausted. Please retry in a minute."
        );
    }

    private AiServiceException generationFailure(Exception exception) {
        LOGGER.error("Failed to generate portfolio suggestion via Groq", exception);
        return exception instanceof AiServiceException aiException
                ? aiException
                : new AiServiceException("Failed to generate portfolio suggestion", exception);
    }

    private PortfolioSuggestResponse normalize(PortfolioSuggestResponse response) {
        if (response.getProjects() == null) {
            response.setProjects(java.util.List.of());
        }
        if (response.getExperiences() == null) {
            response.setExperiences(java.util.List.of());
        }
        if (response.getCertifications() == null) {
            response.setCertifications(java.util.List.of());
        }
        if (response.getTechnicalSkills() == null) {
            response.setTechnicalSkills(java.util.List.of());
        }
        if (response.getCustomLinks() == null) {
            response.setCustomLinks(java.util.List.of());
        }
        return response;
    }
}
