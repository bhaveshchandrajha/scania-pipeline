package com.scania.warranty.dto;

public record DealerConfigurationDto(
        String dealerId,
        String companyName,
        String countryCode,
        int maxClaimAgeDays,
        int claimFilterDays
) {
}