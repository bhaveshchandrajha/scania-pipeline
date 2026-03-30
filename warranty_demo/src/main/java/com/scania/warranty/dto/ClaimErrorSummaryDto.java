package com.scania.warranty.dto;

/**
 * One row of claim error subfile (HSG73PF) for detail / history view.
 */
public record ClaimErrorSummaryDto(
    String errorNr,
    String sequenceNr,
    String description,
    String demandCode,
    int processingStatus
) {}
