/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.dto;

public record V4CheckResultDto(
    boolean isValid,
    String message
) {
    public static V4CheckResultDto valid() {
        return new V4CheckResultDto(true, "V4 validation passed");
    }

    public static V4CheckResultDto invalid(String reason) {
        return new V4CheckResultDto(false, reason);
    }
}