/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.dto;

/**
 * DTO representing the result of the CheckV4 operation.
 */
public record CheckV4ResultDto(
    boolean v4AgreementFound
) {}