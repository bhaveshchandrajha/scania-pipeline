package com.scania.warranty.dto;

import java.math.BigDecimal;
import java.util.List;

public record FailureCreationDto(
    Integer failureCode,
    String groups,
    String partNumber,
    boolean maintenance,
    List<String> textLines,
    BigDecimal materialValue,
    BigDecimal laborValue,
    BigDecimal specialValue
) {
}