package com.scania.warranty.dto;

public record ClaimFilterCriteria(
        String status,
        String operator,
        String vehicleNumber,
        String customerNumber,
        String sdeDate,
        String dealerId,
        Integer maxDays,
        String claimType,
        String minimumOnly,
        String openOnly,
        String searchTerm
) {
}