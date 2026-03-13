/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * JPA entity for invoice header (HSAHKLF3).
 */
@Entity
@Table(name = "HSAHKLF3")
public class InvoiceHeader {

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

    @Column(name = "ADAT", length = 8, nullable = false)
    private String adat; // @rpg-trace: schema

    @Column(name = "ATEXT", length = 40, nullable = false)
    private String atext; // @rpg-trace: schema

    @Column(name = "L.RNR", length = 5, nullable = false)
    private String lrnr; // @rpg-trace: schema

    @Column(name = "STO-BEZ-RE", length = 5, nullable = false)
    private String stoBezRe; // @rpg-trace: schema

    @Column(name = "STO-BEZ-REDAT", length = 8, nullable = false)
    private String stoBezRedat; // @rpg-trace: schema

    @Column(name = "KOR-BEZ-RE", length = 5, nullable = false)
    private String korBezRe; // @rpg-trace: schema

    @Column(name = "KOR-BEZ-REDAT", length = 8, nullable = false)
    private String korBezRedat; // @rpg-trace: schema

    @Column(name = "BFORT", length = 1, nullable = false)
    private String bfort; // @rpg-trace: schema

    @Column(name = "MWST Y/N", length = 1, nullable = false)
    private String mwstYn; // @rpg-trace: schema

    @Column(name = "MWST %", precision = 5, scale = 2, nullable = false)
    private BigDecimal mwstPercent; // @rpg-trace: schema

    @Column(name = "MWST % R.", precision = 5, scale = 2, nullable = false)
    private BigDecimal mwstPercentR; // @rpg-trace: schema

    @Column(name = "BA-SCHLÜSSEL", length = 2, nullable = false)
    private String baSchluessel; // @rpg-trace: schema

    @Column(name = "KST LOHN", length = 5, nullable = false)
    private String kstLohn; // @rpg-trace: schema

    @Column(name = "KST TEILE", length = 5, nullable = false)
    private String kstTeile; // @rpg-trace: schema

    @Column(name = "FIBU MWST", length = 6, nullable = false)
    private String fibuMwst; // @rpg-trace: schema

    @Column(name = "FIBU MWST AT", length = 6, nullable = false)
    private String fibuMwstAt; // @rpg-trace: schema

    @Column(name = "FIBU INTERIM", length = 6, nullable = false)
    private String fibuInterim; // @rpg-trace: schema

    @Column(name = "KTO INTAUF.", length = 6, nullable = false)
    private String ktoIntauf; // @rpg-trace: schema

    @Column(name = "KTR INT AUF.", length = 7, nullable = false)
    private String ktrIntAuf; // @rpg-trace: schema

    @Column(name = "KST INT AUF.", length = 5, nullable = false)
    private String kstIntAuf; // @rpg-trace: schema

    @Column(name = "SPEZ-CODE", length = 10, nullable = false)
    private String spezCode; // @rpg-trace: schema

    @Column(name = "BRANCH", length = 3, nullable = false)
    private String branch; // @rpg-trace: schema

    @Column(name = "PROD-CODE", length = 10, nullable = false)
    private String prodCode; // @rpg-trace: schema

    @Column(name = "PROJEKT", length = 10, nullable = false)
    private String projekt; // @rpg-trace: schema

    @Column(name = "DOKUMENTENNUMMER", length = 20, nullable = false)
    private String dokumentennummer; // @rpg-trace: schema

    @Column(name = "KOSTENCODE KONZINT.", length = 3, nullable = false)
    private String kostencodeKonzint; // @rpg-trace: schema

    @Column(name = "KUNDEN-NR.", length = 6, nullable = false)
    private String kundenNr; // @rpg-trace: schema

    @Column(name = "ANREDE", length = 1, nullable = false)
    private String anrede; // @rpg-trace: schema

    @Column(name = "NAME", length = 30, nullable = false)
    private String name; // @rpg-trace: schema

    @Column(name = "BRANCHE", length = 25, nullable = false)
    private String branche; // @rpg-trace: schema

    @Column(name = "MATCH", length = 5, nullable = false)
    private String match; // @rpg-trace: schema

    @Column(name = "STRASSE", length = 25, nullable = false)
    private String strasse; // @rpg-trace: schema

