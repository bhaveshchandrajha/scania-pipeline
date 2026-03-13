package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link Invoice} (HSAHKLF3).
 * Field names must match the @Id property names in Invoice.
 */
public class InvoiceId implements Serializable {

    private String pakz;
    private String rnr;
    private String rdat;
    private String anr;
    private String berei;
    private String wt;
    private String splitt;

    public InvoiceId() {
    }

    public InvoiceId(String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt) {
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
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceId that = (InvoiceId) o;
        return Objects.equals(pakz, that.pakz) && Objects.equals(rnr, that.rnr)
                && Objects.equals(rdat, that.rdat) && Objects.equals(anr, that.anr)
                && Objects.equals(berei, that.berei) && Objects.equals(wt, that.wt)
                && Objects.equals(splitt, that.splitt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pakz, rnr, rdat, anr, berei, wt, splitt);
    }
}
