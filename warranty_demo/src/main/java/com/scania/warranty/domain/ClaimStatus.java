/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

public enum ClaimStatus {
    EXCLUDED(99),
    APPROVED(20),
    REJECTED(11),
    PENDING(0),
    MINIMUM(5);

    private final int code;

    ClaimStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ClaimStatus fromCode(int code) { // @rpg-trace: n408
        for (ClaimStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}