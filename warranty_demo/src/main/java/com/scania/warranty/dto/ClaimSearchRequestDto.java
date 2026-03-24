/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

import com.scania.warranty.domain.SortDirection;

public record ClaimSearchRequestDto(
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
) {} // @rpg-trace: n436