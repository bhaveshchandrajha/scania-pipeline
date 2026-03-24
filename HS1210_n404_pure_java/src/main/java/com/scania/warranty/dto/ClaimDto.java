package com.scania.warranty.dto;

public record ClaimDto(
    String pakz,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String wete,
    String claimNumber,
    String chassisNumber,
    String licensePlate,
    Integer registrationDate,
    Integer repairDate,
    Integer mileage,
    Integer productType,
    String attachment,
    String foreigner,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCodeSde,
    Integer errorCount,
    String area,
    String orderNumberFull
) {
}