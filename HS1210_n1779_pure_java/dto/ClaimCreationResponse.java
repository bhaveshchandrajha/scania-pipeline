package com.scania.warranty.dto;

public record ClaimCreationResponse(
        boolean success,
        String message,
        String claimNumber
) {
}