package com.scania.warranty.domain;

public enum ClaimStatus {
    CREATED(0),
    PENDING(2),
    SUBMITTED(3),
    APPROVED(10),
    REJECTED(11),
    MINIMUM_REQUEST(5),
    MINIMUM_POSTED(20),
    EXCLUDED(99);
    
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
        return null;
    }
}