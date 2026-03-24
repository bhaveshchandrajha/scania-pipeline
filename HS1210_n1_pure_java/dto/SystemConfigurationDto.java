package com.scania.warranty.dto;

import java.math.BigDecimal;

public record SystemConfigurationDto(
        String key,
        BigDecimal maxAgeOfClaimMonths,
        BigDecimal sssClaimValue,
        BigDecimal currentExchangeRate,
        String customerCompanyCode
) {
}