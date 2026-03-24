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
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String companyCode;
    
    @Column(name = "RNR", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RG-NR. 10A", length = 10)
    private String invoiceNumber10;
    
    @Column(name = "RDAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "KZ S", length = 1)
    private String indicatorS;
    
    @Column(name = "ANR", length = 5)
    private String orderNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "W/T", length = 1)
    private String workshopTheke;
    
    @Column(name = "SPLITT", length = 2)
    private String split;
    
    @Column(name = "ADAT", length = 8)
    private String orderDate;
    
    @Column(name = "ATEXT", length = 40)
    private String orderText;
    
    @Column(name = "L.RNR", length = 5)
    private String lastInvoiceNumber;
    
    @Column(name = "STO-BEZ-RE", length = 5)
    private String stornoBezRe;
    
    @Column(name = "STO-BEZ-REDAT", length = 8)
    private String stornoBezRedat;
    
    @Column(name = "KOR-BEZ-RE", length = 5)
    private String korBezRe;
    
    @Column(name = "KOR-BEZ-REDAT", length = 8)
    private String korBezRedat;
    
    @Column(name = "BFORT", length = 1)
    private String bfort;
    
    @Column(name = "MWST Y/N", length = 1)
    private String mwstYn;
    
    @Column(name = "MWST %", precision = 5, scale = 2)
    private BigDecimal mwstPercent;
    
    @Column(name = "MWST % R.", precision = 5, scale = 2)
    private BigDecimal mwstPercentR;
    
    @Column(name = "BA-SCHLÜSSEL", length = 2)
    private String baSchluessel;
    
    @Column(name = "KST LOHN", length = 5)
    private String kstLohn;
    
    @Column(name = "KST TEILE", length = 5)
    private String kstTeile;
    
    @Column(name = "FIBU MWST", length = 6)
    private String fibuMwst;
    
    @Column(name = "FIBU MWST AT", length = 6)
    private String fibuMwstAt;
    
    @Column(name = "FIBU INTERIM", length = 6)
    private String fibuInterim;
    
    @Column(name = "KTO INTAUF.", length = 6)
    private String ktoIntauf;
    
    @Column(name = "KTR INT AUF.", length = 7)
    private String ktrIntAuf;
    
    @Column(name = "KST INT AUF.", length = 5)
    private String kstIntAuf;
    
    @Column(name = "SPEZ-CODE", length = 10)
    private String spezCode;
    
    @Column(name = "BRANCH", length = 3)
    private String branch;
    
    @Column(name = "PROD-CODE", length = 10)
    private String prodCode;
    
    @Column(name = "PROJEKT", length = 10)
    private String projekt;
    
    @Column(name = "DOKUMENTENNUMMER", length = 20)
    private String dokumentennummer;
    
    @Column(name = "KOSTENCODE KONZINT.", length = 3)
    private String kostencodeKonzint;
    
    @Column(name = "KUNDEN-NR.", length = 6)
    private String kundenNr;
    
    @Column(name = "ANREDE", length = 1)
    private String anrede;
    
    @Column(name = "NAME", length = 30)
    private String name;
    
    @Column(name = "BRANCHE", length = 25)
    private String branche;
    
    @Column(name = "MATCH", length = 5)
    private String match;
    
    @Column(name = "STRASSE", length = 25)
    private String strasse;
    
    @Column(name = "LAND", length = 3)
    private String land;
    
    @Column(name = "PLZ", length = 5)
    private String plz;
    
    @Column(name = "ORT", length = 20)
    private String ort;
    
    @Column(name = "TELEFON", length = 17)
    private String telefon;
    
    @Column(name = "BESTELLER KUNDE", length = 20)
    private String bestellerKunde;
    
    @Column(name = "VALUTA", length = 1)
    private String valuta;
    
    @Column(name = "BONITÄT", length = 1)
    private String bonitaet;
    
    @Column(name = "ZAHLUNGSART", length = 1)
    private String zahlungsart;
    
    @Column(name = "RC", length = 3)
    private String rc;
    
    @Column(name = "RE KUNDEN-NR.", length = 6)
    private String reKundenNr;
    
    @Column(name = "RE ANREDE", length = 1)
    private String reAnrede;
    
    @Column(name = "RE NAME", length = 30)
    private String reName;
    
    @Column(name = "RE BRANCHE", length = 25)
    private String reBranche;
    
    @Column(name = "RE MATCH", length = 5)
    private String reMatch;
    
    @Column(name = "RE STRASSE", length = 25)
    private String reStrasse;
    
    @Column(name = "RE LAND", length = 3)
    private String reLand;
    
    @Column(name = "RE PLZ", length = 5)
    private String rePlz;
    
    @Column(name = "RE ORT", length = 20)
    private String reOrt;
    
    @Column(name = "RE TELE.", length = 17)
    private String reTele;
    
    @Column(name = "RE VALUTA", length = 1)
    private String reValuta;
    
    @Column(name = "RE BONITÄT", length = 1)
    private String reBonitaet;
    
    @Column(name = "RE ZART", length = 1)
    private String reZart;
    
    @Column(name = "RE RC", length = 3)
    private String reRc;
    
    @Column(name = "UST-ID-NR/OK", length = 20)
    private String ustIdNrOk;
    
    @Column(name = "FAHRG.-NR.", length = 17)
    private String fahrgNr;
    
    @Column(name = "KZ", length = 12)
    private String kz;
    
    @Column(name = "TYP", length = 15)
    private String typ;
    
    @Column(name = "BJ", length = 4)
    private String bj;
    
    @Column(name = "ZDAT", length = 8)
    private String zdat;
    
    @Column(name = "WRG.", length = 3)
    private String wrg;
    
    @Column(name = "AU", length = 6)
    private String au;
    
    @Column(name = "GA", length = 8)
    private String ga;
    
    @Column(name = "SP", length = 6)
    private String sp;
    
    @Column(name = "TACHO", length = 8)
    private String tacho;
    
    @Column(name = "KM", length = 8)
    private String km;
    
    @Column(name = "HU", length = 6)
    private String hu;
    
    @Column(name = "AN-TAG", length = 8)
    private String anTag;
    
    @Column(name = "AN-ZEIT", length = 4)
    private String anZeit;
    
    @Column(name = "FERT-TAG", length = 8)
    private String fertTag;
    
    @Column(name = "FERT-ZEIT", length = 4)
    private String fertZeit;
    
    @Column(name = "BERATER", length = 20)
    private String berater;
    
    @Column(name = "LEITZAHL", length = 3)
    private String leitzahl;
    
    @Column(name = "TX.ANF", length = 3)
    private String txAnf;
    
    @Column(name = "TX.ENDE", length = 3)
    private String txEnde;
    
    @Column(name = "MOTOR-NR", length = 10)
    private String motorNr;
    
    @Column(name = "MOTOR-TYP", length = 20)
    private String motorTyp;
    
    @Column(name = "USER AUFTRAG", length = 10)
    private String userAuftrag;
    
    @Column(name = "USER RECHNUNG", length = 10)
    private String userRechnung;
    
    @Column(name = "RGS NETTO", precision = 9, scale = 2)
    private BigDecimal rgsNetto;
    
    @Column(name = "RGS BASIS AT", precision = 9, scale = 2)
    private BigDecimal rgsBasisAt;
    
    @Column(name = "RGS BASIS MWST", precision = 9, scale = 2)
    private BigDecimal rgsBasisMwst;
    
    @Column(name = "RGS MWST", precision = 9, scale = 2)
    private BigDecimal rgsMwst;
    
    @Column(name = "RGS MWST AT", precision = 9, scale = 2)
    private BigDecimal rgsMwstAt;
    
    @Column(name = "RGS GES BRUTTO", precision = 9, scale = 2)
    private BigDecimal rgsGesBrutto;
    
    @Column(name = "EG-UMSATZ", length = 1)
    private String egUmsatz;
    
    @Column(name = "STEUERFREI DRITTLAND", length = 1)
    private String steuerfreiDrittland;
    
    @Column(name = "VERBUCHT?", length = 1)
    private String verbucht;
    
    @Column(name = "RESERVE", precision = 5, scale = 2)
    private BigDecimal reserve1;
    
    @Column(name = "RESERVE", precision = 9, scale = 2)
    private BigDecimal reserve2;
    
    @Column(name = "GA-ÜBERN.", length = 8)
    private String gaUebern;
    
    @Column(name = "WKT-ID", precision = 9, scale = 0)
    private Integer wktId;
    
    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve3;
    
    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve4;
    
    @Column(name = "F:V>0", precision = 3, scale = 0)
    private Integer fvGreaterZero;
    
    @Column(name = "F:B>0", precision = 3, scale = 0)
    private Integer fbGreaterZero;
    
    @Column(name = "KAMPAGNE-NR", precision = 6, scale = 0)
    private Integer kampagneNr;
    
    @Column(name = "SPO ORDER", length = 10)
    private String spoOrder;
    
    @Column(name = "KEN-AV", length = 2)
    private String kenAv;
    
    @Column(name = "KEN-PE", length = 2)
    private String kenPe;
    
    @Column(name = "KLR-BERECH", length = 1)
    private String klrBerech;
    
    @Column(name = "KLR-BETRAG", precision = 5, scale = 2)
    private BigDecimal klrBetrag;
    
    @Column(name = "ASSI-VORGANG-NR", length = 15)
    private String assiVorgangNr;
    
    @Column(name = "ZAGA-GUELTIG", length = 8)
    private String zagaGueltig;
    
    @Column(name = "R&W FREIGABE-NR", length = 15)
    private String rwFreigabeNr;
    
    @Column(name = "KL-ERWEITERUNG", precision = 5, scale = 0)
    private Integer klErweiterung;
    
    @Column(name = "KL-AUSNAHME IDNR", length = 3)
    private String klAusnahmeIdnr;
    
    @Column(name = "KL-AUSNAHME KLARTEXT", length = 40)
    private String klAusnahmeKlartext;
    
    @Column(name = "FAHRZEUG-ART", length = 20)
    private String fahrzeugArt;
    
    @Column(name = "HERSTELLER", length = 20)
    private String hersteller;
    
    @Column(name = "AUFBAUART", length = 20)
    private String aufbauart;
    
    @Column(name = "HERSTELLER AUFBAU", length = 20)
    private String herstellerAufbau;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 1", length = 20)
    private String zusatzausruestung1;
    
    @Column(name = "HERSTELLER ZUSATZ 1", length = 20)
    private String herstellerZusatz1;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 2", length = 20)
    private String zusatzausruestung2;
    
    @Column(name = "HERSTELLER ZUSATZ 2", length = 20)
    private String herstellerZusatz2;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 3", length = 20)
    private String zusatzausruestung3;
    
    @Column(name = "HERSTELLER ZUSATZ 3", length = 20)
    private String herstellerZusatz3;
    
    @Column(name = "EINSATZART", length = 20)
    private String einsatzart;
    
    @Column(name = "EURO-NORM", length = 10)
    private String euroNorm;
    
    @Column(name = "PARTIKELFILTER", length = 1)
    private String partikelfilter;
    
    @Column(name = "IS-ART", length = 5)
    private String isArt;
    
    @Column(name = "MAIL TO", length = 200)
    private String mailTo;
    
    @Column(name = "MAIL CC", length = 200)
    private String mailCc;

    // Constructors
    public Invoice() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getInvoiceNumber10() { return invoiceNumber10; }
    public void setInvoiceNumber10(String invoiceNumber10) { this.invoiceNumber10 = invoiceNumber10; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getIndicatorS() { return indicatorS; }
    public void setIndicatorS(String indicatorS) { this.indicatorS = indicatorS; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getWorkshopTheke() { return workshopTheke; }
    public void setWorkshopTheke(String workshopTheke) { this.workshopTheke = workshopTheke; }

    public String getSplit() { return split; }
    public void setSplit(String split) { this.split = split; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderText() { return orderText; }
    public void setOrderText(String orderText) { this.orderText = orderText; }

    public String getLastInvoiceNumber() { return lastInvoiceNumber; }
    public void setLastInvoiceNumber(String lastInvoiceNumber) { this.lastInvoiceNumber = lastInvoiceNumber; }

    public String getStornoBezRe() { return stornoBezRe; }
    public void setStornoBezRe(String stornoBezRe) { this.stornoBezRe = stornoBezRe; }

    public String getStornoBezRedat() { return stornoBezRedat; }
    public void setStornoBezRedat(String stornoBezRedat) { this.stornoBezRedat = stornoBezRedat; }

    public String getKorBezRe() { return korBezRe; }
    public void setKorBezRe(String korBezRe) { this.korBezRe = korBezRe; }

    public String getKorBezRedat() { return korBezRedat; }
    public void setKorBezRedat(String korBezRedat) { this.korBezRedat = korBezRedat; }

    public String getBfort() { return bfort; }
    public void setBfort(String bfort) { this.bfort = bfort; }

    public String getMwstYn() { return mwstYn; }
    public void setMwstYn(String mwstYn) { this.mwstYn = mwstYn; }

    public BigDecimal getMwstPercent() { return mwstPercent; }
    public void setMwstPercent(BigDecimal mwstPercent) { this.mwstPercent = mwstPercent; }

    public BigDecimal getMwstPercentR() { return mwstPercentR; }
    public void setMwstPercentR(BigDecimal mwstPercentR) { this.mwstPercentR = mwstPercentR; }

    public String getBaSchluessel() { return baSchluessel; }
    public void setBaSchluessel(String baSchluessel) { this.baSchluessel = baSchluessel; }

    public String getKstLohn() { return kstLohn; }
    public void setKstLohn(String kstLohn) { this.kstLohn = kstLohn; }

    public String getKstTeile() { return kstTeile; }
    public void setKstTeile(String kstTeile) { this.kstTeile = kstTeile; }

    public String getFibuMwst() { return fibuMwst; }
    public void setFibuMwst(String fibuMwst) { this.fibuMwst = fibuMwst; }

    public String getFibuMwstAt() { return fibuMwstAt; }
    public void setFibuMwstAt(String fibuMwstAt) { this.fibuMwstAt = fibuMwstAt; }

    public String getFibuInterim() { return fibuInterim; }
    public void setFibuInterim(String fibuInterim) { this.fibuInterim = fibuInterim; }

    public String getKtoIntauf() { return ktoIntauf; }
    public void setKtoIntauf(String ktoIntauf) { this.ktoIntauf = ktoIntauf; }

    public String getKtrIntAuf() { return ktrIntAuf; }
    public void setKtrIntAuf(String ktrIntAuf) { this.ktrIntAuf = ktrIntAuf; }

    public String getKstIntAuf() { return kstIntAuf; }
    public void setKstIntAuf(String kstIntAuf) { this.kstIntAuf = kstIntAuf; }

    public String getSpezCode() { return spezCode; }
    public void setSpezCode(String spezCode) { this.spezCode = spezCode; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getProdCode() { return prodCode; }
    public void setProdCode(String prodCode) { this.prodCode = prodCode; }

    public String getProjekt() { return projekt; }
    public void setProjekt(String projekt) { this.projekt = projekt; }

    public String getDokumentennummer() { return dokumentennummer; }
    public void setDokumentennummer(String dokumentennummer) { this.dokumentennummer = dokumentennummer; }

    public String getKostencodeKonzint() { return kostencodeKonzint; }
    public void setKostencodeKonzint(String kostencodeKonzint) { this.kostencodeKonzint = kostencodeKonzint; }

    public String getKundenNr() { return kundenNr; }
    public void setKundenNr(String kundenNr) { this.kundenNr = kundenNr; }

    public String getAnrede() { return anrede; }
    public void setAnrede(String anrede) { this.anrede = anrede; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranche() { return branche; }
    public void setBranche(String branche) { this.branche = branche; }

    public String getMatch() { return match; }
    public void setMatch(String match) { this.match = match; }

    public String getStrasse() { return strasse; }
    public void setStrasse(String strasse) { this.strasse = strasse; }

    public String getLand() { return land; }
    public void setLand(String land) { this.land = land; }

    public String getPlz() { return plz; }
    public void setPlz(String plz) { this.plz = plz; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getBestellerKunde() { return bestellerKunde; }
    public void setBestellerKunde(String bestellerKunde) { this.bestellerKunde = bestellerKunde; }

    public String getValuta() { return valuta; }
    public void setValuta(String valuta) { this.valuta = valuta; }

    public String getBonitaet() { return bonitaet; }
    public void setBonitaet(String bonitaet) { this.bonitaet = bonitaet; }

    public String getZahlungsart() { return zahlungsart; }
    public void setZahlungsart(String zahlungsart) { this.zahlungsart = zahlungsart; }

    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }

    public String getReKundenNr() { return reKundenNr; }
    public void setReKundenNr(String reKundenNr) { this.reKundenNr = reKundenNr; }

    public String getReAnrede() { return reAnrede; }
    public void setReAnrede(String reAnrede) { this.reAnrede = reAnrede; }

    public String getReName() { return reName; }
    public void setReName(String reName) { this.reName = reName; }

    public String getReBranche() { return reBranche; }
    public void setReBranche(String reBranche) { this.reBranche = reBranche; }

    public String getReMatch() { return reMatch; }
    public void setReMatch(String reMatch) { this.reMatch = reMatch; }

    public String getReStrasse() { return reStrasse; }
    public void setReStrasse(String reStrasse) { this.reStrasse = reStrasse; }

    public String getReLand() { return reLand; }
    public void setReLand(String reLand) { this.reLand = reLand; }

    public String getRePlz() { return rePlz; }
    public void setRePlz(String rePlz) { this.rePlz = rePlz; }

    public String getReOrt() { return reOrt; }
    public void setReOrt(String reOrt) { this.reOrt = reOrt; }

    public String getReTele() { return reTele; }
    public void setReTele(String reTele) { this.reTele = reTele; }

    public String getReValuta() { return reValuta; }
    public void setReValuta(String reValuta) { this.reValuta = reValuta; }

    public String getReBonitaet() { return reBonitaet; }
    public void setReBonitaet(String reBonitaet) { this.reBonitaet = reBonitaet; }

    public String getReZart() { return reZart; }
    public void setReZart(String reZart) { this.reZart = reZart; }

    public String getReRc() { return reRc; }
    public void setReRc(String reRc) { this.reRc = reRc; }

    public String getUstIdNrOk() { return ustIdNrOk; }
    public void setUstIdNrOk(String ustIdNrOk) { this.ustIdNrOk = ustIdNrOk; }

    public String getFahrgNr() { return fahrgNr; }
    public void setFahrgNr(String fahrgNr) { this.fahrgNr = fahrgNr; }

    public String getKz() { return kz; }
    public void setKz(String kz) { this.kz = kz; }

    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }

    public String getBj() { return bj; }
    public void setBj(String bj) { this.bj = bj; }

    public String getZdat() { return zdat; }
    public void setZdat(String zdat) { this.zdat = zdat; }

    public String getWrg() { return wrg; }
    public void setWrg(String wrg) { this.wrg = wrg; }

    public String getAu() { return au; }
    public void setAu(String au) { this.au = au; }

    public String getGa() { return ga; }
    public void setGa(String ga) { this.ga = ga; }

    public String getSp() { return sp; }
    public void setSp(String sp) { this.sp = sp; }

    public String getTacho() { return tacho; }
    public void setTacho(String tacho) { this.tacho = tacho; }

    public String getKm() { return km; }
    public void setKm(String km) { this.km = km; }

    public String getHu() { return hu; }
    public void setHu(String hu) { this.hu = hu; }

    public String getAnTag() { return anTag; }
    public void setAnTag(String anTag) { this.anTag = anTag; }

    public String getAnZeit() { return anZeit; }
    public void setAnZeit(String anZeit) { this.anZeit = anZeit; }

    public String getFertTag() { return fertTag; }
    public void setFertTag(String fertTag) { this.fertTag = fertTag; }

    public String getFertZeit() { return fertZeit; }
    public void setFertZeit(String fertZeit) { this.fertZeit = fertZeit; }

    public String getBerater() { return berater; }
    public void setBerater(String berater) { this.berater = berater; }

    public String getLeitzahl() { return leitzahl; }
    public void setLeitzahl(String leitzahl) { this.leitzahl = leitzahl; }

    public String getTxAnf() { return txAnf; }
    public void setTxAnf(String txAnf) { this.txAnf = txAnf; }

    public String getTxEnde() { return txEnde; }
    public void setTxEnde(String txEnde) { this.txEnde = txEnde; }

    public String getMotorNr() { return motorNr; }
    public void setMotorNr(String motorNr) { this.motorNr = motorNr; }

    public String getMotorTyp() { return motorTyp; }
    public void setMotorTyp(String motorTyp) { this.motorTyp = motorTyp; }

    public String getUserAuftrag() { return userAuftrag; }
    public void setUserAuftrag(String userAuftrag) { this.userAuftrag = userAuftrag; }

    public String getUserRechnung() { return userRechnung; }
    public void setUserRechnung(String userRechnung) { this.userRechnung = userRechnung; }

    public BigDecimal getRgsNetto() { return rgsNetto; }
    public void setRgsNetto(BigDecimal rgsNetto) { this.rgsNetto = rgsNetto; }

    public BigDecimal getRgsBasisAt() { return rgsBasisAt; }
    public void setRgsBasisAt(BigDecimal rgsBasisAt) { this.rgsBasisAt = rgsBasisAt; }

    public BigDecimal getRgsBasisMwst() { return rgsBasisMwst; }
    public void setRgsBasisMwst(BigDecimal rgsBasisMwst) { this.rgsBasisMwst = rgsBasisMwst; }

    public BigDecimal getRgsMwst() { return rgsMwst; }
    public void setRgsMwst(BigDecimal rgsMwst) { this.rgsMwst = rgsMwst; }

    public BigDecimal getRgsMwstAt() { return rgsMwstAt; }
    public void setRgsMwstAt(BigDecimal rgsMwstAt) { this.rgsMwstAt = rgsMwstAt; }

    public BigDecimal getRgsGesBrutto() { return rgsGesBrutto; }
    public void setRgsGesBrutto(BigDecimal rgsGesBrutto) { this.rgsGesBrutto = rgsGesBrutto; }

    public String getEgUmsatz() { return egUmsatz; }
    public void setEgUmsatz(String egUmsatz) { this.egUmsatz = egUmsatz; }

    public String getSteuerfreiDrittland() { return steuerfreiDrittland; }
    public void setSteuerfreiDrittland(String steuerfreiDrittland) { this.steuerfreiDrittland = steuerfreiDrittland; }

    public String getVerbucht() { return verbucht; }
    public void setVerbucht(String verbucht) { this.verbucht = verbucht; }

    public BigDecimal getReserve1() { return reserve1; }
    public void setReserve1(BigDecimal reserve1) { this.reserve1 = reserve1; }

    public BigDecimal getReserve2() { return reserve2; }
    public void setReserve2(BigDecimal reserve2) { this.reserve2 = reserve2; }

    public String getGaUebern() { return gaUebern; }
    public void setGaUebern(String gaUebern) { this.gaUebern = gaUebern; }

    public Integer getWktId() { return wktId; }
    public void setWktId(Integer wktId) { this.wktId = wktId; }

    public Integer getReserve3() { return reserve3; }
    public void setReserve3(Integer reserve3) { this.reserve3 = reserve3; }

    public Integer getReserve4() { return reserve4; }
    public void setReserve4(Integer reserve4) { this.reserve4 = reserve4; }

    public Integer getFvGreaterZero() { return fvGreaterZero; }
    public void setFvGreaterZero(Integer fvGreaterZero) { this.fvGreaterZero = fvGreaterZero; }

    public Integer getFbGreaterZero() { return fbGreaterZero; }
    public void setFbGreaterZero(Integer fbGreaterZero) { this.fbGreaterZero = fbGreaterZero; }

    public Integer getKampagneNr() { return kampagneNr; }
    public void setKampagneNr(Integer kampagneNr) { this.kampagneNr = kampagneNr; }

    public String getSpoOrder() { return spoOrder; }
    public void setSpoOrder(String spoOrder) { this.spoOrder = spoOrder; }

    public String getKenAv() { return kenAv; }
    public void setKenAv(String kenAv) { this.kenAv = kenAv; }

    public String getKenPe() { return kenPe; }
    public void setKenPe(String kenPe) { this.kenPe = kenPe; }

    public String getKlrBerech() { return klrBerech; }
    public void setKlrBerech(String klrBerech) { this.klrBerech = klrBerech; }

    public BigDecimal getKlrBetrag() { return klrBetrag; }
    public void setKlrBetrag(BigDecimal klrBetrag) { this.klrBetrag = klrBetrag; }

    public String getAssiVorgangNr() { return assiVorgangNr; }
    public void setAssiVorgangNr(String assiVorgangNr) { this.assiVorgangNr = assiVorgangNr; }

    public String getZagaGueltig() { return zagaGueltig; }
    public void setZagaGueltig(String zagaGueltig) { this.zagaGueltig = zagaGueltig; }

    public String getRwFreigabeNr() { return rwFreigabeNr; }
    public void setRwFreigabeNr(String rwFreigabeNr) { this.rwFreigabeNr = rwFreigabeNr; }

    public Integer getKlErweiterung() { return klErweiterung; }
    public void setKlErweiterung(Integer klErweiterung) { this.klErweiterung = klErweiterung; }

    public String getKlAusnahmeIdnr() { return klAusnahmeIdnr; }
    public void setKlAusnahmeIdnr(String klAusnahmeIdnr) { this.klAusnahmeIdnr = klAusnahmeIdnr; }

    public String getKlAusnahmeKlartext() { return klAusnahmeKlartext; }
    public void setKlAusnahmeKlartext(String klAusnahmeKlartext) { this.klAusnahmeKlartext = klAusnahmeKlartext; }

    public String getFahrzeugArt() { return fahrzeugArt; }
    public void setFahrzeugArt(String fahrzeugArt) { this.fahrzeugArt = fahrzeugArt; }

    public String getHersteller() { return hersteller; }
    public void setHersteller(String hersteller) { this.hersteller = hersteller; }

    public String getAufbauart() { return aufbauart; }
    public void setAufbauart(String aufbauart) { this.aufbauart = aufbauart; }

    public String getHerstellerAufbau() { return herstellerAufbau; }
    public void setHerstellerAufbau(String herstellerAufbau) { this.herstellerAufbau = herstellerAufbau; }

    public String getZusatzausruestung1() { return zusatzausruestung1; }
    public void setZusatzausruestung1(String zusatzausruestung1) { this.zusatzausruestung1 = zusatzausruestung1; }

    public String getHerstellerZusatz1() { return herstellerZusatz1; }
    public void setHerstellerZusatz1(String herstellerZusatz1) { this.herstellerZusatz1 = herstellerZusatz1; }

    public String getZusatzausruestung2() { return zusatzausruestung2; }
    public void setZusatzausruestung2(String zusatzausruestung2) { this.zusatzausruestung2 = zusatzausruestung2; }

    public String getHerstellerZusatz2() { return herstellerZusatz2; }
    public void setHerstellerZusatz2(String herstellerZusatz2) { this.herstellerZusatz2 = herstellerZusatz2; }

    public String getZusatzausruestung3() { return zusatzausruestung3; }
    public void setZusatzausruestung3(String zusatzausruestung3) { this.zusatzausruestung3 = zusatzausruestung3; }

    public String getHerstellerZusatz3() { return herstellerZusatz3; }
    public void setHerstellerZusatz3(String herstellerZusatz3) { this.herstellerZusatz3 = herstellerZusatz3; }

    public String getEinsatzart() { return einsatzart; }
    public void setEinsatzart(String einsatzart) { this.einsatzart = einsatzart; }

    public String getEuroNorm() { return euroNorm; }
    public void setEuroNorm(String euroNorm) { this.euroNorm = euroNorm; }

    public String getPartikelfilter() { return partikelfilter; }
    public void setPartikelfilter(String partikelfilter) { this.partikelfilter = partikelfilter; }

    public String getIsArt() { return isArt; }
    public void setIsArt(String isArt) { this.isArt = isArt; }

    public String getMailTo() { return mailTo; }
    public void setMailTo(String mailTo) { this.mailTo = mailTo; }

    public String getMailCc() { return mailCc; }
    public void setMailCc(String mailCc) { this.mailCc = mailCc; }
}