package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "FISTAM")
public class FISTAM {

    @Id
    @Column(name = "HDLNR", length = 5, nullable = false)
    private String hdlnr;

    @Column(name = "SA", length = 2, nullable = false)
    private String sa;

    @Column(name = "ZUS", length = 1, nullable = false)
    private String zus;

    @Column(name = "KZ-FIBU", length = 2, nullable = false)
    private String kzFibu;

    @Column(name = "HDL-NR.ET", length = 4, nullable = false)
    private String hdlNrEt;

    @Column(name = "VERS", length = 1, nullable = false)
    private String vers;

    @Column(name = "FIRMENBEZEICHNUNG I", length = 40, nullable = false)
    private String firmenbezeichnungI;

    @Column(name = "FIRMENBEZEICHNUNG II", length = 30, nullable = false)
    private String firmenbezeichnungII;

    @Column(name = "STRASSE", length = 30, nullable = false)
    private String strasse;

    @Column(name = "LKZ", length = 3, nullable = false)
    private String lkz;

    @Column(name = "PLZ", length = 5, nullable = false)
    private String plz;

    @Column(name = "ORT", length = 30, nullable = false)
    private String ort;

    @Column(name = "BLZ1", length = 8, nullable = false)
    private String blz1;

    @Column(name = "RES1", length = 2, nullable = false)
    private String res1;

    @Column(name = "KTO-NR1", length = 15, nullable = false)
    private String ktoNr1;

    @Column(name = "BANKENNAME 1", length = 30, nullable = false)
    private String bankenname1;

    @Column(name = "BLZ2", length = 8, nullable = false)
    private String blz2;

    @Column(name = "RES2", length = 2, nullable = false)
    private String res2;

    @Column(name = "KTO-NR2", length = 15, nullable = false)
    private String ktoNr2;

    @Column(name = "BANKENNAME 2", length = 30, nullable = false)
    private String bankenname2;

    @Column(name = "BLZ3", length = 8, nullable = false)
    private String blz3;

    @Column(name = "RES3", length = 2, nullable = false)
    private String res3;

    @Column(name = "KTO-NR3", length = 15, nullable = false)
    private String ktoNr3;

    @Column(name = "BANKENNAME3", length = 30, nullable = false)
    private String bankenname3;

    @Column(name = "KUNDENBERATER", length = 20, nullable = false)
    private String kundenberater;

    @Column(name = "TEILEBERATER", length = 20, nullable = false)
    private String teileberater;

    @Column(name = "UST-ID-NR/OK", length = 20, nullable = false)
    private String ustIdNrOk;

    @Column(name = "KENNW1", length = 6, nullable = false)
    private String kennw1;

    @Column(name = "KENNW2", length = 6, nullable = false)
    private String kennw2;

    @Column(name = "KENNW3", length = 6, nullable = false)
    private String kennw3;

    @Column(name = "RES5", length = 4, nullable = false)
    private String res5;

    @Column(name = "LFDNR.WE", precision = 5, scale = 0, nullable = false)
    private BigDecimal lfdnrWe;

    @Column(name = "LFDNR.THE", precision = 5, scale = 0, nullable = false)
    private BigDecimal lfdnrThe;

    @Column(name = "KRE.-JOUR.", precision = 5, scale = 0, nullable = false)
    private BigDecimal kreJour;

    @Column(name = "L.JOUR-NR BU-EING1", length = 6, nullable = false)
    private String lJourNrBuEing1;

    @Column(name = "JOUR-NR", precision = 5, scale = 0, nullable = false)
    private BigDecimal jourNr;

    @Column(name = "RES8", precision = 5, scale = 0, nullable = false)
    private BigDecimal res8;

    @Column(name = "FEHL-KTO", length = 6, nullable = false)
    private String fehlKto;

    @Column(name = "BU-DAT", length = 8, nullable = false)
    private String buDat;

    @Column(name = "L.JOUR-NR BU-EING2", length = 6, nullable = false)
    private String lJourNrBuEing2;

    @Column(name = "SPAS-NR.", length = 8, nullable = false)
    private String spasNr;

    @Column(name = "RES11", precision = 7, scale = 0, nullable = false)
    private BigDecimal res11;

    @Column(name = "RES12", precision = 9, scale = 2, nullable = false)
    private BigDecimal res12;

    @Column(name = "RES13", length = 1, nullable = false)
    private String res13;

    @Column(name = "RES14", length = 1, nullable = false)
    private String res14;

    @Column(name = "RES15", length = 1, nullable = false)
    private String res15;

    // Constructors
    public FISTAM() {
    }

    // Getters and Setters
    public String getHdlnr() {
        return hdlnr;
    }

    public void setHdlnr(String hdlnr) {
        this.hdlnr = hdlnr;
    }

    public String getSa() {
        return sa;
    }

    public void setSa(String sa) {
        this.sa = sa;
    }

    public String getZus() {
        return zus;
    }

    public void setZus(String zus) {
        this.zus = zus;
    }

    public String getKzFibu() {
        return kzFibu;
    }

    public void setKzFibu(String kzFibu) {
        this.kzFibu = kzFibu;
    }

    public String getHdlNrEt() {
        return hdlNrEt;
    }

    public void setHdlNrEt(String hdlNrEt) {
        this.hdlNrEt = hdlNrEt;
    }

    public String getVers() {
        return vers;
    }

    public void setVers(String vers) {
        this.vers = vers;
    }

    public String getFirmenbezeichnungI() {
        return firmenbezeichnungI;
    }

    public void setFirmenbezeichnungI(String firmenbezeichnungI) {
        this.firmenbezeichnungI = firmenbezeichnungI;
    }

