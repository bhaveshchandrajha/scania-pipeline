package com.scania.warranty.dto;

public record ClaimCreationRequestDto(
    String companyCode,
    String invoiceNumber,
    String invoiceDate,
    String jobNumber,
    String workshopType
) {}