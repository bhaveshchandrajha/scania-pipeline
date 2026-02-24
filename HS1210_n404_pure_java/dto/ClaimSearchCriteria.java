package com.scania.warranty.dto;

public record ClaimSearchCriteria(
    String pakz,
    Integer filterClaimAge,
    String filterClaimType,
    boolean filterOpenOnly,
    Integer statusFilter,
    String statusComparison,
    String searchString,
    boolean sortAscending,
    String filterPakz,
    String filterVehicle,
    String filterCustomer,
    String filterSdeClaimNr
) {
    public ClaimSearchCriteria {
        if (pakz == null || pakz.isBlank()) {
            throw new IllegalArgumentException("pakz cannot be null or blank");
        }
    }
}