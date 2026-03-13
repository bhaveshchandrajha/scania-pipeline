/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class Hsflalf1Id implements Serializable {

    private String pkz; // @rpg-trace: schema
    private String besDat; // @rpg-trace: schema
    private String besNr; // @rpg-trace: schema
    private Integer lnrFl; // @rpg-trace: schema

    public Hsflalf1Id() {
    }

    public Hsflalf1Id(String pkz, String besDat, String besNr, Integer lnrFl) {
        this.pkz = pkz;
        this.besDat = besDat;
        this.besNr = besNr;
        this.lnrFl = lnrFl;
    }

    public String getPkz() {
        return pkz;
    }

    public void setPkz(String pkz) {
        this.pkz = pkz;
    }

    public String getBesDat() {
        return besDat;
    }

    public void setBesDat(String besDat) {
        this.besDat = besDat;
    }

    public String getBesNr() {
        return besNr;
    }

    public void setBesNr(String besNr) {
        this.besNr = besNr;
    }

    public Integer getLnrFl() {
        return lnrFl;
    }

    public void setLnrFl(Integer lnrFl) {
        this.lnrFl = lnrFl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hsflalf1Id)) return false;
        Hsflalf1Id that = (Hsflalf1Id) o;
        return Objects.equals(pkz, that.pkz) &&
               Objects.equals(besDat, that.besDat) &&
               Objects.equals(besNr, that.besNr) &&
               Objects.equals(lnrFl, that.lnrFl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkz, besDat, besNr, lnrFl);
    }
}