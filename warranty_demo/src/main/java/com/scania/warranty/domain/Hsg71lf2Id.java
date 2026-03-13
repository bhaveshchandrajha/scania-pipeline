/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class Hsg71lf2Id implements Serializable {

    private String pakz; // @rpg-trace: schema
    private String rechNr; // @rpg-trace: schema
    private String rechDatum; // @rpg-trace: schema
    private String auftragsNr; // @rpg-trace: schema
    private String wete; // @rpg-trace: schema
    private String claimNr; // @rpg-trace: schema

    public Hsg71lf2Id() {
    }

    public Hsg71lf2Id(String pakz, String rechNr, String rechDatum, String auftragsNr, String wete, String claimNr) {
        this.pakz = pakz;
        this.rechNr = rechNr;
        this.rechDatum = rechDatum;
        this.auftragsNr = auftragsNr;
        this.wete = wete;
        this.claimNr = claimNr;
    }

    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRechNr() { return rechNr; }
    public void setRechNr(String rechNr) { this.rechNr = rechNr; }
    public String getRechDatum() { return rechDatum; }
    public void setRechDatum(String rechDatum) { this.rechDatum = rechDatum; }
    public String getAuftragsNr() { return auftragsNr; }
    public void setAuftragsNr(String auftragsNr) { this.auftragsNr = auftragsNr; }
    public String getWete() { return wete; }
    public void setWete(String wete) { this.wete = wete; }
    public String getClaimNr() { return claimNr; }
    public void setClaimNr(String claimNr) { this.claimNr = claimNr; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hsg71lf2Id)) return false;
        Hsg71lf2Id that = (Hsg71lf2Id) o;
        return Objects.equals(pakz, that.pakz) &&
               Objects.equals(rechNr, that.rechNr) &&
               Objects.equals(rechDatum, that.rechDatum) &&
               Objects.equals(auftragsNr, that.auftragsNr) &&
               Objects.equals(wete, that.wete) &&
               Objects.equals(claimNr, that.claimNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pakz, rechNr, rechDatum, auftragsNr, wete, claimNr);
    }
}