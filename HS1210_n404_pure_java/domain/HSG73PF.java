package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
public class HSG73PF {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String pakz;
    
    @Column(name = "RECH.-NR.", length = 5)
    private String rechnr;
    
    @Column(name = "RECH.-DATUM", length = 8)
    private String rechdatum;
    
    @Column(name = "AUFTRAGS-NR.", length = 5)
    private String auftragsnr;
    
    @Column(name = "BEREICH", length = 1)
    private String bereich;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimnr;
    
    @Column(name = "FEHLER-NR.", length = 2)
    private String fehlernr;
    
    @Column(name = "FOLGE-NR.", length = 2)
    private String folgenr;
    
    @Column(name = "FEHLER-TEIL", length = 18)
    private String fehlerteil;
    
    @Column(name = "HAUPTGRUPPE", length = 2)
    private String hauptgruppe;
    
    @Column(name = "NEBENGRUPPE", length = 2)
    private String nebengruppe;
    
    @Column(name = "SCHAD.-C1", length = 2)
    private String schadc1;
    
    @Column(name = "SCHAD.-C2", length = 2)
    private String schadc2;
    
    @Column(name = "TEXT1", length = 65)
    private String text1;
    
    @Column(name = "TEXT2", length = 65)
    private String text2;
    
    @Column(name = "STEUER CODE", length = 2)
    private String steuercode;
    
    @Column(name = "BEW. CODE1", length = 2)
    private String bewcode1;
    
    @Column(name = "BEW. CODE2", precision = 4, scale = 0)
    private Integer bewcode2;
    
    @Column(name = "BEW. DATUM", precision = 8, scale = 0)
    private Integer bewdatum;
    
    @Column(name = "VERG. MAT.", precision = 3, scale = 0)
    private Integer vergmat;
    
