package com.scania.warranty.dto;

public record ClaimCreationRequest(
        String companyCode,
        String invoiceNumber,
        String invoiceDate,
        String claimNumber
) {
}