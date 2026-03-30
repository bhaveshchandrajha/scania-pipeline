package com.scania.warranty.dto;

import java.util.List;

/**
 * Claim header plus ordered history (current status first, then error subfile / HSG73).
 */
public record ClaimDetailDto(
    ClaimListItemDto claim,
    List<ClaimHistoryEntryDto> history,
    List<ClaimErrorSummaryDto> errors
) {}
