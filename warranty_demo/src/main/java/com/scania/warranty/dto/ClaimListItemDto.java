/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimListItemDto(
    String companyCode,
    String invoiceNumber,
    String invoiceDate,
    String formattedInvoiceDate,
    String orderNumber,
    String claimNumber,
    String chassisNumber,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCodeSde,
    String statusDescription,
    Integer errorCount,
    String demandCode,
    String colorIndicator
) {
}