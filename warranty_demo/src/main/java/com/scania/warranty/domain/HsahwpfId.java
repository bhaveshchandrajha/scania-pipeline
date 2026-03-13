/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class HsahwpfId implements Serializable {

    private String pakz; // @rpg-trace: schema
    private String rnr; // @rpg-trace: schema
    private String rdat; // @rpg-trace: schema
    private String anr; // @rpg-trace: schema
    private String berei; // @rpg-trace: schema
    private String wt; // @rpg-trace: schema
    private String splitt; // @rpg-trace: schema
    private Integer pos; // @rpg-trace: schema
    private String ec; // @rpg-trace: schema
    private Integer lnrPak; // @rpg-trace: schema
    private String paketNr; // @rpg-trace: schema
    private Integer sortRz; // @rpg-trace: schema
    private Integer lnrRz; // @rpg-trace: schema
    private String ag; // @rpg-trace: schema
    private String lNr; // @rpg-trace: schema

    public HsahwpfId() {
    }

    public HsahwpfId(String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt, Integer pos, String ec, Integer lnrPak, String paketNr, Integer sortRz, Integer lnrRz, String ag, String lNr) {
        this.pakz = pakz;
        this.rnr = rnr;
        this.rdat = rdat;
        this.anr = anr;
        this.berei = berei;
        this.wt = wt;
        this.splitt = splitt;
        this.pos = pos;
        this.ec = ec;
        this.lnrPak = lnrPak;
        this.paketNr = paketNr;
        this.sortRz = sortRz;
        this.lnrRz = lnrRz;
        this.ag = ag;
        this.lNr = lNr;
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
    public Integer getPos() { return pos; }
    public void setPos(Integer pos) { this.pos = pos; }
    public String getEc() { return ec; }
    public void setEc(String ec) { this.ec = ec; }
    public Integer getLnrPak() { return lnrPak; }
    public void setLnrPak(Integer lnrPak) { this.lnrPak = lnrPak; }
    public String getPaketNr() { return paketNr; }
    public void setPaketNr(String paketNr) { this.paketNr = paketNr; }
    public Integer getSortRz() { return sortRz; }
    public void setSortRz(Integer sortRz) { this.sortRz = sortRz; }
    public Integer getLnrRz() { return lnrRz; }
    public void setLnrRz(Integer lnrRz) { this.lnrRz = lnrRz; }
    public String getAg() { return ag; }
    public void setAg(String ag) { this.ag = ag; }
    public String getlNr() { return lNr; }
    public void setlNr(String lNr) { this.lNr = lNr; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HsahwpfId)) return false;
        HsahwpfId that = (HsahwpfId) o;
        return Objects.equals(pakz, that.pakz) &&
               Objects.equals(rnr, that.rnr) &&
               Objects.equals(rdat, that.rdat) &&
               Objects.equals(anr, that.anr) &&
               Objects.equals(berei, that.berei) &&
               Objects.equals(wt, that.wt) &&
               Objects.equals(splitt, that.splitt) &&
               Objects.equals(pos, that.pos) &&
               Objects.equals(ec, that.ec) &&
               Objects.equals(lnrPak, that.lnrPak) &&
               Objects.equals(paketNr, that.paketNr) &&
               Objects.equals(sortRz, that.sortRz) &&
               Objects.equals(lnrRz, that.lnrRz) &&
               Objects.equals(ag, that.ag) &&
               Objects.equals(lNr, that.lNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pakz, rnr, rdat, anr, berei, wt, splitt, pos, ec, lnrPak, paketNr, sortRz, lnrRz, ag, lNr);
    }
}