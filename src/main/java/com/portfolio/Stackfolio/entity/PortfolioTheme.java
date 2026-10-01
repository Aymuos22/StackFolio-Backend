package com.portfolio.Stackfolio.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Public portfolio layout theme",
        allowableValues = {"comic", "minimalist", "dark-tech"}
)
public enum PortfolioTheme {

    COMIC("comic"),
    MINIMALIST("minimalist"),
    DARK_TECH("dark-tech");

    public static final String ALLOWED_VALUES_MESSAGE =
            "theme must be one of: comic, minimalist, dark-tech";

    private final String value;

    PortfolioTheme(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PortfolioTheme fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        for (PortfolioTheme theme : values()) {
            if (theme.value.equals(raw)) {
                return theme;
            }
        }

        throw new IllegalArgumentException(ALLOWED_VALUES_MESSAGE);
    }
}