    @Column(name = "LAND", length = 3, nullable = false)
    private String land; // @rpg-trace: schema

    @Column(name = "PLZ", length = 5, nullable = false)
    private String plz; // @rpg-trace: schema

    @Column(name = "ORT", length = 20, nullable = false)
    private String ort; // @rpg-trace: schema

    @Column(name = "TELEFON", length = 17, nullable = false)
    private String telefon; // @rpg-trace: schema

    @Column(name = "BESTELLER KUNDE", length = 20, nullable = false)
    private String bestellerKunde; // @rpg-trace: schema

    @Column(name = "VALUTA", length = 1, nullable = false)
    private String valuta; // @rpg-trace: schema

    @Column(name = "BONIT#T", length = 1, nullable = false)
    private String bonitaet; // @rpg-trace: schema

    @Column(name = "ZAHLUNGSART", length = 1, nullable = false)
    private String zahlungsart; // @rpg-trace: schema

    @Column(name = "RC", length = 3, nullable = false)
    private String rc; // @rpg-trace: schema

    @Column(name = "RE KUNDEN-NR.", length = 6, nullable = false)
    private String reKundenNr; // @rpg-trace: schema

    @Column(name = "RE ANREDE", length = 1, nullable = false)
    private String reAnrede; // @rpg-trace: schema

    @Column(name = "RE NAME", length = 30, nullable = false)
    private String reName; // @rpg-trace: schema

    @Column(name = "RE BRANCHE", length = 25, nullable = false)
    private String reBranche; // @rpg-trace: schema

    @Column(name = "RE MATCH", length = 5, nullable = false)
    private String reMatch; // @rpg-trace: schema

    @Column(name = "RE STRASSE", length = 25, nullable = false)
    private String reStrasse; // @rpg-trace: schema

    @Column(name = "RE LAND", length = 3, nullable = false)
    private String reland; // @rpg-trace: schema

    @Column(name = "RE PLZ", length = 5, nullable = false)
    private String rePlz; // @rpg-trace: schema

    @Column(name = "RE ORT", length = 20, nullable = false)
    private String reOrt; // @rpg-trace: schema

    @Column(name = "RE TELE.", length = 17, nullable = false)
    private String reTele; // @rpg-trace: schema

    @Column(name = "RE VALUTA", length = 1, nullable = false)
    private String reValuta; // @rpg-trace: schema

    @Column(name = "RE BONIT#T", length = 1, nullable = false)
    private String reBonitaet; // @rpg-trace: schema

    @Column(name = "RE ZART", length = 1, nullable = false)
    private String reZart; // @rpg-trace: schema

    @Column(name = "RE RC", length = 3, nullable = false)
    private String reRc; // @rpg-trace: schema

    @Column(name = "UST-ID-NR/OK", length = 20, nullable = false)
    private String ustIdNrOk; // @rpg-trace: schema

    @Column(name = "FAHRG.-NR.", length = 17, nullable = false)
    private String fahrgnr; // @rpg-trace: schema

    @Column(name = "KZ", length = 12, nullable = false)
    private String kz; // @rpg-trace: schema

    @Column(name = "TYP", length = 15, nullable = false)
    private String typ; // @rpg-trace: schema

    @Column(name = "BJ", length = 4, nullable = false)
    private String bj; // @rpg-trace: schema

    @Column(name = "ZDAT", length = 8, nullable = false)
    private String zdat; // @rpg-trace: schema

    @Column(name = "WRG.", length = 3, nullable = false)
    private String wrg; // @rpg-trace: schema

    @Column(name = "AU", length = 6, nullable = false)
    private String au; // @rpg-trace: schema

    @Column(name = "GA", length = 8, nullable = false)
    private String ga; // @rpg-trace: schema

    @Column(name = "SP", length = 6, nullable = false)
    private String sp; // @rpg-trace: schema

    @Column(name = "TACHO", length = 8, nullable = false)
    private String tacho; // @rpg-trace: schema

    @Column(name = "KM", length = 8, nullable = false)
    private String km; // @rpg-trace: schema

    @Column(name = "HU", length = 6, nullable = false)
    private String hu; // @rpg-trace: schema

    @Column(name = "AN-TAG", length = 8, nullable = false)
    private String anTag; // @rpg-trace: schema

