package com.scania.warranty.domain;

public enum PositionType {
    MATERIAL("MAT"),
    LABOR("ARB"),
    TEXT("TXT"),
    SPECIAL("SPE");

    private final String code;

    PositionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PositionType fromCode(String code) {
        for (PositionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return SPECIAL;
    }
}