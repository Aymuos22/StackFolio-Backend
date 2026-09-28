package com.portfolio.Stackfolio.repository;

import com.portfolio.Stackfolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndPortfolio_User_Id(Long id, Long userId);
}
