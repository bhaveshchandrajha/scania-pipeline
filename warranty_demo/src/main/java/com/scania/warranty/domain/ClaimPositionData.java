/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n2020}.
 */

package com.scania.warranty.domain;

/**
 * Value object for aggregated claim position data.
 */
public record ClaimPositionData(
    Integer lineNo,
    Integer position
) {
}