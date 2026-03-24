/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class LaborId implements Serializable {
    private String ahw000;
    private String ahw010;
    private String ahw020;
    private String ahw030;
    private String ahw040;
    private String ahw050;
    private String ahw060;
    private String ahw070;
    private String ahw080;
    private BigDecimal ahw085;
    private String ahw090;
    private BigDecimal ahw093;
    private BigDecimal ahw095;
    private String ahw100;
    private String ahw110;

    public LaborId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaborId that = (LaborId) o;
        return Objects.equals(ahw000, that.ahw000) && Objects.equals(ahw010, that.ahw010) &&
               Objects.equals(ahw020, that.ahw020) && Objects.equals(ahw030, that.ahw030) &&
               Objects.equals(ahw040, that.ahw040) && Objects.equals(ahw050, that.ahw050) &&
               Objects.equals(ahw060, that.ahw060) && Objects.equals(ahw070, that.ahw070) &&
               Objects.equals(ahw080, that.ahw080) && Objects.equals(ahw085, that.ahw085) &&
               Objects.equals(ahw090, that.ahw090) && Objects.equals(ahw093, that.ahw093) &&
               Objects.equals(ahw095, that.ahw095) && Objects.equals(ahw100, that.ahw100) &&
               Objects.equals(ahw110, that.ahw110);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ahw000, ahw010, ahw020, ahw030, ahw040, ahw050, ahw060, ahw070,
                           ahw080, ahw085, ahw090, ahw093, ahw095, ahw100, ahw110);
    }
}