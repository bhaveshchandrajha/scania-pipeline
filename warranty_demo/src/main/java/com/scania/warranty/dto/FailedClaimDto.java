package com.scania.warranty.dto;

import java.time.Instant;

public record FailedClaimDto(
    Long id,
    String companyCode,
    String invoiceNr,
    String invoiceDate,
    String workshopCode,
    String failureReason,
    Integer repairAgeDays,
    Instant failedAt
) {}
