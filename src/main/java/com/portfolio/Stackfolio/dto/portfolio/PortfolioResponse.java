package com.portfolio.Stackfolio.dto.portfolio;

public class PortfolioResponse {

    private Long id;
    private String slug;
    private String message;

    public PortfolioResponse(Long id, String slug, String message) {
        this.id = id;
        this.slug = slug;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}