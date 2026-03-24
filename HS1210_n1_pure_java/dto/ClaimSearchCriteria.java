package com.scania.warranty.dto;

public record ClaimSearchCriteria(
        String dealerId,
        String status,
        String vehicleNumber,
        String customerNumber,
        String sdeNumber,
        Integer filterDays,
        String claimType,
        boolean showOnlyOpen,
        boolean showOnlyMinimum
) {
}