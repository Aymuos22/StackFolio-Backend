package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    Optional<Experience> findByIdAndPortfolio_User_Id(Long id, Long userId);
}
