package com.scania.warranty.dto;

public record PositionRenumberDto(
    String dealerId,
    String claimNo,
    int positionsRenumbered,
    boolean success
) {}