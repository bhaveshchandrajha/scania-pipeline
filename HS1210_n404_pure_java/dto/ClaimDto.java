package com.scania.warranty.dto;

public record ClaimDto(
    String companyCode,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String workshopCounter,
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
    String jobNumber,
    String statusDescription,
    String statusColor
) {}