package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfessionalSummaryRequest {

    @NotBlank(message = "Summary cannot be blank")
    private String summary;
}
