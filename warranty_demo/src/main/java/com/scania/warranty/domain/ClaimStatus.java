package com.scania.warranty.domain;

public enum ClaimStatus {
    CREATED(0, "Created"),
    PENDING(2, "Pending"),
    SUBMITTED(3, "Submitted"),
    MINIMUM_REQUEST(5, "Minimum Request"),
    APPROVED(10, "Approved"),
    REJECTED(11, "Rejected"),
    ERROR(16, "Error"),
    MINIMUM_POSTED(20, "Minimum Posted"),
    PENDING_MANUAL(30, "Pending Manual Processing"),
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