    @Column(name = "AN-ZEIT", length = 4, nullable = false)
    private String anZeit; // @rpg-trace: schema

    @Column(name = "FERT-TAG", length = 8, nullable = false)
    private String fertTag; // @rpg-trace: schema

    @Column(name = "FERT-ZEIT", length = 4, nullable = false)
    private String fertZeit; // @rpg-trace: schema

    @Column(name = "BERATER", length = 20, nullable = false)
    private String berater; // @rpg-trace: schema

    @Column(name = "LEITZAHL", length = 3, nullable = false)
    private String leitzahl; // @rpg-trace: schema

    @Column(name = "TX.ANF", length = 3, nullable = false)
    private String txAnf; // @rpg-trace: schema

    @Column(name = "TX.ENDE", length = 3, nullable = false)
    private String txEnde; // @rpg-trace: schema

    @Column(name = "MOTOR-NR", length = 10, nullable = false)
    private String motornr; // @rpg-trace: schema

    @Column(name = "MOTOR-TYP", length = 20, nullable = false)
    private String motorTyp; // @rpg-trace: schema

    @Column(name = "USER AUFTRAG", length = 10, nullable = false)
    private String userAuftrag; // @rpg-trace: schema

    @Column(name = "USER RECHNUNG", length = 10, nullable = false)
    private String userRechnung; // @rpg-trace: schema

