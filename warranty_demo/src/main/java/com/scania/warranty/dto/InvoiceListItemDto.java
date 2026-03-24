package com.scania.warranty.dto;

/**
 * DTO for listing invoices available for claim creation.
 */
public record InvoiceListItemDto(
        String companyCode,
        String invoiceNumber,
        String invoiceDate,
        String orderNumber,
        String workshopType,
        String customerName
) {}