    public String getFirmenbezeichnungII() {
        return firmenbezeichnungII;
    }

    public void setFirmenbezeichnungII(String firmenbezeichnungII) {
        this.firmenbezeichnungII = firmenbezeichnungII;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getLkz() {
        return lkz;
    }

    public void setLkz(String lkz) {
        this.lkz = lkz;
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

    public String getBlz1() {
        return blz1;
    }

    public void setBlz1(String blz1) {
        this.blz1 = blz1;
    }

    public String getRes1() {
        return res1;
    }

    public void setRes1(String res1) {
        this.res1 = res1;
    }

    public String getKtoNr1() {
        return ktoNr1;
    }

    public void setKtoNr1(String ktoNr1) {
        this.ktoNr1 = ktoNr1;
    }

    public String getBankenname1() {
        return bankenname1;
    }

    public void setBankenname1(String bankenname1) {
        this.bankenname1 = bankenname1;
    }

    public String getBlz2() {
        return blz2;
    }

    public void setBlz2(String blz2) {
        this.blz2 = blz2;
    }

    public String getRes2() {
        return res2;
    }

    public void setRes2(String res2) {
        this.res2 = res2;
    }

    public String getKtoNr2() {
        return ktoNr2;
    }

    public void setKtoNr2(String ktoNr2) {
        this.ktoNr2 = ktoNr2;
    }

    public String getBankenname2() {
        return bankenname2;
    }

    public void setBankenname2(String bankenname2) {
        this.bankenname2 = bankenname2;
    }

    public String getBlz3() {
        return blz3;
    }

    public void setBlz3(String blz3) {
        this.blz3 = blz3;
    }

    public String getRes3() {
        return res3;
    }

    public void setRes3(String res3) {
        this.res3 = res3;
    }

    public String getKtoNr3() {
        return ktoNr3;
    }

    public void setKtoNr3(String ktoNr3) {
        this.ktoNr3 = ktoNr3;
    }

    public String getBankenname3() {
        return bankenname3;
    }

    public void setBankenname3(String bankenname3) {
        this.bankenname3 = bankenname3;
    }

    public String getKundenberater() {
        return kundenberater;
    }

    public void setKundenberater(String kundenberater) {
        this.kundenberater = kundenberater;
    }

    public String getTeileberater() {
        return teileberater;
    }

    public void setTeileberater(String teileberater) {
        this.teileberater = teileberater;
    }

    public String getUstIdNrOk() {
        return ustIdNrOk;
    }

    public void setUstIdNrOk(String ustIdNrOk) {
        this.ustIdNrOk = ustIdNrOk;
    }

    public String getKennw1() {
        return kennw1;
    }

    public void setKennw1(String kennw1) {
        this.kennw1 = kennw1;
    }

    public String getKennw2() {
        return kennw2;
    }

    public void setKennw2(String kennw2) {
        this.kennw2 = kennw2;
    }

    public String getKennw3() {
        return kennw3;
    }

    public void setKennw3(String kennw3) {
        this.kennw3 = kennw3;
    }

    public String getRes5() {
        return res5;
    }

    public void setRes5(String res5) {
        this.res5 = res5;
    }

    public BigDecimal getLfdnrWe() {
        return lfdnrWe;
    }

    public void setLfdnrWe(BigDecimal lfdnrWe) {
        this.lfdnrWe = lfdnrWe;
    }

    public BigDecimal getLfdnrThe() {
        return lfdnrThe;
    }

    public void setLfdnrThe(BigDecimal lfdnrThe) {
        this.lfdnrThe = lfdnrThe;
    }

    public BigDecimal getKreJour() {
        return kreJour;
    }

    public void setKreJour(BigDecimal kreJour) {
        this.kreJour = kreJour;
    }

    public String getlJourNrBuEing1() {
        return lJourNrBuEing1;
    }

    public void setlJourNrBuEing1(String lJourNrBuEing1) {
        this.lJourNrBuEing1 = lJourNrBuEing1;
    }

    public BigDecimal getJourNr() {
        return jourNr;
    }

    public void setJourNr(BigDecimal jourNr) {
        this.jourNr = jourNr;
    }

    public BigDecimal getRes8() {
        return res8;
    }

    public void setRes8(BigDecimal res8) {
        this.res8 = res8;
    }

    public String getFehlKto() {
        return fehlKto;
    }

    public void setFehlKto(String fehlKto) {
        this.fehlKto = fehlKto;
    }

    public String getBuDat() {
        return buDat;
    }

    public void setBuDat(String buDat) {
        this.buDat = buDat;
    }

    public String getlJourNrBuEing2() {
        return lJourNrBuEing2;
    }

    public void setlJourNrBuEing2(String lJourNrBuEing2) {
        this.lJourNrBuEing2 = lJourNrBuEing2;
    }

    public String getSpasNr() {
        return spasNr;
    }

    public void setSpasNr(String spasNr) {
        this.spasNr = spasNr;
    }

    public BigDecimal getRes11() {
        return res11;
    }

    public void setRes11(BigDecimal res11) {
        this.res11 = res11;
    }

    public BigDecimal getRes12() {
        return res12;
    }

    public void setRes12(BigDecimal res12) {
        this.res12 = res12;
    }

    public String getRes13() {
        return res13;
    }

    public void setRes13(String res13) {
        this.res13 = res13;
    }

    public String getRes14() {
        return res14;
    }

    public void setRes14(String res14) {
        this.res14 = res14;
    }

    public String getRes15() {
        return res15;
    }

    public void setRes15(String res15) {
        this.res15 = res15;
    }
}