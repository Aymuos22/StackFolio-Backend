package com.portfolio.Stackfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(name = "public_email", length = 255)
    private String publicEmail;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(length = 100)
    private String theme;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private ProfessionalSummary professionalSummary;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<TechnicalSkill> technicalSkills = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<CustomLink> customLinks = new ArrayList<>();
}
