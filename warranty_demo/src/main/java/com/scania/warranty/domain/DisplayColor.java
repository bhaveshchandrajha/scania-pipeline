/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

/**
 * Enum for display color codes.
 */
public enum DisplayColor {
    NONE(0),
    RED(1),
    YELLOW(2),
    BLUE(3);

    private final int code;

    DisplayColor(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}