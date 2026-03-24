/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

import java.util.List;

/**
 * DTO for claim detail view.
 */
public record ClaimDetailDto(
    String companyCode,
    String claimNumber,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String workshopTheke,
    String chassisNumber,
    String licensePlate,
    Integer registrationDate,
    Integer repairDate,
    Integer mileage,
    Integer productType,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCodeSde,
    String statusDescription,
    Integer errorCount,
    List<ClaimErrorDto> errors
) {}