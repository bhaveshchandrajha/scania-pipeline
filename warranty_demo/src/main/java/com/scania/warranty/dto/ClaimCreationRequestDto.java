/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

/**
 * DTO for claim creation request.
 */
public record ClaimCreationRequestDto(
        String companyCode,
        String invoiceNumber,
        String invoiceDate,
        String orderNumber,
        String area
) {
}