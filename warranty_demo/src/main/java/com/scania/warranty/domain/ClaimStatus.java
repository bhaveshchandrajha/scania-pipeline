/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

public enum ClaimStatus {
    PENDING(0),
    MINIMUM(5),
    REJECTED(11),
    APPROVED(20),
    EXCLUDED(99);

    private final int code;

    ClaimStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ClaimStatus fromCode(int code) {
        for (ClaimStatus s : values()) {
            if (s.code == code) return s;
        }
        return PENDING;
    }
}