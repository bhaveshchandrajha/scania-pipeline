package com.scania.warranty.dto;

public record ClaimListItemDto(
        String dealerId,
        String claimNumber,
        String invoiceNumber,
        String invoiceDate,
        String vehicleNumber,
        String customerNumber,
        String customerName,
        String demandCode,
        String status,
        String statusDescription,
        Integer errorCount,
        String color,
        String sdeDate
) {
}