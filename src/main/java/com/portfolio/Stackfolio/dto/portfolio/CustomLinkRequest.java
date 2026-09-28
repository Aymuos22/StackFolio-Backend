package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomLinkRequest {

    @NotBlank(message = "Label cannot be blank")
    @Size(max = 100, message = "Label must be at most 100 characters")
    private String label;

    @NotBlank(message = "URL cannot be blank")
    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    @Size(max = 100, message = "Icon must be at most 100 characters")
    private String icon;

    private int displayOrder;
}
