package com.scania.warranty.domain;

public record ClaimSearchCriteria(
    String companyCode,
    String statusFilter,
    String statusOperator,
    String vehicleNumber,
    String customerNumber,
    String claimNumberSde,
    String claimType,
    boolean openClaimsOnly,
    boolean minimumOnly,
    Integer claimAgeDays,
    String searchText
) {
    public ClaimSearchCriteria {
        if (statusOperator == null || statusOperator.isBlank()) {
            statusOperator = "=";
        }
    }
}