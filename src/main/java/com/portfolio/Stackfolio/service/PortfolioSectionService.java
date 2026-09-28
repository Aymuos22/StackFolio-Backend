package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.portfolio.CertificationRequest;
import com.portfolio.Stackfolio.dto.portfolio.CustomLinkRequest;
import com.portfolio.Stackfolio.dto.portfolio.ExperienceRequest;
import com.portfolio.Stackfolio.dto.portfolio.ProfessionalSummaryRequest;
import com.portfolio.Stackfolio.dto.portfolio.ProjectRequest;
import com.portfolio.Stackfolio.dto.portfolio.SectionResponse;
import com.portfolio.Stackfolio.dto.portfolio.TechnicalSkillRequest;
import com.portfolio.Stackfolio.entity.Certification;
import com.portfolio.Stackfolio.entity.CustomLink;
import com.portfolio.Stackfolio.entity.Experience;
import com.portfolio.Stackfolio.entity.Portfolio;
import com.portfolio.Stackfolio.entity.ProfessionalSummary;
import com.portfolio.Stackfolio.entity.Project;
import com.portfolio.Stackfolio.entity.TechnicalSkill;
import com.portfolio.Stackfolio.exception.NotFoundException;
import com.portfolio.Stackfolio.repository.CertificationRepository;
import com.portfolio.Stackfolio.repository.CustomLinkRepository;
import com.portfolio.Stackfolio.repository.ExperienceRepository;
import com.portfolio.Stackfolio.repository.PortfolioRepository;
import com.portfolio.Stackfolio.repository.ProfessionalSummaryRepository;
import com.portfolio.Stackfolio.repository.ProjectRepository;
import com.portfolio.Stackfolio.repository.TechnicalSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioSectionService {

    private final PortfolioRepository portfolioRepository;
    private final ProfessionalSummaryRepository professionalSummaryRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificationRepository certificationRepository;
    private final TechnicalSkillRepository technicalSkillRepository;
    private final CustomLinkRepository customLinkRepository;

    public PortfolioSectionService(
            PortfolioRepository portfolioRepository,
            ProfessionalSummaryRepository professionalSummaryRepository,
            ProjectRepository projectRepository,
            ExperienceRepository experienceRepository,
            CertificationRepository certificationRepository,
            TechnicalSkillRepository technicalSkillRepository,
            CustomLinkRepository customLinkRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.professionalSummaryRepository = professionalSummaryRepository;
        this.projectRepository = projectRepository;
        this.experienceRepository = experienceRepository;
        this.certificationRepository = certificationRepository;
        this.technicalSkillRepository = technicalSkillRepository;
        this.customLinkRepository = customLinkRepository;
    }

    @Transactional
    public SectionResponse upsertSummary(
            Long userId,
            ProfessionalSummaryRequest request
    ) {
        Portfolio portfolio = getPortfolio(userId);

        ProfessionalSummary summary = professionalSummaryRepository
                .findByPortfolio_User_Id(userId)
                .orElseGet(ProfessionalSummary::new);

        summary.setPortfolio(portfolio);
        summary.setSummary(request.getSummary());

        ProfessionalSummary saved =
                professionalSummaryRepository.save(summary);

        return new SectionResponse(
                saved.getId(),
                "Professional summary saved successfully"
        );
    }

    @Transactional
    public SectionResponse createProject(Long userId, ProjectRequest request) {
        Project project = new Project();
        project.setPortfolio(getPortfolio(userId));
        applyProject(request, project);

        Project saved = projectRepository.save(project);
        return new SectionResponse(saved.getId(), "Project created successfully");
    }

    @Transactional
    public SectionResponse updateProject(
            Long userId,
            Long projectId,
            ProjectRequest request
    ) {
        Project project = projectRepository
                .findByIdAndPortfolio_User_Id(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        applyProject(request, project);

        Project saved = projectRepository.save(project);
        return new SectionResponse(saved.getId(), "Project updated successfully");
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        Project project = projectRepository
                .findByIdAndPortfolio_User_Id(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        projectRepository.delete(project);
    }

    @Transactional
    public SectionResponse createExperience(
            Long userId,
            ExperienceRequest request
    ) {
        Experience experience = new Experience();
        experience.setPortfolio(getPortfolio(userId));
        applyExperience(request, experience);

        Experience saved = experienceRepository.save(experience);
        return new SectionResponse(saved.getId(), "Experience created successfully");
    }

    @Transactional
    public SectionResponse updateExperience(
            Long userId,
            Long experienceId,
            ExperienceRequest request
    ) {
        Experience experience = experienceRepository
                .findByIdAndPortfolio_User_Id(experienceId, userId)
                .orElseThrow(() -> new NotFoundException("Experience not found"));

        applyExperience(request, experience);

        Experience saved = experienceRepository.save(experience);
        return new SectionResponse(saved.getId(), "Experience updated successfully");
    }

    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository
                .findByIdAndPortfolio_User_Id(experienceId, userId)
                .orElseThrow(() -> new NotFoundException("Experience not found"));

        experienceRepository.delete(experience);
    }

    @Transactional
    public SectionResponse createCertification(
            Long userId,
            CertificationRequest request
    ) {
        Certification certification = new Certification();
        certification.setPortfolio(getPortfolio(userId));
        applyCertification(request, certification);

        Certification saved = certificationRepository.save(certification);
        return new SectionResponse(saved.getId(), "Certification created successfully");
    }

    @Transactional
    public SectionResponse updateCertification(
            Long userId,
            Long certificationId,
            CertificationRequest request
    ) {
        Certification certification = certificationRepository
                .findByIdAndPortfolio_User_Id(certificationId, userId)
                .orElseThrow(() -> new NotFoundException("Certification not found"));

        applyCertification(request, certification);

        Certification saved = certificationRepository.save(certification);
        return new SectionResponse(saved.getId(), "Certification updated successfully");
    }

    @Transactional
    public void deleteCertification(Long userId, Long certificationId) {
        Certification certification = certificationRepository
                .findByIdAndPortfolio_User_Id(certificationId, userId)
                .orElseThrow(() -> new NotFoundException("Certification not found"));

        certificationRepository.delete(certification);
    }

    @Transactional
    public SectionResponse createTechnicalSkill(
            Long userId,
            TechnicalSkillRequest request
    ) {
        TechnicalSkill technicalSkill = new TechnicalSkill();
        technicalSkill.setPortfolio(getPortfolio(userId));
        applyTechnicalSkill(request, technicalSkill);

        TechnicalSkill saved = technicalSkillRepository.save(technicalSkill);
        return new SectionResponse(saved.getId(), "Technical skill created successfully");
    }

    @Transactional
    public SectionResponse updateTechnicalSkill(
            Long userId,
            Long technicalSkillId,
            TechnicalSkillRequest request
    ) {
        TechnicalSkill technicalSkill = technicalSkillRepository
                .findByIdAndPortfolio_User_Id(technicalSkillId, userId)
                .orElseThrow(() -> new NotFoundException("Technical skill not found"));

        applyTechnicalSkill(request, technicalSkill);

        TechnicalSkill saved = technicalSkillRepository.save(technicalSkill);
        return new SectionResponse(saved.getId(), "Technical skill updated successfully");
    }

    @Transactional
    public void deleteTechnicalSkill(Long userId, Long technicalSkillId) {
        TechnicalSkill technicalSkill = technicalSkillRepository
                .findByIdAndPortfolio_User_Id(technicalSkillId, userId)
                .orElseThrow(() -> new NotFoundException("Technical skill not found"));

        technicalSkillRepository.delete(technicalSkill);
    }

    @Transactional
    public SectionResponse createCustomLink(
            Long userId,
            CustomLinkRequest request
    ) {
        CustomLink customLink = new CustomLink();
        customLink.setPortfolio(getPortfolio(userId));
        applyCustomLink(request, customLink);

        CustomLink saved = customLinkRepository.save(customLink);
        return new SectionResponse(saved.getId(), "Custom link created successfully");
    }

    @Transactional
    public SectionResponse updateCustomLink(
            Long userId,
            Long customLinkId,
            CustomLinkRequest request
    ) {
        CustomLink customLink = customLinkRepository
                .findByIdAndPortfolio_User_Id(customLinkId, userId)
                .orElseThrow(() -> new NotFoundException("Custom link not found"));

        applyCustomLink(request, customLink);

        CustomLink saved = customLinkRepository.save(customLink);
        return new SectionResponse(saved.getId(), "Custom link updated successfully");
    }

    @Transactional
    public void deleteCustomLink(Long userId, Long customLinkId) {
        CustomLink customLink = customLinkRepository
                .findByIdAndPortfolio_User_Id(customLinkId, userId)
                .orElseThrow(() -> new NotFoundException("Custom link not found"));

        customLinkRepository.delete(customLink);
    }

    private Portfolio getPortfolio(Long userId) {
        return portfolioRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
    }

    private void applyProject(ProjectRequest request, Project project) {
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setGithubUrl(request.getGithubUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setImageUrl(request.getImageUrl());
        project.setDisplayOrder(request.getDisplayOrder());
    }

    private void applyExperience(
            ExperienceRequest request,
            Experience experience
    ) {
        experience.setCompanyName(request.getCompanyName());
        experience.setJobTitle(request.getJobTitle());
        experience.setEmploymentType(request.getEmploymentType());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setCurrentlyWorking(request.isCurrentlyWorking());
        experience.setDescription(request.getDescription());
        experience.setDisplayOrder(request.getDisplayOrder());
    }

    private void applyCertification(
            CertificationRequest request,
            Certification certification
    ) {
        certification.setName(request.getName());
        certification.setIssuingOrganization(request.getIssuingOrganization());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpiryDate(request.getExpiryDate());
        certification.setCredentialId(request.getCredentialId());
        certification.setCredentialUrl(request.getCredentialUrl());
        certification.setDisplayOrder(request.getDisplayOrder());
    }

    private void applyTechnicalSkill(
            TechnicalSkillRequest request,
            TechnicalSkill technicalSkill
    ) {
        technicalSkill.setSkillName(request.getSkillName());
        technicalSkill.setCategory(request.getCategory());
        technicalSkill.setDisplayOrder(request.getDisplayOrder());
    }

    private void applyCustomLink(
            CustomLinkRequest request,
            CustomLink customLink
    ) {
        customLink.setLabel(request.getLabel());
        customLink.setUrl(request.getUrl());
        customLink.setIcon(request.getIcon());
        customLink.setDisplayOrder(request.getDisplayOrder());
    }
}
