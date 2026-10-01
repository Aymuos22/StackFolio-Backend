package com.portfolio.Stackfolio.dto.portfolio;

import com.portfolio.Stackfolio.entity.PortfolioTheme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioRequest {

    @NotBlank(message = "Slug cannot be blank")
    @Size(max = 100, message = "Slug must be at most 100 characters")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug may contain lowercase letters, numbers, and hyphens"
    )
    private String slug;

    @NotBlank(message = "Full name cannot be blank")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Email(message = "Public email should be valid")
    @Size(max = 255, message = "Public email must be at most 255 characters")
    private String publicEmail;

    @Size(max = 500, message = "LinkedIn URL must be at most 500 characters")
    private String linkedinUrl;

    @Size(max = 500, message = "GitHub URL must be at most 500 characters")
    private String githubUrl;

    @Schema(
            description = "Public portfolio layout theme",
            allowableValues = {"comic", "minimalist", "dark-tech"},
            example = "comic"
    )
    private PortfolioTheme theme;

    @Pattern(
            regexp = "^$|^#[0-9a-fA-F]{6}$",
            message = "Primary color must be a hex color like #2563eb"
    )
    private String primaryColor;

    @Pattern(
            regexp = "^$|^#[0-9a-fA-F]{6}$",
            message = "Secondary color must be a hex color like #111827"
    )
    private String secondaryColor;
}
