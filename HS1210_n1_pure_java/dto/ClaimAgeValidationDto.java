package com.scania.warranty.dto;

import java.time.LocalDate;

public record ClaimAgeValidationDto(
    LocalDate repairDate,
    LocalDate currentDate,
    long daysDifference,
    int maxAllowedDays,
    boolean isValid
) {}