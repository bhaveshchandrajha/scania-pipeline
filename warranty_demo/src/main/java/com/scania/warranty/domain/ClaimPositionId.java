/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimPositionId implements Serializable {
    
    private String kuerzel; // @rpg-trace: schema
    private String claimNr; // @rpg-trace: schema
    private String fehlerNr; // @rpg-trace: schema
    private String folgeNr; // @rpg-trace: schema
    private Integer zeile; // @rpg-trace: schema
    private Integer aufteilung; // @rpg-trace: schema
    
    public ClaimPositionId() {
    }
    
    public ClaimPositionId(String kuerzel, String claimNr, String fehlerNr, String folgeNr, Integer zeile, Integer aufteilung) {
        this.kuerzel = kuerzel;
        this.claimNr = claimNr;
        this.fehlerNr = fehlerNr;
        this.folgeNr = folgeNr;
        this.zeile = zeile;
        this.aufteilung = aufteilung;
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
    
    public Integer getZeile() {
        return zeile;
    }
    
    public void setZeile(Integer zeile) {
        this.zeile = zeile;
    }
    
    public Integer getAufteilung() {
        return aufteilung;
    }
    
    public void setAufteilung(Integer aufteilung) {
        this.aufteilung = aufteilung;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimPositionId that = (ClaimPositionId) o;
        return Objects.equals(kuerzel, that.kuerzel) &&
               Objects.equals(claimNr, that.claimNr) &&
               Objects.equals(fehlerNr, that.fehlerNr) &&
               Objects.equals(folgeNr, that.folgeNr) &&
               Objects.equals(zeile, that.zeile) &&
               Objects.equals(aufteilung, that.aufteilung);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(kuerzel, claimNr, fehlerNr, folgeNr, zeile, aufteilung);
    }
}