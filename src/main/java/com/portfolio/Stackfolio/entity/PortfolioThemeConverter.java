package com.portfolio.Stackfolio.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PortfolioThemeConverter implements AttributeConverter<PortfolioTheme, String> {

    @Override
    public String convertToDatabaseColumn(PortfolioTheme attribute) {
        return attribute == null ? PortfolioTheme.COMIC.getValue() : attribute.getValue();
    }

    @Override
    public PortfolioTheme convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return PortfolioTheme.COMIC;
        }
        return PortfolioTheme.fromValue(dbData);
    }
}
