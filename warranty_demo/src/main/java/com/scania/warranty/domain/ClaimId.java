/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimId implements Serializable {
    
    private String pakz; // @rpg-trace: schema
    private String rechNr; // @rpg-trace: schema
    private String rechDatum; // @rpg-trace: schema
    private String auftragsNr; // @rpg-trace: schema
    private String bereich; // @rpg-trace: schema
    
    public ClaimId() {
    }
    
    public ClaimId(String pakz, String rechNr, String rechDatum, String auftragsNr, String bereich) {
        this.pakz = pakz;
        this.rechNr = rechNr;
        this.rechDatum = rechDatum;
        this.auftragsNr = auftragsNr;
        this.bereich = bereich;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimId claimId = (ClaimId) o;
        return Objects.equals(pakz, claimId.pakz) &&
               Objects.equals(rechNr, claimId.rechNr) &&
               Objects.equals(rechDatum, claimId.rechDatum) &&
               Objects.equals(auftragsNr, claimId.auftragsNr) &&
               Objects.equals(bereich, claimId.bereich);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pakz, rechNr, rechDatum, auftragsNr, bereich);
    }
}