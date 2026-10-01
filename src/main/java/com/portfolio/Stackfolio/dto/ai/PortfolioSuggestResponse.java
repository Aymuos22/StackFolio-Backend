package com.portfolio.Stackfolio.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioSuggestResponse {

    private String fullName;
    private String phone;
    private String publicEmail;
    private String linkedinUrl;
    private String githubUrl;
    private String summary;
    private List<ProjectSuggestion> projects = new ArrayList<>();
    private List<ExperienceSuggestion> experiences = new ArrayList<>();
    private List<CertificationSuggestion> certifications = new ArrayList<>();
    private List<TechnicalSkillSuggestion> technicalSkills = new ArrayList<>();
    private List<CustomLinkSuggestion> customLinks = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProjectSuggestion {
        private String title;
        private String description;
        private String techStack;
        private String githubUrl;
        private String liveUrl;
        private int displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExperienceSuggestion {
        private String companyName;
        private String jobTitle;
        private String employmentType;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean currentlyWorking;
        private String description;
        private int displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CertificationSuggestion {
        private String name;
        private String issuingOrganization;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialId;
        private String credentialUrl;
        private int displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TechnicalSkillSuggestion {
        private String skillName;
        private String category;
        private int displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CustomLinkSuggestion {
        private String label;
        private String url;
        private String icon;
        private int displayOrder;
    }
}
