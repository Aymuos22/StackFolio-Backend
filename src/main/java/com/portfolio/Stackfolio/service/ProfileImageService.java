package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.portfolio.ImageUploadResponse;
import com.portfolio.Stackfolio.entity.Portfolio;
import com.portfolio.Stackfolio.exception.NotFoundException;
import com.portfolio.Stackfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageService {

    private final PortfolioRepository portfolioRepository;
    private final R2StorageService r2StorageService;

    public ProfileImageService(
            PortfolioRepository portfolioRepository,
            R2StorageService r2StorageService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.r2StorageService = r2StorageService;
    }

    @Transactional
    public ImageUploadResponse uploadProfileImage(Long userId, MultipartFile file) {
        Portfolio portfolio = portfolioRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));

        String previousUrl = portfolio.getProfileImageUrl();
        String imageUrl = r2StorageService.uploadProfileImage(userId, file);

        portfolio.setProfileImageUrl(imageUrl);
        portfolioRepository.save(portfolio);

        if (previousUrl != null && !previousUrl.equals(imageUrl)) {
            r2StorageService.deleteByPublicUrl(previousUrl);
        }

        return new ImageUploadResponse(imageUrl, "Profile image uploaded successfully");
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));

        String previousUrl = portfolio.getProfileImageUrl();
        portfolio.setProfileImageUrl(null);
        portfolioRepository.save(portfolio);

        r2StorageService.deleteByPublicUrl(previousUrl);
    }
}