    @Column(name = "VERG. ARB.", precision = 3, scale = 0)
    private Integer vergarb;
    
    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0)
    private Integer vergspez;
    
    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2)
    private BigDecimal beantrmat;
    
    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2)
    private BigDecimal beantrgarb;
    
    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2)
    private BigDecimal beantrgspez;
    
    @Column(name = "CLAIM-ART", precision = 1, scale = 0)
    private Integer claimart;
    
    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0)
    private Integer vrepdatum;
    
    @Column(name = "V.-KM-STAND", precision = 3, scale = 0)
    private Integer vkmstand;
    
    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0)
    private Integer feldtestnr;
    
    @Column(name = "KAMPAGNEN-NR.", length = 8)
    private String kampagnennr;
    
    @Column(name = "EPS", length = 20)
    private String eps;
    
    @Column(name = "STATUS CODE", precision = 2, scale = 0)
    private Integer statuscode;
    
    @Column(name = "VARIANT CODE", precision = 2, scale = 0)
    private Integer variantcode;
    
    @Column(name = "ACTION CODE", precision = 2, scale = 0)
    private Integer actioncode;
    
    @Column(name = "TEXT3", length = 65)
    private String text3;
    
    @Column(name = "TEXT4", length = 65)
    private String text4;
    
    @Column(name = "FEHLER-NR. SDE", length = 2)
    private String fehlernrsde;
    
    @Column(name = "ANHANG", length = 1)
    private String anhang;
    
    @Column(name = "SOURCE", length = 5)
    private String source;
    
    @Column(name = "COMPLAIN", length = 5)
    private String complain;
    
    @Column(name = "SYMPTOM", length = 5)
    private String symptom;
    
    @Column(name = "FAILURE", length = 5)
    private String failure;
    
    @Column(name = "LOCATION", length = 5)
    private String location;
    
    @Column(name = "REPAIR", length = 5)
    private String repair;
    
    @Column(name = "ERG.CODE", length = 2)
    private String ergcode;
    
    @Column(name = "RESULT1", length = 2)
    private String result1;
    
    @Column(name = "RESULT2", length = 5)
    private String result2;
    
    @Column(name = "FAULT1", length = 2)
    private String fault1;
    
    @Column(name = "FAULT2", length = 5)
    private String fault2;
    
    @Column(name = "REPLY1", length = 2)
    private String reply1;
    
    @Column(name = "REPLY2", length = 5)
    private String reply2;
    
    @Column(name = "EXPLANATION1", length = 2)
    private String explanation1;
    
    @Column(name = "EXPLANATION2", length = 5)
    private String explanation2;

    // Constructors
    public HSG73PF() {
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

    public String getRechnr() {
        return rechnr;
    }

    public void setRechnr(String rechnr) {
        this.rechnr = rechnr;
    }

    public String getRechdatum() {
        return rechdatum;
    }

    public void setRechdatum(String rechdatum) {
        this.rechdatum = rechdatum;
    }

    public String getAuftragsnr() {
        return auftragsnr;
    }

    public void setAuftragsnr(String auftragsnr) {
        this.auftragsnr = auftragsnr;
    }

    public String getBereich() {
        return bereich;
    }

    public void setBereich(String bereich) {
        this.bereich = bereich;
    }

    public String getClaimnr() {
        return claimnr;
    }

    public void setClaimnr(String claimnr) {
        this.claimnr = claimnr;
    }

    public String getFehlernr() {
        return fehlernr;
    }

    public void setFehlernr(String fehlernr) {
        this.fehlernr = fehlernr;
    }

    public String getFolgenr() {
        return folgenr;
    }

    public void setFolgenr(String folgenr) {
        this.folgenr = folgenr;
    }

    public String getFehlerteil() {
        return fehlerteil;
    }

    public void setFehlerteil(String fehlerteil) {
        this.fehlerteil = fehlerteil;
    }

    public String getHauptgruppe() {
        return hauptgruppe;
    }

    public void setHauptgruppe(String hauptgruppe) {
        this.hauptgruppe = hauptgruppe;
    }

    public String getNebengruppe() {
        return nebengruppe;
    }

    public void setNebengruppe(String nebengruppe) {
        this.nebengruppe = nebengruppe;
    }

    public String getSchadc1() {
        return schadc1;
    }

    public void setSchadc1(String schadc1) {
        this.schadc1 = schadc1;
    }

    public String getSchadc2() {
        return schadc2;
    }

    public void setSchadc2(String schadc2) {
        this.schadc2 = schadc2;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getSteuercode() {
        return steuercode;
    }

    public void setSteuercode(String steuercode) {
        this.steuercode = steuercode;
    }

    public String getBewcode1() {
        return bewcode1;
    }

    public void setBewcode1(String bewcode1) {
        this.bewcode1 = bewcode1;
    }

    public Integer getBewcode2() {
        return bewcode2;
    }

    public void setBewcode2(Integer bewcode2) {
        this.bewcode2 = bewcode2;
    }

    public Integer getBewdatum() {
        return bewdatum;
    }

    public void setBewdatum(Integer bewdatum) {
        this.bewdatum = bewdatum;
    }

    public Integer getVergmat() {
        return vergmat;
    }

    public void setVergmat(Integer vergmat) {
        this.vergmat = vergmat;
    }

    public Integer getVergarb() {
        return vergarb;
    }

    public void setVergarb(Integer vergarb) {
        this.vergarb = vergarb;
    }

    public Integer getVergspez() {
        return vergspez;
    }

    public void setVergspez(Integer vergspez) {
        this.vergspez = vergspez;
    }

    public BigDecimal getBeantrmat() {
        return beantrmat;
    }

    public void setBeantrmat(BigDecimal beantrmat) {
        this.beantrmat = beantrmat;
    }

    public BigDecimal getBeantrgarb() {
        return beantrgarb;
    }

    public void setBeantrgarb(BigDecimal beantrgarb) {
        this.beantrgarb = beantrgarb;
    }

    public BigDecimal getBeantrgspez() {
        return beantrgspez;
    }

    public void setBeantrgspez(BigDecimal beantrgspez) {
        this.beantrgspez = beantrgspez;
    }

    public Integer getClaimart() {
        return claimart;
    }

    public void setClaimart(Integer claimart) {
        this.claimart = claimart;
    }

    public Integer getVrepdatum() {
        return vrepdatum;
    }

    public void setVrepdatum(Integer vrepdatum) {
        this.vrepdatum = vrepdatum;
    }

    public Integer getVkmstand() {
        return vkmstand;
    }

    public void setVkmstand(Integer vkmstand) {
        this.vkmstand = vkmstand;
    }

    public Integer getFeldtestnr() {
        return feldtestnr;
    }

    public void setFeldtestnr(Integer feldtestnr) {
        this.feldtestnr = feldtestnr;
    }

    public String getKampagnennr() {
        return kampagnennr;
    }

    public void setKampagnennr(String kampagnennr) {
        this.kampagnennr = kampagnennr;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public Integer getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(Integer statuscode) {
        this.statuscode = statuscode;
    }

    public Integer getVariantcode() {
        return variantcode;
    }

    public void setVariantcode(Integer variantcode) {
        this.variantcode = variantcode;
    }

    public Integer getActioncode() {
        return actioncode;
    }

    public void setActioncode(Integer actioncode) {
        this.actioncode = actioncode;
    }

    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    public String getFehlernrsde() {
        return fehlernrsde;
    }

    public void setFehlernrsde(String fehlernrsde) {
        this.fehlernrsde = fehlernrsde;
    }

    public String getAnhang() {
        return anhang;
    }

    public void setAnhang(String anhang) {
        this.anhang = anhang;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getComplain() {
        return complain;
    }

    public void setComplain(String complain) {
        this.complain = complain;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRepair() {
        return repair;
    }

    public void setRepair(String repair) {
        this.repair = repair;
    }

    public String getErgcode() {
        return ergcode;
    }

    public void setErgcode(String ergcode) {
        this.ergcode = ergcode;
    }

    public String getResult1() {
        return result1;
    }

    public void setResult1(String result1) {
        this.result1 = result1;
    }

    public String getResult2() {
        return result2;
    }

    public void setResult2(String result2) {
        this.result2 = result2;
    }

    public String getFault1() {
        return fault1;
    }

    public void setFault1(String fault1) {
        this.fault1 = fault1;
    }

    public String getFault2() {
        return fault2;
    }

    public void setFault2(String fault2) {
        this.fault2 = fault2;
    }

    public String getReply1() {
        return reply1;
    }

    public void setReply1(String reply1) {
        this.reply1 = reply1;
    }

    public String getReply2() {
        return reply2;
    }

    public void setReply2(String reply2) {
        this.reply2 = reply2;
    }

    public String getExplanation1() {
        return explanation1;
    }

    public void setExplanation1(String explanation1) {
        this.explanation1 = explanation1;
    }

    public String getExplanation2() {
        return explanation2;
    }

    public void setExplanation2(String explanation2) {
        this.explanation2 = explanation2;
    }
}