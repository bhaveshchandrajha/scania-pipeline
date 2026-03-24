/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

public record CheckV4Result(boolean v4Found) {
    public static CheckV4Result found() {
        return new CheckV4Result(true);
    }

    public static CheckV4Result notFound() {
        return new CheckV4Result(false);
    }
}