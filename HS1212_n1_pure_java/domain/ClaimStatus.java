package com.scania.warranty.domain;

public enum ClaimStatus {
    DRAFT(0, "Draft"),
    CREATED(1, "Created"),
    SUBMITTED(2, "Submitted"),
    IN_REVIEW(3, "In Review"),
    APPROVED(4, "Approved"),
    MINIMUM(5, "Minimum"),
    REJECTED(16, "Rejected"),
    SENT(20, "Sent"),
    ERROR(30, "Error"),
    EXCLUDED(99, "Excluded");

    private final int code;
    private final String description;

    ClaimStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ClaimStatus fromCode(int code) {
        for (ClaimStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return DRAFT;
    }
}