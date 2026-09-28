package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio,Long> {
    Optional<Portfolio> findByUser_Id(Long userId);
    Optional<Portfolio> findBySlug(String slug);
    boolean existsByUser_Id(Long userId);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndUser_IdNot(String slug, Long userId);
}
