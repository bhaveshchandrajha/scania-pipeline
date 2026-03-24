package com.scania.warranty.dto;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimStatus;

public record ClaimListItemDto(
    String claimNumber,
    String dealerId,
    String invoiceNumber,
    String invoiceDate,
    String chassisNumber,
    Integer repairDate,
    Integer mileage,
    Integer sdeStatusCode,
    String statusDescription,
    Integer errorCount
) {
    public static ClaimListItemDto fromEntity(Claim claim) {
        return new ClaimListItemDto(
            claim.getClaimNumber(),
            claim.getDealerId(),
            claim.getInvoiceNumber(),
            claim.getInvoiceDate(),
            claim.getChassisNumber(),
            claim.getRepairDate(),
            claim.getMileage(),
            claim.getSdeStatusCode(),
            ClaimStatus.fromCode(claim.getSdeStatusCode()).getDescription(),
            claim.getErrorCount()
        );
    }
}