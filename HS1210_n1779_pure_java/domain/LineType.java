package com.scania.warranty.domain;

public enum LineType {
    MATERIAL("MAT"),
    LABOR("ARB"),
    TEXT("TXT"),
    SPECIAL("SPE");
    
    private final String code;
    
    LineType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static LineType fromCode(String code) {
        for (LineType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return SPECIAL;
    }
}