/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

import java.time.LocalDate;

public record ClaimSearchCriteria(
    String companyCode,
    boolean ascending,
    Integer filterAgeDays,
    String filterType,
    String filterOpen,
    String searchString,
    String statusFilter,
    String statusOperator,
    String filterCompany,
    String filterVehicle,
    String filterCustomer,
    String filterSde,
    LocalDate currentDate
) {
}