package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    Optional<Certification> findByIdAndPortfolio_User_Id(Long id, Long userId);
}
