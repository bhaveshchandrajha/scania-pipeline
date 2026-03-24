/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.domain;

import java.util.List;

public record ClaimCheckResult(
    boolean valid,
    List<String> errors,
    String updatedG73360
) {
}