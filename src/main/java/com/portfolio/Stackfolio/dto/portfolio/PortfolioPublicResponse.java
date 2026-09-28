package com.portfolio.Stackfolio.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class PortfolioPublicResponse {

    private Long id;
    private String slug;
    private String fullName;
    private String phone;
    private String publicEmail;
    private String linkedinUrl;
    private String githubUrl;
    private String theme;
    private String primaryColor;
    private String secondaryColor;
    private String summary;
    private List<ProjectResponse> projects;
    private List<ExperienceResponse> experiences;
    private List<CertificationResponse> certifications;
    private List<TechnicalSkillResponse> technicalSkills;
    private List<CustomLinkResponse> customLinks;

    @Getter
    @AllArgsConstructor
    public static class ProjectResponse {
        private Long id;
        private String title;
        private String description;
        private String techStack;
        private String githubUrl;
        private String liveUrl;
        private String imageUrl;
        private int displayOrder;
    }

    @Getter
    @AllArgsConstructor
    public static class ExperienceResponse {
        private Long id;
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
    @AllArgsConstructor
    public static class CertificationResponse {
        private Long id;
        private String name;
        private String issuingOrganization;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialId;
        private String credentialUrl;
        private int displayOrder;
    }

    @Getter
    @AllArgsConstructor
    public static class TechnicalSkillResponse {
        private Long id;
        private String skillName;
        private String category;
        private int displayOrder;
    }

    @Getter
    @AllArgsConstructor
    public static class CustomLinkResponse {
        private Long id;
        private String label;
        private String url;
        private String icon;
        private int displayOrder;
    }
}
