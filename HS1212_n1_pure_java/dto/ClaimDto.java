package com.scania.warranty.dto;

import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.domain.ClaimType;
import com.scania.warranty.domain.ProductType;

import java.time.LocalDate;
import java.util.List;

public record ClaimDto(
    String claimNumber,
    String dealerId,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String wete,
    String chassisNumber,
    String registrationNumber,
    Integer registrationDate,
    Integer repairDate,
    Integer mileage,
    Integer productType,
    String productTypeDescription,
    String attachment,
    String foreigner,
    String customerNumber,
    String customerName,
    String sdeClaimNumber,
    Integer sdeStatusCode,
    String statusDescription,
    Integer errorCount,
    String area,
    String workOrderNumber,
    List<ClaimErrorDto> errors
) {
    public static ClaimDto fromEntity(com.scania.warranty.domain.Claim claim) {
        return new ClaimDto(
            claim.getClaimNumber(),
            claim.getDealerId(),
            claim.getInvoiceNumber(),
            claim.getInvoiceDate(),
            claim.getOrderNumber(),
            claim.getWete(),
            claim.getChassisNumber(),
            claim.getRegistrationNumber(),
            claim.getRegistrationDate(),
            claim.getRepairDate(),
            claim.getMileage(),
            claim.getProductType(),
            ProductType.fromCode(claim.getProductType()).getDescription(),
            claim.getAttachment(),
            claim.getForeigner(),
            claim.getCustomerNumber(),
            claim.getCustomerName(),
            claim.getSdeClaimNumber(),
            claim.getSdeStatusCode(),
            ClaimStatus.fromCode(claim.getSdeStatusCode()).getDescription(),
            claim.getErrorCount(),
            claim.getArea(),
            claim.getWorkOrderNumber(),
            claim.getErrors().stream()
                .map(ClaimErrorDto::fromEntity)
                .toList()
        );
    }
}