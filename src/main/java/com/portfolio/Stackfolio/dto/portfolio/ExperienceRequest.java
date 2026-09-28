package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExperienceRequest {

    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 255, message = "Company name must be at most 255 characters")
    private String companyName;

    @NotBlank(message = "Job title cannot be blank")
    @Size(max = 255, message = "Job title must be at most 255 characters")
    private String jobTitle;

    @Size(max = 100, message = "Employment type must be at most 100 characters")
    private String employmentType;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private boolean currentlyWorking;
    private String description;
    private int displayOrder;
}
