/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class GpsLineId implements Serializable {
    
    private String kuerzel; // @rpg-trace: schema
    private String claimNr; // @rpg-trace: schema
    private String fehlerNr; // @rpg-trace: schema
    private String folgeNr; // @rpg-trace: schema

    public GpsLineId() {
    }

    public GpsLineId(String kuerzel, String claimNr, String fehlerNr, String folgeNr) {
        this.kuerzel = kuerzel;
        this.claimNr = claimNr;
        this.fehlerNr = fehlerNr;
        this.folgeNr = folgeNr;
    }

    public String getKuerzel() {
        return kuerzel;
    }

    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
    }

    public String getClaimNr() {
        return claimNr;
    }

    public void setClaimNr(String claimNr) {
        this.claimNr = claimNr;
    }

    public String getFehlerNr() {
        return fehlerNr;
    }

    public void setFehlerNr(String fehlerNr) {
        this.fehlerNr = fehlerNr;
    }

    public String getFolgeNr() {
        return folgeNr;
    }

    public void setFolgeNr(String folgeNr) {
        this.folgeNr = folgeNr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GpsLineId gpsLineId = (GpsLineId) o;
        return Objects.equals(kuerzel, gpsLineId.kuerzel) &&
               Objects.equals(claimNr, gpsLineId.claimNr) &&
               Objects.equals(fehlerNr, gpsLineId.fehlerNr) &&
               Objects.equals(folgeNr, gpsLineId.folgeNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kuerzel, claimNr, fehlerNr, folgeNr);
    }
}