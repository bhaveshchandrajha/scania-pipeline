package com.scania.warranty.domain;

public record ClaimSearchCriteria(
    String pakz,
    String status,
    String filterOperator,
    String vehicleNumber,
    String customerNumber,
    String claimNumberSde,
    String claimType,
    boolean openClaimsOnly,
    boolean minimumOnly,
    Integer filterDays
) {
}