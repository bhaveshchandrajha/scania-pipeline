/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.dto;

import java.util.List;

public record CheckClaimResultDto(
    boolean valid,
    List<String> errors,
    String updatedG73360
) {}