package com.scania.warranty.dto;

import java.math.BigDecimal;

public record WarrantyClaimErrorDto(
    String companyCode,
    String claimNumber,
    String claimSequence,
    String invoiceNumber,
    String invoiceSequence,
    String claimLineNumber,
    String failureCode,
    String partNumber,
    String mainGroup,
    String subGroup,
    String serviceCode,
    String textLine1,
    String textLine2,
    String controlCode,
    BigDecimal materialCostPercentage,
    BigDecimal laborCostPercentage,
    BigDecimal specialCostPercentage,
    BigDecimal materialValue,
    BigDecimal laborValue,
    BigDecimal specialValue,
    Integer quantity,
    Integer statusFlag,
    String textLine3,
    String textLine4
) {
}