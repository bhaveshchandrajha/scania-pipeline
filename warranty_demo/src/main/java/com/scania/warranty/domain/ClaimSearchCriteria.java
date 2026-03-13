/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

/**
 * Value object for claim search criteria.
 */
public record ClaimSearchCriteria(
    String companyCode,
    String filterType,
    String searchText,
    Integer ageFilterDays,
    String claimTypeFilter,
    boolean openClaimsOnly,
    String statusFilter,
    String statusOperator,
    String vehicleFilter,
    String customerFilter,
    String sdeClaimFilter,
    boolean minimumOnly,
    String sortField,
    boolean ascending
) {
}