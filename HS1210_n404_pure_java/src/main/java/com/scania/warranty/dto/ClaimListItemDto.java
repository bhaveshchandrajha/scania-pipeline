package com.scania.warranty.dto;

public record ClaimListItemDto(
    String pakz,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String claimNumber,
    String chassisNumber,
    String licensePlate,
    String repairDate,
    Integer mileage,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCode,
    String statusText,
    Integer errorCount,
    String colorIndicator
) {
}