package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CertificationRequest {

    @NotBlank(message = "Certification name cannot be blank")
    @Size(max = 255, message = "Certification name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Issuing organization must be at most 255 characters")
    private String issuingOrganization;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Size(max = 255, message = "Credential ID must be at most 255 characters")
    private String credentialId;

    @Size(max = 500, message = "Credential URL must be at most 500 characters")
    private String credentialUrl;

    private int displayOrder;
}
