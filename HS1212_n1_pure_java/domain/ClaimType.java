package com.scania.warranty.domain;

public enum ClaimType {
    ORIGINAL_ASSEMBLY("1", "Original Mont."),
    ORIGINAL_PART("2", "Orig. teil mont."),
    ORIGINAL_STOCK("4", "Orig. ab Lager"),
    IRM_CONTRACT("6", "IRM Vertrag"),
    SP_MAINTENANCE("8", "SP. Maintenance"),
    EPC("9", "EPC");

    private final String code;
    private final String description;

    ClaimType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ClaimType fromCode(String code) {
        for (ClaimType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return ORIGINAL_ASSEMBLY;
    }
}