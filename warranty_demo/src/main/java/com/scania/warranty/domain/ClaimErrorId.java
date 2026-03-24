/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimErrorId implements Serializable {
    private String g73000;
    private String g73010;
    private String g73020;
    private String g73030;
    private String g73040;
    private String g73050;
    private String g73060;

    public ClaimErrorId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimErrorId that = (ClaimErrorId) o;
        return Objects.equals(g73000, that.g73000) && Objects.equals(g73010, that.g73010) &&
               Objects.equals(g73020, that.g73020) && Objects.equals(g73030, that.g73030) &&
               Objects.equals(g73040, that.g73040) && Objects.equals(g73050, that.g73050) &&
               Objects.equals(g73060, that.g73060);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g73000, g73010, g73020, g73030, g73040, g73050, g73060);
    }
}