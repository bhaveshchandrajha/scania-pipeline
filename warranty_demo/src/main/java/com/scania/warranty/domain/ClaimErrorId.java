package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link ClaimError} (HSG73PF).
 * Field names must match the @Id property names in ClaimError.
 */
public class ClaimErrorId implements Serializable {

    private String pakz;
    private String rechNr;
    private String rechDatum;
    private String auftragsNr;
    private String bereich;
    private String claimNr;
    private String fehlerNr;
    private String folgeNr;

    public ClaimErrorId() {
    }

    public ClaimErrorId(String pakz, String rechNr, String rechDatum, String auftragsNr, String bereich, String claimNr, String fehlerNr, String folgeNr) {
        this.pakz = pakz;
        this.rechNr = rechNr;
        this.rechDatum = rechDatum;
        this.auftragsNr = auftragsNr;
        this.bereich = bereich;
        this.claimNr = claimNr;
        this.fehlerNr = fehlerNr;
        this.folgeNr = folgeNr;
    }

    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRechNr() { return rechNr; }
    public void setRechNr(String rechNr) { this.rechNr = rechNr; }
    public String getRechDatum() { return rechDatum; }
    public void setRechDatum(String rechDatum) { this.rechDatum = rechDatum; }
    public String getAuftragsNr() { return auftragsNr; }
    public void setAuftragsNr(String auftragsNr) { this.auftragsNr = auftragsNr; }
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    public String getClaimNr() { return claimNr; }
    public void setClaimNr(String claimNr) { this.claimNr = claimNr; }
    public String getFehlerNr() { return fehlerNr; }
    public void setFehlerNr(String fehlerNr) { this.fehlerNr = fehlerNr; }
    public String getFolgeNr() { return folgeNr; }
    public void setFolgeNr(String folgeNr) { this.folgeNr = folgeNr; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimErrorId that = (ClaimErrorId) o;
        return Objects.equals(pakz, that.pakz) && Objects.equals(rechNr, that.rechNr)
                && Objects.equals(rechDatum, that.rechDatum) && Objects.equals(auftragsNr, that.auftragsNr)
                && Objects.equals(bereich, that.bereich) && Objects.equals(claimNr, that.claimNr)
                && Objects.equals(fehlerNr, that.fehlerNr) && Objects.equals(folgeNr, that.folgeNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pakz, rechNr, rechDatum, auftragsNr, bereich, claimNr, fehlerNr, folgeNr);
    }
}
