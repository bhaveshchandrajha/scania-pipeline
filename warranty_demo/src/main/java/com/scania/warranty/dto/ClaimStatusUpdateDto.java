/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimStatusUpdateDto(
    String companyCode,
    String claimNr,
    int newStatus
) {} // @rpg-trace: n654