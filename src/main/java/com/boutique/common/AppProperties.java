package com.boutique.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String siteName,
        String currency,
        BigDecimal freeShippingThreshold,
        BigDecimal shippingFee,
        List<String> corsOrigins,
        String uploadDir,
        Security security
) {
    public record Security(String jwtSecret, int tokenHours, String adminEmail, String adminPassword, String adminName) {}
}
