package com.portfolio.Stackfolio.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private String description;
    private String techStack;

    @Size(max = 500, message = "GitHub URL must be at most 500 characters")
    private String githubUrl;

    @Size(max = 500, message = "Live URL must be at most 500 characters")
    private String liveUrl;

    @Size(max = 500, message = "Image URL must be at most 500 characters")
    private String imageUrl;

    private int displayOrder;
}
