package com.portfolio.Stackfolio.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioSuggestRequest {

    @NotBlank(message = "Text cannot be blank")
    @Size(max = 50000, message = "Text must be at most 50000 characters")
    private String text;
}
