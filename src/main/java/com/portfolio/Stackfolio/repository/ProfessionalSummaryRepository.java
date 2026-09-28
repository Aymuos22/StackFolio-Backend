package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.ProfessionalSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionalSummaryRepository
        extends JpaRepository<ProfessionalSummary, Long> {

    Optional<ProfessionalSummary> findByPortfolio_User_Id(Long userId);
}
