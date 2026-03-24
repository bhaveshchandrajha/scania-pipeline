package com.scania.warranty.domain;

public enum ProductType {
    TRUCK(1, "LKW"),
    BUS(2, "BUS"),
    ENGINE(3, "Motor"),
    TRAILER(4, "Trailer"),
    MARINE(5, "Marine");

    private final int code;
    private final String description;

    ProductType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProductType fromCode(int code) {
        for (ProductType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return TRUCK;
    }
}