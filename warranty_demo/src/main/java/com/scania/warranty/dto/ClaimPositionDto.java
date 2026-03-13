/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n2020}.
 */

package com.scania.warranty.dto;

/**
 * Response DTO for claim position.
 */
public record ClaimPositionDto(
    String dealerId,
    String claimNo,
    Integer lineNo,
    Integer position
) {
}