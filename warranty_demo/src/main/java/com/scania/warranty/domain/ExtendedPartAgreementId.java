/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ExtendedPartAgreementId implements Serializable {

    private String epa000;
    private String epa040;
    private String epa050;
    private String epa060;
    private String epaType;

    public ExtendedPartAgreementId() {}

    public ExtendedPartAgreementId(String epa000, String epa040, String epa050,
                                    String epa060, String epaType) {
        this.epa000 = epa000;
        this.epa040 = epa040;
        this.epa050 = epa050;
        this.epa060 = epa060;
        this.epaType = epaType;
    }

    public String getEpa000() { return epa000; }
    public void setEpa000(String epa000) { this.epa000 = epa000; }

    public String getEpa040() { return epa040; }
    public void setEpa040(String epa040) { this.epa040 = epa040; }

    public String getEpa050() { return epa050; }
    public void setEpa050(String epa050) { this.epa050 = epa050; }

    public String getEpa060() { return epa060; }
    public void setEpa060(String epa060) { this.epa060 = epa060; }

    public String getEpaType() { return epaType; }
    public void setEpaType(String epaType) { this.epaType = epaType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedPartAgreementId that = (ExtendedPartAgreementId) o;
        return Objects.equals(epa000, that.epa000) &&
               Objects.equals(epa040, that.epa040) &&
               Objects.equals(epa050, that.epa050) &&
               Objects.equals(epa060, that.epa060) &&
               Objects.equals(epaType, that.epaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epa000, epa040, epa050, epa060, epaType);
    }
}