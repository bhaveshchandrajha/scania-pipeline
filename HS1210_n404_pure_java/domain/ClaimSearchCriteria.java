package com.scania.warranty.domain;

public record ClaimSearchCriteria(
    String companyCode,
    String statusFilter,
    String statusOperator,
    String vehicleNumberFilter,
    String customerNumberFilter,
    String sdeClaimNumberFilter,
    String claimTypeFilter,
    Integer claimAgeDays,
    boolean openClaimsOnly,
    boolean minimumRequestsOnly,
    String searchText,
    boolean sortAscending,
    String sortField
) {
    public ClaimSearchCriteria {
        if (statusOperator == null || statusOperator.isBlank()) {
            statusOperator = "=";
        }
    }
}