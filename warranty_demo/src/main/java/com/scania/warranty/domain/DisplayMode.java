/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

public enum DisplayMode {
    DISPLAY("A"),
    PRINT("D"),
    SUMMARY("Z"),
    COPY("C");

    private final String code;

    DisplayMode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}