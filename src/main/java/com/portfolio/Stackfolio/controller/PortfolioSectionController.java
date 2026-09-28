package com.portfolio.Stackfolio.controller;

import com.portfolio.Stackfolio.dto.portfolio.CertificationRequest;
import com.portfolio.Stackfolio.dto.portfolio.CustomLinkRequest;
import com.portfolio.Stackfolio.dto.portfolio.ExperienceRequest;
import com.portfolio.Stackfolio.dto.portfolio.ImageUploadResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse;
import com.portfolio.Stackfolio.dto.portfolio.ProfessionalSummaryRequest;
import com.portfolio.Stackfolio.dto.portfolio.ProjectRequest;
import com.portfolio.Stackfolio.dto.portfolio.SectionResponse;
import com.portfolio.Stackfolio.dto.portfolio.TechnicalSkillRequest;
import com.portfolio.Stackfolio.service.PortfolioSectionService;
import com.portfolio.Stackfolio.service.PortfolioService;
import com.portfolio.Stackfolio.service.ProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/portfolio")
@Tag(name = "Portfolio Sections", description = "Manage authenticated user's portfolio sections")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioSectionController {

    private final PortfolioSectionService portfolioSectionService;
    private final PortfolioService portfolioService;
    private final ProfileImageService profileImageService;

    public PortfolioSectionController(
            PortfolioSectionService portfolioSectionService,
            PortfolioService portfolioService,
            ProfileImageService profileImageService
    ) {
        this.portfolioSectionService = portfolioSectionService;
        this.portfolioService = portfolioService;
        this.profileImageService = profileImageService;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's portfolio")
    public ResponseEntity<PortfolioPublicResponse> getMyPortfolio(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioService.fetchMyPortfolio(userId(authentication))
        );
    }

    @PutMapping("/summary")
    @Operation(summary = "Create or update the authenticated user's professional summary")
    public ResponseEntity<SectionResponse> upsertSummary(
            @RequestBody @Valid ProfessionalSummaryRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.upsertSummary(userId(authentication), request)
        );
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace the authenticated user's profile image (Cloudflare R2)")
    public ResponseEntity<ImageUploadResponse> uploadProfileImage(
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                profileImageService.uploadProfileImage(userId(authentication), file)
        );
    }

    @DeleteMapping("/image")
    @Operation(summary = "Delete the authenticated user's profile image")
    public ResponseEntity<Void> deleteProfileImage(
            @Parameter(hidden = true) Authentication authentication
    ) {
        profileImageService.deleteProfileImage(userId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects")
    @Operation(summary = "Create a project")
    public ResponseEntity<SectionResponse> createProject(
            @RequestBody @Valid ProjectRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioSectionService.createProject(userId(authentication), request));
    }

    @PutMapping("/projects/{projectId}")
    @Operation(summary = "Update a project")
    public ResponseEntity<SectionResponse> updateProject(
            @PathVariable Long projectId,
            @RequestBody @Valid ProjectRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.updateProject(
                        userId(authentication),
                        projectId,
                        request
                )
        );
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        portfolioSectionService.deleteProject(userId(authentication), projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/experiences")
    @Operation(summary = "Create an experience")
    public ResponseEntity<SectionResponse> createExperience(
            @RequestBody @Valid ExperienceRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioSectionService.createExperience(userId(authentication), request));
    }

    @PutMapping("/experiences/{experienceId}")
    @Operation(summary = "Update an experience")
    public ResponseEntity<SectionResponse> updateExperience(
            @PathVariable Long experienceId,
            @RequestBody @Valid ExperienceRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.updateExperience(
                        userId(authentication),
                        experienceId,
                        request
                )
        );
    }

    @DeleteMapping("/experiences/{experienceId}")
    @Operation(summary = "Delete an experience")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long experienceId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        portfolioSectionService.deleteExperience(userId(authentication), experienceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/certifications")
    @Operation(summary = "Create a certification")
    public ResponseEntity<SectionResponse> createCertification(
            @RequestBody @Valid CertificationRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioSectionService.createCertification(userId(authentication), request));
    }

    @PutMapping("/certifications/{certificationId}")
    @Operation(summary = "Update a certification")
    public ResponseEntity<SectionResponse> updateCertification(
            @PathVariable Long certificationId,
            @RequestBody @Valid CertificationRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.updateCertification(
                        userId(authentication),
                        certificationId,
                        request
                )
        );
    }

    @DeleteMapping("/certifications/{certificationId}")
    @Operation(summary = "Delete a certification")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable Long certificationId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        portfolioSectionService.deleteCertification(userId(authentication), certificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/technical-skills")
    @Operation(summary = "Create a technical skill")
    public ResponseEntity<SectionResponse> createTechnicalSkill(
            @RequestBody @Valid TechnicalSkillRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioSectionService.createTechnicalSkill(userId(authentication), request));
    }

    @PutMapping("/technical-skills/{technicalSkillId}")
    @Operation(summary = "Update a technical skill")
    public ResponseEntity<SectionResponse> updateTechnicalSkill(
            @PathVariable Long technicalSkillId,
            @RequestBody @Valid TechnicalSkillRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.updateTechnicalSkill(
                        userId(authentication),
                        technicalSkillId,
                        request
                )
        );
    }

    @DeleteMapping("/technical-skills/{technicalSkillId}")
    @Operation(summary = "Delete a technical skill")
    public ResponseEntity<Void> deleteTechnicalSkill(
            @PathVariable Long technicalSkillId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        portfolioSectionService.deleteTechnicalSkill(userId(authentication), technicalSkillId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custom-links")
    @Operation(summary = "Create a custom link")
    public ResponseEntity<SectionResponse> createCustomLink(
            @RequestBody @Valid CustomLinkRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioSectionService.createCustomLink(userId(authentication), request));
    }

    @PutMapping("/custom-links/{customLinkId}")
    @Operation(summary = "Update a custom link")
    public ResponseEntity<SectionResponse> updateCustomLink(
            @PathVariable Long customLinkId,
            @RequestBody @Valid CustomLinkRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                portfolioSectionService.updateCustomLink(
                        userId(authentication),
                        customLinkId,
                        request
                )
        );
    }

    @DeleteMapping("/custom-links/{customLinkId}")
    @Operation(summary = "Delete a custom link")
    public ResponseEntity<Void> deleteCustomLink(
            @PathVariable Long customLinkId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        portfolioSectionService.deleteCustomLink(userId(authentication), customLinkId);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication authentication) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuth.getToken();
        return jwt.getClaim("userId");
    }
}
