package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.ai.PortfolioSuggestResponse;
import com.portfolio.Stackfolio.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
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

    public PortfolioSuggestService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public PortfolioSuggestResponse suggest(String text) {
        try {
            PortfolioSuggestResponse response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(text)
                    .call()
                    .entity(PortfolioSuggestResponse.class);

            if (response == null) {
                throw new AiServiceException("AI returned an empty portfolio suggestion");
            }

            return normalize(response);
        } catch (AiServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error("Failed to generate portfolio suggestion via Groq", exception);
            throw new AiServiceException("Failed to generate portfolio suggestion", exception);
        }
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
