package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.TechnicalSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicalSkillRepository extends JpaRepository<TechnicalSkill, Long> {

    Optional<TechnicalSkill> findByIdAndPortfolio_User_Id(Long id, Long userId);
}
