/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimFailureId implements Serializable {
    
    private String pakz; // @rpg-trace: schema
    private String rechNr; // @rpg-trace: schema
    private String rechDatum; // @rpg-trace: schema
    private String auftragsNr; // @rpg-trace: schema
    private String bereich; // @rpg-trace: schema
    private String claimNr; // @rpg-trace: schema
    private String fehlerNr; // @rpg-trace: schema
    
    public ClaimFailureId() {
    }
    
    public ClaimFailureId(String pakz, String rechNr, String rechDatum, String auftragsNr, String bereich, String claimNr, String fehlerNr) {
        this.pakz = pakz;
        this.rechNr = rechNr;
        this.rechDatum = rechDatum;
        this.auftragsNr = auftragsNr;
        this.bereich = bereich;
        this.claimNr = claimNr;
        this.fehlerNr = fehlerNr;
    }
    
    public String getPakz() {
        return pakz;
    }
    
    public void setPakz(String pakz) {
        this.pakz = pakz;
    }
    
    public String getRechNr() {
        return rechNr;
    }
    
    public void setRechNr(String rechNr) {
        this.rechNr = rechNr;
    }
    
    public String getRechDatum() {
        return rechDatum;
    }
    
    public void setRechDatum(String rechDatum) {
        this.rechDatum = rechDatum;
    }
    
    public String getAuftragsNr() {
        return auftragsNr;
    }
    
    public void setAuftragsNr(String auftragsNr) {
        this.auftragsNr = auftragsNr;
    }
    
    public String getBereich() {
        return bereich;
    }
    
    public void setBereich(String bereich) {
        this.bereich = bereich;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimFailureId that = (ClaimFailureId) o;
        return Objects.equals(pakz, that.pakz) &&
               Objects.equals(rechNr, that.rechNr) &&
               Objects.equals(rechDatum, that.rechDatum) &&
               Objects.equals(auftragsNr, that.auftragsNr) &&
               Objects.equals(bereich, that.bereich) &&
               Objects.equals(claimNr, that.claimNr) &&
               Objects.equals(fehlerNr, that.fehlerNr);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pakz, rechNr, rechDatum, auftragsNr, bereich, claimNr, fehlerNr);
    }
}