/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class HsgpspfId implements Serializable {

    private String gps000;
    private String gps010;
    private String gps020;
    private BigDecimal gps030;
    private BigDecimal gps035;
    private String gps040;

    public HsgpspfId() {
    }

    public HsgpspfId(String gps000, String gps010, String gps020, BigDecimal gps030, BigDecimal gps035, String gps040) {
        this.gps000 = gps000;
        this.gps010 = gps010;
        this.gps020 = gps020;
        this.gps030 = gps030;
        this.gps035 = gps035;
        this.gps040 = gps040;
    }

    public String getGps000() {
        return gps000;
    }

    public void setGps000(String gps000) {
        this.gps000 = gps000;
    }

    public String getGps010() {
        return gps010;
    }

    public void setGps010(String gps010) {
        this.gps010 = gps010;
    }

    public String getGps020() {
        return gps020;
    }

    public void setGps020(String gps020) {
        this.gps020 = gps020;
    }

    public BigDecimal getGps030() {
        return gps030;
    }

    public void setGps030(BigDecimal gps030) {
        this.gps030 = gps030;
    }

    public BigDecimal getGps035() {
        return gps035;
    }

    public void setGps035(BigDecimal gps035) {
        this.gps035 = gps035;
    }

    public String getGps040() {
        return gps040;
    }

    public void setGps040(String gps040) {
        this.gps040 = gps040;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HsgpspfId that = (HsgpspfId) o;
        return Objects.equals(gps000, that.gps000) &&
                Objects.equals(gps010, that.gps010) &&
                Objects.equals(gps020, that.gps020) &&
                Objects.equals(gps030, that.gps030) &&
                Objects.equals(gps035, that.gps035) &&
                Objects.equals(gps040, that.gps040);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gps000, gps010, gps020, gps030, gps035, gps040);
    }
}