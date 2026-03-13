package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link ExternalService} (HSFLALF1).
 * Field names must match the @Id property names in ExternalService.
 */
public class ExternalServiceId implements Serializable {

    private String pkz;
    private String besDat;
    private String besNr;
    private Integer lnrFl;

    public ExternalServiceId() {
    }

    public ExternalServiceId(String pkz, String besDat, String besNr, Integer lnrFl) {
        this.pkz = pkz;
        this.besDat = besDat;
        this.besNr = besNr;
        this.lnrFl = lnrFl;
    }

    public String getPkz() { return pkz; }
    public void setPkz(String pkz) { this.pkz = pkz; }
    public String getBesDat() { return besDat; }
    public void setBesDat(String besDat) { this.besDat = besDat; }
    public String getBesNr() { return besNr; }
    public void setBesNr(String besNr) { this.besNr = besNr; }
    public Integer getLnrFl() { return lnrFl; }
    public void setLnrFl(Integer lnrFl) { this.lnrFl = lnrFl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalServiceId that = (ExternalServiceId) o;
        return Objects.equals(pkz, that.pkz) && Objects.equals(besDat, that.besDat)
                && Objects.equals(besNr, that.besNr) && Objects.equals(lnrFl, that.lnrFl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkz, besDat, besNr, lnrFl);
    }
}
