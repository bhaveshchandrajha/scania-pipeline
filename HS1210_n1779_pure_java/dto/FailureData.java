package com.scania.warranty.dto;

import java.math.BigDecimal;
import java.util.List;

public record FailureData(
    Integer failureNumber,
    String groups,
    String partNumber,
    boolean maintenance,
    List<String> textLines,
    BigDecimal valueMaterial,
    BigDecimal valueLabor,
    BigDecimal valueSpecial
) {
}