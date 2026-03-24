/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

import java.math.BigDecimal;

/**
 * DTO for claim error/failure.
 */
public record ClaimErrorDto(
    String errorNumber,
    String sequenceNumber,
    String errorPart,
    String mainGroup,
    String subGroup,
    String text1,
    String text2,
    BigDecimal requestedMaterial,
    BigDecimal requestedLabor,
    BigDecimal requestedSpecial,
    Integer statusCode,
    String campaignNumber
) {}