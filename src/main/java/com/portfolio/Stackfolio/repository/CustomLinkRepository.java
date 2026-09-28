package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.CustomLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomLinkRepository extends JpaRepository<CustomLink, Long> {

    Optional<CustomLink> findByIdAndPortfolio_User_Id(Long id, Long userId);
}
