package com.scania.warranty.domain;

public enum ClaimStatus {
    DRAFT(0),
    IN_PROGRESS(1),
    READY_FOR_SUBMISSION(2),
    SUBMITTED(3),
    APPROVED(4),
    REJECTED(5);

    private final int code;

    ClaimStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ClaimStatus fromCode(int code) {
        for (ClaimStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown claim status code: " + code);
    }
}