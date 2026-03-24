package com.scania.warranty.domain;

public enum RecordType {
    SMA("SMA", "Special Material"),
    STANDARD("STD", "Standard Position");

    private final String code;
    private final String description;

    RecordType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RecordType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return STANDARD;
        }
        for (RecordType type : values()) {
            if (type.code.equals(code.trim())) {
                return type;
            }
        }
        return STANDARD;
    }
}