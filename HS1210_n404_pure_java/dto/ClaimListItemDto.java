package com.scania.warranty.dto;

public record ClaimListItemDto(
    String claimNumber,
    String invoiceNumber,
    String invoiceDate,
    String chassisNumber,
    String customerNumber,
    String customerName,
    String demandCode,
    Integer statusCodeSde,
    Integer errorCount,
    String statusColor
) {}