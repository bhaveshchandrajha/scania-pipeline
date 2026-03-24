package com.scania.warranty.domain;

public enum PositionRecordType {
    MATERIAL("MAT"),
    LABOR("ARB"),
    TEXT("TXT"),
    OTHER("");
    
    private final String code;
    
    PositionRecordType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static PositionRecordType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        for (PositionRecordType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}