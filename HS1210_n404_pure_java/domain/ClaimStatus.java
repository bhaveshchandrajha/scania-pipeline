package com.scania.warranty.domain;

public enum ClaimStatus {
    CREATED(0, "Created"),
    MINIMUM_REQUEST(5, "Minimum Request"),
    PENDING(2, "Pending"),
    SUBMITTED(3, "Submitted"),
    APPROVED(10, "Approved"),
    REJECTED(11, "Rejected"),
    ERROR(16, "Error"),
    MINIMUM_POSTED(20, "Minimum Posted"),
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
        return null;
    }
}