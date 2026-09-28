package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicalSkillRequest {

    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 100, message = "Skill name must be at most 100 characters")
    private String skillName;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    private int displayOrder;
}
