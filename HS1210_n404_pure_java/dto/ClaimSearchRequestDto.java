package com.scania.warranty.dto;

public record ClaimSearchRequestDto(
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
) {}