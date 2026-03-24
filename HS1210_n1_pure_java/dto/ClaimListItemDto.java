package com.scania.warranty.dto;

import java.time.LocalDate;

public record ClaimListItemDto(
        String dealerId,
        String claimNumber,
        String invoiceNumber,
        LocalDate invoiceDate,
        String orderNumber,
        String vehicleNumber,
        String customerNumber,
        String customerName,
        String demandCode,
        String status,
        String statusDescription,
        Integer errorCount,
        String colorIndicator
) {
}