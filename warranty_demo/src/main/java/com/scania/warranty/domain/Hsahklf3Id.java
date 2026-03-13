/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class Hsahklf3Id implements Serializable {

    private String pakz; // @rpg-trace: schema
    private String rnr; // @rpg-trace: schema
    private String rdat; // @rpg-trace: schema
    private String anr; // @rpg-trace: schema
    private String berei; // @rpg-trace: schema
    private String wt; // @rpg-trace: schema
    private String splitt; // @rpg-trace: schema

    public Hsahklf3Id() {
    }

    public Hsahklf3Id(String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt) {
        this.pakz = pakz;
        this.rnr = rnr;
        this.rdat = rdat;
        this.anr = anr;
        this.berei = berei;
        this.wt = wt;
        this.splitt = splitt;
    }

    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRnr() { return rnr; }
    public void setRnr(String rnr) { this.rnr = rnr; }
    public String getRdat() { return rdat; }
    public void setRdat(String rdat) { this.rdat = rdat; }
    public String getAnr() { return anr; }
    public void setAnr(String anr) { this.anr = anr; }
    public String getBerei() { return berei; }
    public void setBerei(String berei) { this.berei = berei; }
    public String getWt() { return wt; }
    public void setWt(String wt) { this.wt = wt; }
    public String getSplitt() { return splitt; }
    public void setSplitt(String splitt) { this.splitt = splitt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hsahklf3Id)) return false;
        Hsahklf3Id that = (Hsahklf3Id) o;
        return Objects.equals(pakz, that.pakz) &&
               Objects.equals(rnr, that.rnr) &&
               Objects.equals(rdat, that.rdat) &&
               Objects.equals(anr, that.anr) &&
               Objects.equals(berei, that.berei) &&
               Objects.equals(wt, that.wt) &&
               Objects.equals(splitt, that.splitt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pakz, rnr, rdat, anr, berei, wt, splitt);
    }
}