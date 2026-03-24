package com.scania.warranty.domain;

import java.math.BigDecimal;
import java.util.List;

public record FailureCreationRequest(
    String companyCode,
    String claimNumber,
    String claimSequence,
    String invoiceNumber,
    String invoiceSequence,
    String claimLineNumber,
    String approvalReleaseNumber,
    String defaultPartNumber,
    String defaultServiceCode,
    String defaultControlCode,
    String btsCode,
    Integer failureCode,
    String groups,
    String partNumber,
    boolean maintenance,
    List<String> textLines,
    BigDecimal materialValue,
    BigDecimal laborValue,
    BigDecimal specialValue
) {
    public FailureCreationRequest {
        if (textLines == null || textLines.size() != 4) {
            throw new IllegalArgumentException("textLines must contain exactly 4 elements");
        }
    }
}