package com.scania.warranty.dto;

public record ClaimListItemDto(
    String companyCode,
    String invoiceNumber,
    String invoiceDate,
    String jobNumber,
    String workshopType,
    String claimNumber,
    String chassisNumber,
    String licensePlate,
    String repairDate,
    Integer mileage,
    String customerNumber,
    String customerName,
    String claimNumberSde,
    Integer statusCode,
    String statusDescription,
    Integer numberOfFailures,
    String colorIndicator
) {}