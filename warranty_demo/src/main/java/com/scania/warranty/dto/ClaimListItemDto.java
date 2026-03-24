/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimListItemDto(
    String companyCode,
    String invoiceNr,
    String invoiceDate,
    String customerNr,
    String claimType,
    String claimNr,
    String chassisNr,
    String registrationNr,
    String repairDate,
    String mileage,
    String customerCode,
    String customerName,
    String sdeClaimNr,
    int statusCode,
    String statusText,
    String demandCode,
    String colorIndicator,
    int errorCount
) {} // @rpg-trace: n471