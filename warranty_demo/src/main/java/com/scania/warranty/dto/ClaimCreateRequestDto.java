/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimCreateRequestDto(
    String companyCode,
    String invoiceNumber,
    String invoiceDate,
    String branchCode,
    String workshopType
) {
}