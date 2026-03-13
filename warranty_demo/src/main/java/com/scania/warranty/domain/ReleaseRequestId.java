package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for ReleaseRequest (kzl, rnr, rdat).
 */
public class ReleaseRequestId implements Serializable {

    private String kzl;
    private String rnr;
    private String rdat;

    public ReleaseRequestId() {
    }

    public ReleaseRequestId(String kzl, String rnr, String rdat) {
        this.kzl = kzl;
        this.rnr = rnr;
        this.rdat = rdat;
    }

    public String getKzl() {
        return kzl;
    }

    public void setKzl(String kzl) {
        this.kzl = kzl;
    }

    public String getRnr() {
        return rnr;
    }

    public void setRnr(String rnr) {
        this.rnr = rnr;
    }

    public String getRdat() {
        return rdat;
    }

    public void setRdat(String rdat) {
        this.rdat = rdat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReleaseRequestId that = (ReleaseRequestId) o;
        return Objects.equals(kzl, that.kzl)
                && Objects.equals(rnr, that.rnr)
                && Objects.equals(rdat, that.rdat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kzl, rnr, rdat);
    }
}
