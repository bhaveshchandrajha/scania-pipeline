/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

public record ClaimSearchCriteria(
    String companyCode,
    String status,
    String statusCompareSign,
    String filterBranch,
    String filterCustomer,
    String filterSdeClaimNr,
    String filterType,
    int filterAgeDays,
    boolean filterOpenOnly,
    boolean filterMinimumOnly,
    String searchString,
    SortDirection sortDirection,
    boolean sortByClaimNr
) {}