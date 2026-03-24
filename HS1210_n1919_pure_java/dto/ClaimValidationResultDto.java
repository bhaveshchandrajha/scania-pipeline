package com.scania.warranty.dto;

import com.scania.warranty.domain.ClaimValidationError;

import java.util.List;

public record ClaimValidationResultDto(
    boolean valid,
    List<ClaimValidationError> errors
) {
    public String getErrorText() {
        if (errors.isEmpty()) {
            return "";
        }

        StringBuilder errorText = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                errorText.append(" ");
            }
            errorText.append(errors.get(i).message());
        }
        return errorText.toString();
    }

    public int getErrorCount() {
        return errors.size();
    }
}