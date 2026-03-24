/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

public enum FilterOption {
    KULANZ("K"),
    GARANTIE("G"),
    OTHER("");

    private final String code;

    FilterOption(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FilterOption fromCode(String code) {
        if ("K".equals(code)) return KULANZ;
        if ("G".equals(code)) return GARANTIE;
        return OTHER;
    }
}