    @Column(name = "RGS NETTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsNetto; // @rpg-trace: schema

    @Column(name = "RGS BASIS AT", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsBasisAt; // @rpg-trace: schema

    @Column(name = "RGS BASIS MWST", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsBasisMwst; // @rpg-trace: schema

    @Column(name = "RGS MWST", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsMwst; // @rpg-trace: schema

    @Column(name = "RGS MWST AT", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsMwstAt; // @rpg-trace: schema

    @Column(name = "RGS GES BRUTTO", precision = 9, scale = 2, nullable = false)
    private BigDecimal rgsGesBrutto; // @rpg-trace: schema

    @Column(name = "EG-UMSATZ", length = 1, nullable = false)
    private String egUmsatz; // @rpg-trace: schema

    @Column(name = "STEUERFREI DRITTLAND", length = 1, nullable = false)
    private String steuerfreiDrittland; // @rpg-trace: schema

    @Column(name = "VERBUCHT?", length = 1, nullable = false)
    private String verbucht; // @rpg-trace: schema

    @Column(name = "RESERVE1", precision = 5, scale = 2, nullable = false)
    private BigDecimal reserve1; // @rpg-trace: schema (was RESERVE)

    @Column(name = "RESERVE2", precision = 9, scale = 2, nullable = false)
    private BigDecimal reserve2; // @rpg-trace: schema (was RESERVE)

    @Column(name = "GA-ÜBERN.", length = 8, nullable = false)
    private String gaUebern; // @rpg-trace: schema

    @Column(name = "WKT-ID", precision = 9, scale = 0, nullable = false)
    private Integer wktId; // @rpg-trace: schema

    @Column(name = "RESERVE3", precision = 2, scale = 0, nullable = false)
    private Integer reserve3; // @rpg-trace: schema (was RESERVE)

    @Column(name = "RESERVE4", precision = 2, scale = 0, nullable = false)
    private Integer reserve4; // @rpg-trace: schema (was RESERVE)

    @Column(name = "F:V>0", precision = 3, scale = 0, nullable = false)
    private Integer fvGt0; // @rpg-trace: schema

    @Column(name = "F:B>0", precision = 3, scale = 0, nullable = false)
    private Integer fbGt0; // @rpg-trace: schema

    @Column(name = "KAMPAGNE-NR", precision = 6, scale = 0, nullable = false)
    private Integer kampagneNr; // @rpg-trace: schema

    @Column(name = "SPO ORDER", length = 10, nullable = false)
    private String spoOrder; // @rpg-trace: schema

    @Column(name = "KEN-AV", length = 2, nullable = false)
    private String kenAv; // @rpg-trace: schema

    @Column(name = "KEN-PE", length = 2, nullable = false)
    private String kenPe; // @rpg-trace: schema

    @Column(name = "KLR-BERECH", length = 1, nullable = false)
    private String klrBerech; // @rpg-trace: schema

    @Column(name = "KLR-BETRAG", precision = 5, scale = 2, nullable = false)
    private BigDecimal klrBetrag; // @rpg-trace: schema

    @Column(name = "ASSI-VORGANG-NR", length = 15, nullable = false)
    private String assiVorgangNr; // @rpg-trace: schema

    @Column(name = "ZAGA-GUELTIG", length = 8, nullable = false)
    private String zagaGueltig; // @rpg-trace: schema

    @Column(name = "R&W FREIGABE-NR", length = 15, nullable = false)
    private String rwFreigabeNr; // @rpg-trace: schema

    @Column(name = "KL-ERWEITERUNG", precision = 5, scale = 0, nullable = false)
    private Integer klErweiterung; // @rpg-trace: schema

    @Column(name = "KL-AUSNAHME IDNR", length = 3, nullable = false)
    private String klAusnahmeIdnr; // @rpg-trace: schema

    @Column(name = "KL-AUSNAHME KLARTEXT", length = 40, nullable = false)
    private String klAusnahmeKlartext; // @rpg-trace: schema

    @Column(name = "FAHRZEUG-ART", length = 20, nullable = false)
    private String fahrzeugArt; // @rpg-trace: schema

    @Column(name = "HERSTELLER", length = 20, nullable = false)
    private String hersteller; // @rpg-trace: schema

    @Column(name = "AUFBAUART", length = 20, nullable = false)
    private String aufbauart; // @rpg-trace: schema

    @Column(name = "HERSTELLER AUFBAU", length = 20, nullable = false)
    private String herstellerAufbau; // @rpg-trace: schema

    @Column(name = "ZUSATZAUSRÜSTUNG 1", length = 20, nullable = false)
    private String zusatzausruestung1; // @rpg-trace: schema

    @Column(name = "HERSTELLER ZUSATZ 1", length = 20, nullable = false)
    private String herstellerZusatz1; // @rpg-trace: schema

    @Column(name = "ZUSATZAUSRÜSTUNG 2", length = 20, nullable = false)
    private String zusatzausruestung2; // @rpg-trace: schema

    @Column(name = "HERSTELLER ZUSATZ 2", length = 20, nullable = false)
    private String herstellerZusatz2; // @rpg-trace: schema

    @Column(name = "ZUSATZAUSRÜSTUNG 3", length = 20, nullable = false)
    private String zusatzausruestung3; // @rpg-trace: schema

    @Column(name = "HERSTELLER ZUSATZ 3", length = 20, nullable = false)
    private String herstellerZusatz3; // @rpg-trace: schema

    @Column(name = "EINSATZART", length = 20, nullable = false)
    private String einsatzart; // @rpg-trace: schema

    @Column(name = "EURO-NORM", length = 10, nullable = false)
    private String euroNorm; // @rpg-trace: schema

    @Column(name = "PARTIKELFILTER", length = 1, nullable = false)
    private String partikelfilter; // @rpg-trace: schema

    @Column(name = "IS-ART", length = 5, nullable = false)
    private String isArt; // @rpg-trace: schema

    @Column(name = "MAIL TO", length = 200, nullable = false)
    private String mailTo; // @rpg-trace: schema

    @Column(name = "MAIL CC", length = 200, nullable = false)
    private String mailCc; // @rpg-trace: schema

    public InvoiceHeader() {
    }

    // Getters and Setters
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

    public String getAdat() {
        return adat;
    }

    public void setAdat(String adat) {
        this.adat = adat;
    }

    public String getAtext() {
        return atext;
    }

    public void setAtext(String atext) {
        this.atext = atext;
    }

    public String getLrnr() {
        return lrnr;
    }

    public void setLrnr(String lrnr) {
        this.lrnr = lrnr;
    }

    public String getStoBezRe() {
        return stoBezRe;
    }

    public void setStoBezRe(String stoBezRe) {
        this.stoBezRe = stoBezRe;
    }

    public String getStoBezRedat() {
        return stoBezRedat;
    }

    public void setStoBezRedat(String stoBezRedat) {
        this.stoBezRedat = stoBezRedat;
    }

    public String getKorBezRe() {
        return korBezRe;
    }

    public void setKorBezRe(String korBezRe) {
        this.korBezRe = korBezRe;
    }

    public String getKorBezRedat() {
        return korBezRedat;
    }

    public void setKorBezRedat(String korBezRedat) {
        this.korBezRedat = korBezRedat;
    }

    public String getBfort() {
        return bfort;
    }

    public void setBfort(String bfort) {
        this.bfort = bfort;
    }

    public String getMwstYn() {
        return mwstYn;
    }

    public void setMwstYn(String mwstYn) {
        this.mwstYn = mwstYn;
    }

    public BigDecimal getMwstPercent() {
        return mwstPercent;
    }

    public void setMwstPercent(BigDecimal mwstPercent) {
        this.mwstPercent = mwstPercent;
    }

    public BigDecimal getMwstPercentR() {
        return mwstPercentR;
    }

    public void setMwstPercentR(BigDecimal mwstPercentR) {
        this.mwstPercentR = mwstPercentR;
    }

    public String getBaSchluessel() {
        return baSchluessel;
    }

    public void setBaSchluessel(String baSchluessel) {
        this.baSchluessel = baSchluessel;
    }

    public String getKstLohn() {
        return kstLohn;
    }

    public void setKstLohn(String kstLohn) {
        this.kstLohn = kstLohn;
    }

    public String getKstTeile() {
        return kstTeile;
    }

    public void setKstTeile(String kstTeile) {
        this.kstTeile = kstTeile;
    }

    public String getFibuMwst() {
        return fibuMwst;
    }

    public void setFibuMwst(String fibuMwst) {
        this.fibuMwst = fibuMwst;
    }

    public String getFibuMwstAt() {
        return fibuMwstAt;
    }

    public void setFibuMwstAt(String fibuMwstAt) {
        this.fibuMwstAt = fibuMwstAt;
    }

    public String getFibuInterim() {
        return fibuInterim;
    }

    public void setFibuInterim(String fibuInterim) {
        this.fibuInterim = fibuInterim;
    }

    public String getKtoIntauf() {
        return ktoIntauf;
    }

    public void setKtoIntauf(String ktoIntauf) {
        this.ktoIntauf = ktoIntauf;
    }

    public String getKtrIntAuf() {
        return ktrIntAuf;
    }

    public void setKtrIntAuf(String ktrIntAuf) {
        this.ktrIntAuf = ktrIntAuf;
    }

    public String getKstIntAuf() {
        return kstIntAuf;
    }

    public void setKstIntAuf(String kstIntAuf) {
        this.kstIntAuf = kstIntAuf;
    }

    public String getSpezCode() {
        return spezCode;
    }

    public void setSpezCode(String spezCode) {
        this.spezCode = spezCode;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getProdCode() {
        return prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public String getProjekt() {
        return projekt;
    }

    public void setProjekt(String projekt) {
        this.projekt = projekt;
    }

    public String getDokumentennummer() {
        return dokumentennummer;
    }

    public void setDokumentennummer(String dokumentennummer) {
        this.dokumentennummer = dokumentennummer;
    }

    public String getKostencodeKonzint() {
        return kostencodeKonzint;
    }

    public void setKostencodeKonzint(String kostencodeKonzint) {
        this.kostencodeKonzint = kostencodeKonzint;
    }

    public String getKundenNr() {
        return kundenNr;
    }

    public void setKundenNr(String kundenNr) {
        this.kundenNr = kundenNr;
    }

    public String getAnrede() {
        return anrede;
    }

    public void setAnrede(String anrede) {
        this.anrede = anrede;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranche() {
        return branche;
    }

    public void setBranche(String branche) {
        this.branche = branche;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getBestellerKunde() {
        return bestellerKunde;
    }

    public void setBestellerKunde(String bestellerKunde) {
        this.bestellerKunde = bestellerKunde;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    public String getBonitaet() {
        return bonitaet;
    }

    public void setBonitaet(String bonitaet) {
        this.bonitaet = bonitaet;
    }

    public String getZahlungsart() {
        return zahlungsart;
    }

    public void setZahlungsart(String zahlungsart) {
        this.zahlungsart = zahlungsart;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getReKundenNr() {
        return reKundenNr;
    }

    public void setReKundenNr(String reKundenNr) {
        this.reKundenNr = reKundenNr;
    }

    public String getReAnrede() {
        return reAnrede;
    }

    public void setReAnrede(String reAnrede) {
        this.reAnrede = reAnrede;
    }

    public String getReName() {
        return reName;
    }

    public void setReName(String reName) {
        this.reName = reName;
    }

    public String getReBranche() {
        return reBranche;
    }

    public void setReBranche(String reBranche) {
        this.reBranche = reBranche;
    }

    public String getReMatch() {
        return reMatch;
    }

    public void setReMatch(String reMatch) {
        this.reMatch = reMatch;
    }

    public String getReStrasse() {
        return reStrasse;
    }

    public void setReStrasse(String reStrasse) {
        this.reStrasse = reStrasse;
    }

    public String getReland() {
        return reland;
    }

    public void setReland(String reland) {
        this.reland = reland;
    }

    public String getRePlz() {
        return rePlz;
    }

    public void setRePlz(String rePlz) {
        this.rePlz = rePlz;
    }

    public String getReOrt() {
        return reOrt;
    }

    public void setReOrt(String reOrt) {
        this.reOrt = reOrt;
    }

    public String getReTele() {
        return reTele;
    }

    public void setReTele(String reTele) {
        this.reTele = reTele;
    }

    public String getReValuta() {
        return reValuta;
    }

    public void setReValuta(String reValuta) {
        this.reValuta = reValuta;
    }

    public String getReBonitaet() {
        return reBonitaet;
    }

    public void setReBonitaet(String reBonitaet) {
        this.reBonitaet = reBonitaet;
    }

    public String getReZart() {
        return reZart;
    }

    public void setReZart(String reZart) {
        this.reZart = reZart;
    }

    public String getReRc() {
        return reRc;
    }

    public void setReRc(String reRc) {
        this.reRc = reRc;
    }

    public String getUstIdNrOk() {
        return ustIdNrOk;
    }

    public void setUstIdNrOk(String ustIdNrOk) {
        this.ustIdNrOk = ustIdNrOk;
    }

    public String getFahrgnr() {
        return fahrgnr;
    }

    public void setFahrgnr(String fahrgnr) {
        this.fahrgnr = fahrgnr;
    }

    public String getKz() {
        return kz;
    }

    public void setKz(String kz) {
        this.kz = kz;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getBj() {
        return bj;
    }

    public void setBj(String bj) {
        this.bj = bj;
    }

    public String getZdat() {
        return zdat;
    }

    public void setZdat(String zdat) {
        this.zdat = zdat;
    }

    public String getWrg() {
        return wrg;
    }

    public void setWrg(String wrg) {
        this.wrg = wrg;
    }

    public String getAu() {
        return au;
    }

    public void setAu(String au) {
        this.au = au;
    }

    public String getGa() {
        return ga;
    }

    public void setGa(String ga) {
        this.ga = ga;
    }

    public String getSp() {
        return sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public String getTacho() {
        return tacho;
    }

    public void setTacho(String tacho) {
        this.tacho = tacho;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getHu() {
        return hu;
    }

    public void setHu(String hu) {
        this.hu = hu;
    }

    public String getAnTag() {
        return anTag;
    }

    public void setAnTag(String anTag) {
        this.anTag = anTag;
    }

    public String getAnZeit() {
        return anZeit;
    }

    public void setAnZeit(String anZeit) {
        this.anZeit = anZeit;
    }

    public String getFertTag() {
        return fertTag;
    }

    public void setFertTag(String fertTag) {
        this.fertTag = fertTag;
    }

    public String getFertZeit() {
        return fertZeit;
    }

    public void setFertZeit(String fertZeit) {
        this.fertZeit = fertZeit;
    }

    public String getBerater() {
        return berater;
    }

    public void setBerater(String berater) {
        this.berater = berater;
    }

    public String getLeitzahl() {
        return leitzahl;
    }

    public void setLeitzahl(String leitzahl) {
        this.leitzahl = leitzahl;
    }

    public String getTxAnf() {
        return txAnf;
    }

    public void setTxAnf(String txAnf) {
        this.txAnf = txAnf;
    }

    public String getTxEnde() {
        return txEnde;
    }

    public void setTxEnde(String txEnde) {
        this.txEnde = txEnde;
    }

    public String getMotornr() {
        return motornr;
    }

    public void setMotornr(String motornr) {
        this.motornr = motornr;
    }

    public String getMotorTyp() {
        return motorTyp;
    }

    public void setMotorTyp(String motorTyp) {
        this.motorTyp = motorTyp;
    }

    public String getUserAuftrag() {
        return userAuftrag;
    }

    public void setUserAuftrag(String userAuftrag) {
        this.userAuftrag = userAuftrag;
    }

    public String getUserRechnung() {
        return userRechnung;
    }

    public void setUserRechnung(String userRechnung) {
        this.userRechnung = userRechnung;
    }

    public BigDecimal getRgsNetto() {
        return rgsNetto;
    }

    public void setRgsNetto(BigDecimal rgsNetto) {
        this.rgsNetto = rgsNetto;
    }

    public BigDecimal getRgsBasisAt() {
        return rgsBasisAt;
    }

    public void setRgsBasisAt(BigDecimal rgsBasisAt) {
        this.rgsBasisAt = rgsBasisAt;
    }

    public BigDecimal getRgsBasisMwst() {
        return rgsBasisMwst;
    }

    public void setRgsBasisMwst(BigDecimal rgsBasisMwst) {
        this.rgsBasisMwst = rgsBasisMwst;
    }

    public BigDecimal getRgsMwst() {
        return rgsMwst;
    }

    public void setRgsMwst(BigDecimal rgsMwst) {
        this.rgsMwst = rgsMwst;
    }

    public BigDecimal getRgsMwstAt() {
        return rgsMwstAt;
    }

    public void setRgsMwstAt(BigDecimal rgsMwstAt) {
        this.rgsMwstAt = rgsMwstAt;
    }

    public BigDecimal getRgsGesBrutto() {
        return rgsGesBrutto;
    }

    public void setRgsGesBrutto(BigDecimal rgsGesBrutto) {
        this.rgsGesBrutto = rgsGesBrutto;
    }

    public String getEgUmsatz() {
        return egUmsatz;
    }

    public void setEgUmsatz(String egUmsatz) {
        this.egUmsatz = egUmsatz;
    }

    public String getSteuerfreiDrittland() {
        return steuerfreiDrittland;
    }

    public void setSteuerfreiDrittland(String steuerfreiDrittland) {
        this.steuerfreiDrittland = steuerfreiDrittland;
    }

    public String getVerbucht() {
        return verbucht;
    }

    public void setVerbucht(String verbucht) {
        this.verbucht = verbucht;
    }

    public BigDecimal getReserve1() {
        return reserve1;
    }

    public void setReserve1(BigDecimal reserve1) {
        this.reserve1 = reserve1;
    }

    public BigDecimal getReserve2() {
        return reserve2;
    }

    public void setReserve2(BigDecimal reserve2) {
        this.reserve2 = reserve2;
    }

    public String getGaUebern() {
        return gaUebern;
    }

    public void setGaUebern(String gaUebern) {
        this.gaUebern = gaUebern;
    }

    public Integer getWktId() {
        return wktId;
    }

    public void setWktId(Integer wktId) {
        this.wktId = wktId;
    }

    public Integer getReserve3() {
        return reserve3;
    }

    public void setReserve3(Integer reserve3) {
        this.reserve3 = reserve3;
    }

    public Integer getReserve4() {
        return reserve4;
    }

    public void setReserve4(Integer reserve4) {
        this.reserve4 = reserve4;
    }

    public Integer getFvGt0() {
        return fvGt0;
    }

    public void setFvGt0(Integer fvGt0) {
        this.fvGt0 = fvGt0;
    }

    public Integer getFbGt0() {
        return fbGt0;
    }

    public void setFbGt0(Integer fbGt0) {
        this.fbGt0 = fbGt0;
    }

    public Integer getKampagneNr() {
        return kampagneNr;
    }

    public void setKampagneNr(Integer kampagneNr) {
        this.kampagneNr = kampagneNr;
    }

    public String getSpoOrder() {
        return spoOrder;
    }

    public void setSpoOrder(String spoOrder) {
        this.spoOrder = spoOrder;
    }

    public String getKenAv() {
        return kenAv;
    }

    public void setKenAv(String kenAv) {
        this.kenAv = kenAv;
    }

    public String getKenPe() {
        return kenPe;
    }

    public void setKenPe(String kenPe) {
        this.kenPe = kenPe;
    }

    public String getKlrBerech() {
        return klrBerech;
    }

    public void setKlrBerech(String klrBerech) {
        this.klrBerech = klrBerech;
    }

    public BigDecimal getKlrBetrag() {
        return klrBetrag;
    }

    public void setKlrBetrag(BigDecimal klrBetrag) {
        this.klrBetrag = klrBetrag;
    }

    public String getAssiVorgangNr() {
        return assiVorgangNr;
    }

    public void setAssiVorgangNr(String assiVorgangNr) {
        this.assiVorgangNr = assiVorgangNr;
    }

    public String getZagaGueltig() {
        return zagaGueltig;
    }

    public void setZagaGueltig(String zagaGueltig) {
        this.zagaGueltig = zagaGueltig;
    }

    public String getRwFreigabeNr() {
        return rwFreigabeNr;
    }

    public void setRwFreigabeNr(String rwFreigabeNr) {
        this.rwFreigabeNr = rwFreigabeNr;
    }

    public Integer getKlErweiterung() {
        return klErweiterung;
    }

    public void setKlErweiterung(Integer klErweiterung) {
        this.klErweiterung = klErweiterung;
    }

    public String getKlAusnahmeIdnr() {
        return klAusnahmeIdnr;
    }

    public void setKlAusnahmeIdnr(String klAusnahmeIdnr) {
        this.klAusnahmeIdnr = klAusnahmeIdnr;
    }

    public String getKlAusnahmeKlartext() {
        return klAusnahmeKlartext;
    }

    public void setKlAusnahmeKlartext(String klAusnahmeKlartext) {
        this.klAusnahmeKlartext = klAusnahmeKlartext;
    }

    public String getFahrzeugArt() {
        return fahrzeugArt;
    }

    public void setFahrzeugArt(String fahrzeugArt) {
        this.fahrzeugArt = fahrzeugArt;
    }

    public String getHersteller() {
        return hersteller;
    }

    public void setHersteller(String hersteller) {
        this.hersteller = hersteller;
    }

    public String getAufbauart() {
        return aufbauart;
    }

    public void setAufbauart(String aufbauart) {
        this.aufbauart = aufbauart;
    }

    public String getHerstellerAufbau() {
        return herstellerAufbau;
    }

    public void setHerstellerAufbau(String herstellerAufbau) {
        this.herstellerAufbau = herstellerAufbau;
    }

    public String getZusatzausruestung1() {
        return zusatzausruestung1;
    }

    public void setZusatzausruestung1(String zusatzausruestung1) {
        this.zusatzausruestung1 = zusatzausruestung1;
    }

    public String getHerstellerZusatz1() {
        return herstellerZusatz1;
    }

    public void setHerstellerZusatz1(String herstellerZusatz1) {
        this.herstellerZusatz1 = herstellerZusatz1;
    }

    public String getZusatzausruestung2() {
        return zusatzausruestung2;
    }

    public void setZusatzausruestung2(String zusatzausruestung2) {
        this.zusatzausruestung2 = zusatzausruestung2;
    }

    public String getHerstellerZusatz2() {
        return herstellerZusatz2;
    }

    public void setHerstellerZusatz2(String herstellerZusatz2) {
        this.herstellerZusatz2 = herstellerZusatz2;
    }

    public String getZusatzausruestung3() {
        return zusatzausruestung3;
    }

    public void setZusatzausruestung3(String zusatzausruestung3) {
        this.zusatzausruestung3 = zusatzausruestung3;
    }

    public String getHerstellerZusatz3() {
        return herstellerZusatz3;
    }

    public void setHerstellerZusatz3(String herstellerZusatz3) {
        this.herstellerZusatz3 = herstellerZusatz3;
    }

    public String getEinsatzart() {
        return einsatzart;
    }

    public void setEinsatzart(String einsatzart) {
        this.einsatzart = einsatzart;
    }

    public String getEuroNorm() {
        return euroNorm;
    }

    public void setEuroNorm(String euroNorm) {
        this.euroNorm = euroNorm;
    }

    public String getPartikelfilter() {
        return partikelfilter;
    }

    public void setPartikelfilter(String partikelfilter) {
        this.partikelfilter = partikelfilter;
    }

    public String getIsArt() {
        return isArt;
    }

    public void setIsArt(String isArt) {
        this.isArt = isArt;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailCc() {
        return mailCc;
    }

    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }
}