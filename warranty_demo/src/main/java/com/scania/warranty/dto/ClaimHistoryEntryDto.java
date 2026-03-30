package com.scania.warranty.dto;

/**
 * Single line in claim drill-down history: current status first, then error subfile rows.
 */
public record ClaimHistoryEntryDto(
    String entryType,
    String title,
    String detail,
    String reference
) {
    public static final String TYPE_STATUS = "STATUS";
    public static final String TYPE_ERROR = "ERROR";
}
