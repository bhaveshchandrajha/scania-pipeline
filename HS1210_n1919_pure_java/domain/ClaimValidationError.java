package com.scania.warranty.domain;

public record ClaimValidationError(
    String message
) {
    public static ClaimValidationError of(String message) {
        return new ClaimValidationError(message);
    }
}