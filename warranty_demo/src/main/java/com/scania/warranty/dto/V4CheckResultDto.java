/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.dto;

/**
 * Result DTO for the V4 check procedure.
 * Maps to the boolean return value of the RPG CheckV4 procedure.
 */
public record V4CheckResultDto(
    boolean v4Found,
    String variantCode,
    String message
) {}