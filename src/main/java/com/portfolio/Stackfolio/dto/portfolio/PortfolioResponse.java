package com.portfolio.Stackfolio.dto.portfolio;

import com.portfolio.Stackfolio.entity.PortfolioTheme;

public class PortfolioResponse {

    private Long id;
    private String slug;
    private PortfolioTheme theme;
    private String message;

    public PortfolioResponse(Long id, String slug, PortfolioTheme theme, String message) {
        this.id = id;
        this.slug = slug;
        this.theme = theme;
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

    public PortfolioTheme getTheme() {
        return theme;
    }

    public void setTheme(PortfolioTheme theme) {
        this.theme = theme;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
