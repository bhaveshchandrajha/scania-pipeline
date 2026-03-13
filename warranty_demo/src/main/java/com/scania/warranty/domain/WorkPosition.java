/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * JPA entity for work position line items (HSAHWPF).
 */
@Entity
@Table(name = "HSAHWPF")
public class WorkPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PAKZ", length = 3, nullable = false)
    private String pakz; // @rpg-trace: schema

    @Column(name = "RNR", length = 5, nullable = false)
    private String rnr; // @rpg-trace: schema

    @Column(name = "RG-NR. 10A", length = 10, nullable = false)
    private String rgNr10a; // @rpg-trace: schema

    @Column(name = "RDAT", length = 8, nullable = false)
    private String rdat; // @rpg-trace: schema

    @Column(name = "KZ S", length = 1, nullable = false)
    private String kzS; // @rpg-trace: schema

    @Column(name = "ANR", length = 5, nullable = false)
    private String anr; // @rpg-trace: schema

    @Column(name = "BEREI", length = 1, nullable = false)
    private String berei; // @rpg-trace: schema

    @Column(name = "W/T", length = 1, nullable = false)
    private String wt; // @rpg-trace: schema

    @Column(name = "SPLITT", length = 2, nullable = false)
    private String splitt; // @rpg-trace: schema

    @Column(name = "POS.", precision = 3, scale = 0, nullable = false)
    private Integer pos; // @rpg-trace: schema

    @Column(name = "EC", length = 2, nullable = false)
    private String ec; // @rpg-trace: schema

    @Column(name = "LNR PAK", precision = 3, scale = 0, nullable = false)
    private Integer lnrPak; // @rpg-trace: schema

    @Column(name = "PAKET-NR.", length = 8, nullable = false)
    private String paketNr; // @rpg-trace: schema

    @Column(name = "SORT RZ", precision = 3, scale = 0, nullable = false)
    private Integer sortRz; // @rpg-trace: schema

    @Column(name = "LNR RZ", precision = 3, scale = 0, nullable = false)
    private Integer lnrRz; // @rpg-trace: schema

    @Column(name = "AG", length = 8, nullable = false)
    private String ag; // @rpg-trace: schema

    @Column(name = "L.NR.", length = 3, nullable = false)
    private String lnr; // @rpg-trace: schema

    @Column(name = "BEZ.", length = 40, nullable = false)
    private String bez; // @rpg-trace: schema

    @Column(name = "WERKSZEIT", precision = 5, scale = 2, nullable = false)
    private BigDecimal werkszeit; // @rpg-trace: schema

    @Column(name = "AW-STUNDEN", precision = 5, scale = 2, nullable = false)
    private BigDecimal awStunden; // @rpg-trace: schema

    @Column(name = "ZE", precision = 5, scale = 0, nullable = false)
    private Integer ze; // @rpg-trace: schema

    @Column(name = "PE", precision = 5, scale = 0, nullable = false)
    private Integer pe; // @rpg-trace: schema

    @Column(name = "SATZ-PE", precision = 5, scale = 2, nullable = false)
    private BigDecimal satzPe; // @rpg-trace: schema

    @Column(name = "GEW-ZE", length = 1, nullable = false)
    private String gewZe; // @rpg-trace: schema

    @Column(name = "PREIS", precision = 9, scale = 2, nullable = false)
    private BigDecimal preis; // @rpg-trace: schema

    @Column(name = "MONTEUR", length = 3, nullable = false)
    private String monteur; // @rpg-trace: schema

    @Column(name = "BC", length = 2, nullable = false)
    private String bc; // @rpg-trace: schema

    @Column(name = "V-SATZ", precision = 5, scale = 2, nullable = false)
    private BigDecimal vSatz; // @rpg-trace: schema

    @Column(name = "M-STUNDEN", precision = 5, scale = 2, nullable = false)
    private BigDecimal mStunden; // @rpg-trace: schema

    @Column(name = "V-DM-NETTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal vDmNetto; // @rpg-trace: schema

    @Column(name = "V-DM BRUTTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal vDmBrutto; // @rpg-trace: schema

    @Column(name = "V-STUNDEN", precision = 9, scale = 2, nullable = false)
    private BigDecimal vStunden; // @rpg-trace: schema

    @Column(name = "ZUSCHLAG", precision = 5, scale = 2, nullable = false)
    private BigDecimal zuschlag; // @rpg-trace: schema

    @Column(name = "RABATT", precision = 5, scale = 2, nullable = false)
    private BigDecimal rabatt; // @rpg-trace: schema

    @Column(name = "KZ S/AW", length = 1, nullable = false)
    private String kzSAw; // @rpg-trace: schema

    @Column(name = "KZ-MWST", length = 1, nullable = false)
    private String kzMwst; // @rpg-trace: schema

    @Column(name = "VERDICHTEN", length = 1, nullable = false)
    private String verdichten; // @rpg-trace: schema

    @Column(name = "TXT-KEY", length = 3, nullable = false)
    private String txtKey; // @rpg-trace: schema

    @Column(name = "RG BRUTTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgBrutto; // @rpg-trace: schema

    @Column(name = "RG RABATT", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgRabatt; // @rpg-trace: schema

    @Column(name = "RG NETTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgNetto; // @rpg-trace: schema

    @Column(name = "KEN.RE2SUM", length = 1, nullable = false)
    private String kenRe2sum; // @rpg-trace: schema

    @Column(name = "URSPR-FAK/H MON", precision = 5, scale = 2, nullable = false)
    private BigDecimal ursprFakHMon; // @rpg-trace: schema

    @Column(name = "URSPR-NETTO MON", precision = 9, scale = 2, nullable = false)
    private BigDecimal ursprNettoMon; // @rpg-trace: schema

    @Column(name = "EINSTANDSPREIS", precision = 9, scale = 2, nullable = false)
    private BigDecimal einstandspreis; // @rpg-trace: schema

    @Column(name = "EPS NAME", length = 20, nullable = false)
    private String epsName; // @rpg-trace: schema

    @Column(name = "EPS MINDERUNG %", precision = 5, scale = 2, nullable = false)
    private BigDecimal epsMinderungPercent; // @rpg-trace: schema

    @Column(name = "VARIANTE", length = 500, nullable = false)
    private String variante; // @rpg-trace: schema

    @Column(name = "ARBEITSBESCHREIBUNG", length = 2000, nullable = false)
    private String arbeitsbeschreibung; // @rpg-trace: schema

    @Column(name = "RECHNUNGSTEXT", length = 2000, nullable = false)
    private String rechnungstext; // @rpg-trace: schema

    public WorkPosition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPakz() {
        return pakz;
    }

    public void setPakz(String pakz) {
        this.pakz = pakz;
    }

    public String getRnr() {
        return rnr;
    }

    public void setRnr(String rnr) {
        this.rnr = rnr;
    }

    public String getRgNr10a() {
        return rgNr10a;
    }

    public void setRgNr10a(String rgNr10a) {
        this.rgNr10a = rgNr10a;
    }

    public String getRdat() {
        return rdat;
    }

    public void setRdat(String rdat) {
        this.rdat = rdat;
    }

    public String getKzS() {
        return kzS;
    }

    public void setKzS(String kzS) {
        this.kzS = kzS;
    }

    public String getAnr() {
        return anr;
    }

    public void setAnr(String anr) {
        this.anr = anr;
    }

    public String getBerei() {
        return berei;
    }

    public void setBerei(String berei) {
        this.berei = berei;
    }

    public String getWt() {
        return wt;
    }

    public void setWt(String wt) {
        this.wt = wt;
    }

    public String getSplitt() {
        return splitt;
    }

    public void setSplitt(String splitt) {
        this.splitt = splitt;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getEc() {
        return ec;
    }

    public void setEc(String ec) {
        this.ec = ec;
    }

    public Integer getLnrPak() {
        return lnrPak;
    }

    public void setLnrPak(Integer lnrPak) {
        this.lnrPak = lnrPak;
    }

    public String getPaketNr() {
        return paketNr;
    }

    public void setPaketNr(String paketNr) {
        this.paketNr = paketNr;
    }

    public Integer getSortRz() {
        return sortRz;
    }

    public void setSortRz(Integer sortRz) {
        this.sortRz = sortRz;
    }

    public Integer getLnrRz() {
        return lnrRz;
    }

    public void setLnrRz(Integer lnrRz) {
        this.lnrRz = lnrRz;
    }

    public String getAg() {
        return ag;
    }

    public void setAg(String ag) {
        this.ag = ag;
    }

    public String getLnr() {
        return lnr;
    }

    public void setLnr(String lnr) {
        this.lnr = lnr;
    }

    public String getBez() {
        return bez;
    }

    public void setBez(String bez) {
        this.bez = bez;
    }

    public BigDecimal getWerkszeit() {
        return werkszeit;
    }

    public void setWerkszeit(BigDecimal werkszeit) {
        this.werkszeit = werkszeit;
    }

    public BigDecimal getAwStunden() {
        return awStunden;
    }

    public void setAwStunden(BigDecimal awStunden) {
        this.awStunden = awStunden;
    }

    public Integer getZe() {
        return ze;
    }

    public void setZe(Integer ze) {
        this.ze = ze;
    }

    public Integer getPe() {
        return pe;
    }

    public void setPe(Integer pe) {
        this.pe = pe;
    }

    public BigDecimal getSatzPe() {
        return satzPe;
    }

    public void setSatzPe(BigDecimal satzPe) {
        this.satzPe = satzPe;
    }

    public String getGewZe() {
        return gewZe;
    }

    public void setGewZe(String gewZe) {
        this.gewZe = gewZe;
    }

    public BigDecimal getPreis() {
        return preis;
    }

    public void setPreis(BigDecimal preis) {
        this.preis = preis;
    }

    public String getMonteur() {
        return monteur;
    }

    public void setMonteur(String monteur) {
        this.monteur = monteur;
    }

    public String getBc() {
        return bc;
    }

    public void setBc(String bc) {
        this.bc = bc;
    }

    public BigDecimal getvSatz() {
        return vSatz;
    }

    public void setvSatz(BigDecimal vSatz) {
        this.vSatz = vSatz;
    }

    public BigDecimal getmStunden() {
        return mStunden;
    }

    public void setmStunden(BigDecimal mStunden) {
        this.mStunden = mStunden;
    }

    public BigDecimal getvDmNetto() {
        return vDmNetto;
    }

    public void setvDmNetto(BigDecimal vDmNetto) {
        this.vDmNetto = vDmNetto;
    }

    public BigDecimal getvDmBrutto() {
        return vDmBrutto;
    }

    public void setvDmBrutto(BigDecimal vDmBrutto) {
        this.vDmBrutto = vDmBrutto;
    }

    public BigDecimal getvStunden() {
        return vStunden;
    }

    public void setvStunden(BigDecimal vStunden) {
        this.vStunden = vStunden;
    }

    public BigDecimal getZuschlag() {
        return zuschlag;
    }

    public void setZuschlag(BigDecimal zuschlag) {
        this.zuschlag = zuschlag;
    }

    public BigDecimal getRabatt() {
        return rabatt;
    }

    public void setRabatt(BigDecimal rabatt) {
        this.rabatt = rabatt;
    }

    public String getKzSAw() {
        return kzSAw;
    }

    public void setKzSAw(String kzSAw) {
        this.kzSAw = kzSAw;
    }

    public String getKzMwst() {
        return kzMwst;
    }

    public void setKzMwst(String kzMwst) {
        this.kzMwst = kzMwst;
    }

    public String getVerdichten() {
        return verdichten;
    }

    public void setVerdichten(String verdichten) {
        this.verdichten = verdichten;
    }

    public String getTxtKey() {
        return txtKey;
    }

    public void setTxtKey(String txtKey) {
        this.txtKey = txtKey;
    }

    public BigDecimal getRgBrutto() {
        return rgBrutto;
    }

    public void setRgBrutto(BigDecimal rgBrutto) {
        this.rgBrutto = rgBrutto;
    }

    public BigDecimal getRgRabatt() {
        return rgRabatt;
    }

    public void setRgRabatt(BigDecimal rgRabatt) {
        this.rgRabatt = rgRabatt;
    }

    public BigDecimal getRgNetto() {
        return rgNetto;
    }

    public void setRgNetto(BigDecimal rgNetto) {
        this.rgNetto = rgNetto;
    }

    public String getKenRe2sum() {
        return kenRe2sum;
    }

    public void setKenRe2sum(String kenRe2sum) {
        this.kenRe2sum = kenRe2sum;
    }

    public BigDecimal getUrsprFakHMon() {
        return ursprFakHMon;
    }

    public void setUrsprFakHMon(BigDecimal ursprFakHMon) {
        this.ursprFakHMon = ursprFakHMon;
    }

    public BigDecimal getUrsprNettoMon() {
        return ursprNettoMon;
    }

    public void setUrsprNettoMon(BigDecimal ursprNettoMon) {
        this.ursprNettoMon = ursprNettoMon;
    }

    public BigDecimal getEinstandspreis() {
        return einstandspreis;
    }

    public void setEinstandspreis(BigDecimal einstandspreis) {
        this.einstandspreis = einstandspreis;
    }

    public String getEpsName() {
        return epsName;
    }

    public void setEpsName(String epsName) {
        this.epsName = epsName;
    }

    public BigDecimal getEpsMinderungPercent() {
        return epsMinderungPercent;
    }

    public void setEpsMinderungPercent(BigDecimal epsMinderungPercent) {
        this.epsMinderungPercent = epsMinderungPercent;
    }

    public String getVariante() {
        return variante;
    }

    public void setVariante(String variante) {
        this.variante = variante;
    }

    public String getArbeitsbeschreibung() {
        return arbeitsbeschreibung;
    }

    public void setArbeitsbeschreibung(String arbeitsbeschreibung) {
        this.arbeitsbeschreibung = arbeitsbeschreibung;
    }

    public String getRechnungstext() {
        return rechnungstext;
    }

    public void setRechnungstext(String rechnungstext) {
        this.rechnungstext = rechnungstext;
    }
}