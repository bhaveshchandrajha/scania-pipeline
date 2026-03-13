/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class Hsg70fId implements Serializable {

    private String kzl; // @rpg-trace: schema
    private String rNr; // @rpg-trace: schema
    private String rDat; // @rpg-trace: schema

    public Hsg70fId() {
    }

    public Hsg70fId(String kzl, String rNr, String rDat) {
        this.kzl = kzl;
        this.rNr = rNr;
        this.rDat = rDat;
    }

    public String getKzl() { return kzl; }
    public void setKzl(String kzl) { this.kzl = kzl; }
    public String getrNr() { return rNr; }
    public void setrNr(String rNr) { this.rNr = rNr; }
    public String getrDat() { return rDat; }
    public void setrDat(String rDat) { this.rDat = rDat; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hsg70fId)) return false;
        Hsg70fId that = (Hsg70fId) o;
        return Objects.equals(kzl, that.kzl) &&
               Objects.equals(rNr, that.rNr) &&
               Objects.equals(rDat, that.rDat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kzl, rNr, rDat);
    }
}