/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimSubfileDto(
    String companyCode,
    String claimNumber,
    String invoiceNumber,
    String invoiceDate,
    String chassisNumber,
    String customerNumber,
    String customerName,
    String demandCode,
    Integer statusCode,
    String statusDescription,
    Integer errorCount,
    String colorIndicator
) {
}