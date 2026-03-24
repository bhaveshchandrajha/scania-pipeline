/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

/**
 * DTO for claim list item display.
 */
public record ClaimListItemDto(
    String companyCode,
    String claimNumber,
    String invoiceNumber,
    String invoiceDate,
    String chassisNumber,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCodeSde,
    String statusDescription,
    Integer errorCount,
    String displayColor
) {}