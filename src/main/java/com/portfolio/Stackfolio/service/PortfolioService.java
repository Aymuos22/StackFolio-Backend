package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse.CertificationResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse.CustomLinkResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse.ExperienceResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse.ProjectResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse.TechnicalSkillResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioRequest;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioResponse;
import com.portfolio.Stackfolio.entity.Certification;
import com.portfolio.Stackfolio.entity.CustomLink;
import com.portfolio.Stackfolio.entity.Experience;
import com.portfolio.Stackfolio.entity.Portfolio;
import com.portfolio.Stackfolio.entity.ProfessionalSummary;
import com.portfolio.Stackfolio.entity.Project;
import com.portfolio.Stackfolio.entity.TechnicalSkill;
import com.portfolio.Stackfolio.entity.User;
import com.portfolio.Stackfolio.exception.ConflictException;
import com.portfolio.Stackfolio.exception.NotFoundException;
import com.portfolio.Stackfolio.repository.PortfolioRepository;
import com.portfolio.Stackfolio.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            UserRepository userRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    public Portfolio getPortfolioById(Long userId) {

        Portfolio portfolio = portfolioRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));

        return portfolio;
    }

    @Transactional(readOnly = true)
    public PortfolioPublicResponse fetchPortfolio(String slug) {

        Portfolio portfolio = portfolioRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new NotFoundException("Portfolio not found")
                );

        return mapPortfolioPublicResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioPublicResponse fetchMyPortfolio(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new NotFoundException("Portfolio not found")
                );

        return mapPortfolioPublicResponse(portfolio);
    }

    private PortfolioPublicResponse mapPortfolioPublicResponse(Portfolio portfolio) {
        ProfessionalSummary professionalSummary =
                portfolio.getProfessionalSummary();

        return new PortfolioPublicResponse(
                portfolio.getId(),
                portfolio.getSlug(),
                portfolio.getFullName(),
                portfolio.getPhone(),
                portfolio.getPublicEmail(),
                portfolio.getLinkedinUrl(),
                portfolio.getGithubUrl(),
                portfolio.getProfileImageUrl(),
                portfolio.getTheme(),
                portfolio.getPrimaryColor(),
                portfolio.getSecondaryColor(),
                professionalSummary == null
                        ? null
                        : professionalSummary.getSummary(),
                mapProjects(portfolio.getProjects()),
                mapExperiences(portfolio.getExperiences()),
                mapCertifications(portfolio.getCertifications()),
                mapTechnicalSkills(portfolio.getTechnicalSkills()),
                mapCustomLinks(portfolio.getCustomLinks())
        );
    }

    private List<ProjectResponse> mapProjects(List<Project> projects) {
        return projects.stream()
                .sorted(Comparator.comparingInt(Project::getDisplayOrder))
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getTitle(),
                        project.getDescription(),
                        project.getTechStack(),
                        project.getGithubUrl(),
                        project.getLiveUrl(),
                        project.getImageUrl(),
                        project.getDisplayOrder()
                ))
                .toList();
    }

    private List<ExperienceResponse> mapExperiences(List<Experience> experiences) {
        return experiences.stream()
                .sorted(Comparator.comparingInt(Experience::getDisplayOrder))
                .map(experience -> new ExperienceResponse(
                        experience.getId(),
                        experience.getCompanyName(),
                        experience.getJobTitle(),
                        experience.getEmploymentType(),
                        experience.getLocation(),
                        experience.getStartDate(),
                        experience.getEndDate(),
                        experience.isCurrentlyWorking(),
                        experience.getDescription(),
                        experience.getDisplayOrder()
                ))
                .toList();
    }

    private List<CertificationResponse> mapCertifications(
            List<Certification> certifications
    ) {
        return certifications.stream()
                .sorted(Comparator.comparingInt(Certification::getDisplayOrder))
                .map(certification -> new CertificationResponse(
                        certification.getId(),
                        certification.getName(),
                        certification.getIssuingOrganization(),
                        certification.getIssueDate(),
                        certification.getExpiryDate(),
                        certification.getCredentialId(),
                        certification.getCredentialUrl(),
                        certification.getDisplayOrder()
                ))
                .toList();
    }

    private List<TechnicalSkillResponse> mapTechnicalSkills(
            List<TechnicalSkill> technicalSkills
    ) {
        return technicalSkills.stream()
                .sorted(Comparator.comparingInt(TechnicalSkill::getDisplayOrder))
                .map(technicalSkill -> new TechnicalSkillResponse(
                        technicalSkill.getId(),
                        technicalSkill.getSkillName(),
                        technicalSkill.getCategory(),
                        technicalSkill.getDisplayOrder()
                ))
                .toList();
    }

    private List<CustomLinkResponse> mapCustomLinks(List<CustomLink> customLinks) {
        return customLinks.stream()
                .sorted(Comparator.comparingInt(CustomLink::getDisplayOrder))
                .map(customLink -> new CustomLinkResponse(
                        customLink.getId(),
                        customLink.getLabel(),
                        customLink.getUrl(),
                        customLink.getIcon(),
                        customLink.getDisplayOrder()
                ))
                .toList();
    }

    @Transactional
    public PortfolioResponse createPortfolio(
            PortfolioRequest request,
            Long userId
    ) {
        if (portfolioRepository.existsByUser_Id(userId)) {
            throw new ConflictException("User already has a portfolio");
        }

        if (portfolioRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Portfolio slug already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found: " + userId)
                );

        Portfolio portfolio = new Portfolio();

        portfolio.setUser(user);
        portfolio.setSlug(request.getSlug());
        portfolio.setFullName(request.getFullName());
        portfolio.setPhone(request.getPhone());
        portfolio.setPublicEmail(request.getPublicEmail());
        portfolio.setLinkedinUrl(request.getLinkedinUrl());
        portfolio.setGithubUrl(request.getGithubUrl());
        portfolio.setTheme(request.getTheme());
        portfolio.setPrimaryColor(request.getPrimaryColor());
        portfolio.setSecondaryColor(request.getSecondaryColor());

        LocalDateTime now = LocalDateTime.now();

        portfolio.setCreatedAt(now);
        portfolio.setUpdatedAt(now);

        Portfolio savedPortfolio =
                portfolioRepository.save(portfolio);

        return new PortfolioResponse(
                savedPortfolio.getId(),
                savedPortfolio.getSlug(),
                "Portfolio created successfully"
        );
    }

    @Transactional
    public PortfolioResponse updatePortfolio(
            PortfolioRequest request,
            Long userId
    ) {
        if (portfolioRepository.existsBySlugAndUser_IdNot(request.getSlug(), userId)) {
            throw new ConflictException("Portfolio slug already exists");
        }

        Portfolio existingPortfolio =
                portfolioRepository.findByUser_Id(userId)
                        .orElseThrow(() ->
                                new NotFoundException("Portfolio not found")
                        );

        existingPortfolio.setSlug(request.getSlug());
        existingPortfolio.setFullName(request.getFullName());
        existingPortfolio.setPhone(request.getPhone());
        existingPortfolio.setPublicEmail(request.getPublicEmail());
        existingPortfolio.setLinkedinUrl(request.getLinkedinUrl());
        existingPortfolio.setGithubUrl(request.getGithubUrl());
        existingPortfolio.setTheme(request.getTheme());
        existingPortfolio.setPrimaryColor(request.getPrimaryColor());
        existingPortfolio.setSecondaryColor(request.getSecondaryColor());

        existingPortfolio.setUpdatedAt(LocalDateTime.now());

        Portfolio savedPortfolio =
                portfolioRepository.save(existingPortfolio);

        return new PortfolioResponse(
                savedPortfolio.getId(),
                savedPortfolio.getSlug(),
                "Portfolio updated successfully"
        );
    }
}
