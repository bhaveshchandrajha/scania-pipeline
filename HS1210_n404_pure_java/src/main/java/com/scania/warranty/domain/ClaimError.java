package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
public class ClaimError {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String pakz;
    
    @Column(name = "RECH.-NR.", length = 5)
    private String rechNr;
    
    @Column(name = "RECH.-DATUM", length = 8)
    private String rechDatum;
    
    @Column(name = "AUFTRAGS-NR.", length = 5)
    private String auftragsNr;
    
    @Column(name = "BEREICH", length = 1)
    private String bereich;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNr;
    
    @Column(name = "FEHLER-NR.", length = 2)
    private String fehlerNr;
    
    @Column(name = "FOLGE-NR.", length = 2)
    private String folgeNr;
    
    @Column(name = "FEHLER-TEIL", length = 18)
    private String fehlerTeil;
    
    @Column(name = "HAUPTGRUPPE", length = 2)
    private String hauptgruppe;
    
    @Column(name = "NEBENGRUPPE", length = 2)
    private String nebengruppe;
    
    @Column(name = "SCHAD.-C1", length = 2)
    private String schadC1;
    
    @Column(name = "SCHAD.-C2", length = 2)
    private String schadC2;
    
    @Column(name = "TEXT1", length = 65)
    private String text1;
    
    @Column(name = "TEXT2", length = 65)
    private String text2;
    
    @Column(name = "STEUER CODE", length = 2)
    private String steuerCode;
    
    @Column(name = "BEW. CODE1", length = 2)
    private String bewCode1;
    
    @Column(name = "BEW. CODE2", precision = 4, scale = 0)
    private Integer bewCode2;
    
    @Column(name = "BEW. DATUM", precision = 8, scale = 0)
    private Integer bewDatum;
    
    @Column(name = "VERG. MAT.", precision = 3, scale = 0)
    private Integer vergMat;
    
    @Column(name = "VERG. ARB.", precision = 3, scale = 0)
    private Integer vergArb;
    
    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0)
    private Integer vergSpez;
    
    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2)
    private BigDecimal beantrMat;
    
    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2)
    private BigDecimal beantrgArb;
    
    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2)
    private BigDecimal beantrgSpez;
    
    @Column(name = "CLAIM-ART", precision = 1, scale = 0)
    private Integer claimArt;
    
    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0)
    private Integer vRepDatum;
    
    @Column(name = "V.-KM-STAND", precision = 3, scale = 0)
    private Integer vKmStand;
    
    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0)
    private Integer feldtestNr;
    
    @Column(name = "KAMPAGNEN-NR.", length = 8)
    private String kampagnenNr;
    
    @Column(name = "EPS", length = 20)
    private String eps;
    
    @Column(name = "STATUS CODE", precision = 2, scale = 0)
    private Integer statusCode;
    
    @Column(name = "VARIANT CODE", precision = 2, scale = 0)
    private Integer variantCode;
    
    @Column(name = "ACTION CODE", precision = 2, scale = 0)
    private Integer actionCode;
    
    @Column(name = "TEXT3", length = 65)
    private String text3;
    
    @Column(name = "TEXT4", length = 65)
    private String text4;
    
    @Column(name = "FEHLER-NR. SDE", length = 2)
    private String fehlerNrSde;
    
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
    private String ergCode;
    
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
    
    public ClaimError() {
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
    
    public String getFolgeNr() {
        return folgeNr;
    }
    
    public void setFolgeNr(String folgeNr) {
        this.folgeNr = folgeNr;
    }
    
    public String getFehlerTeil() {
        return fehlerTeil;
    }
    
    public void setFehlerTeil(String fehlerTeil) {
        this.fehlerTeil = fehlerTeil;
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
    
    public String getSchadC1() {
        return schadC1;
    }
    
    public void setSchadC1(String schadC1) {
        this.schadC1 = schadC1;
    }
    
    public String getSchadC2() {
        return schadC2;
    }
    
    public void setSchadC2(String schadC2) {
        this.schadC2 = schadC2;
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
    
    public String getSteuerCode() {
        return steuerCode;
    }
    
    public void setSteuerCode(String steuerCode) {
        this.steuerCode = steuerCode;
    }
    
    public String getBewCode1() {
        return bewCode1;
    }
    
    public void setBewCode1(String bewCode1) {
        this.bewCode1 = bewCode1;
    }
    
    public Integer getBewCode2() {
        return bewCode2;
    }
    
    public void setBewCode2(Integer bewCode2) {
        this.bewCode2 = bewCode2;
    }
    
    public Integer getBewDatum() {
        return bewDatum;
    }
    
    public void setBewDatum(Integer bewDatum) {
        this.bewDatum = bewDatum;
    }
    
    public Integer getVergMat() {
        return vergMat;
    }
    
    public void setVergMat(Integer vergMat) {
        this.vergMat = vergMat;
    }
    
    public Integer getVergArb() {
        return vergArb;
    }
    
    public void setVergArb(Integer vergArb) {
        this.vergArb = vergArb;
    }
    
    public Integer getVergSpez() {
        return vergSpez;
    }
    
    public void setVergSpez(Integer vergSpez) {
        this.vergSpez = vergSpez;
    }
    
    public BigDecimal getBeantrMat() {
        return beantrMat;
    }
    
    public void setBeantrMat(BigDecimal beantrMat) {
        this.beantrMat = beantrMat;
    }
    
    public BigDecimal getBeantrgArb() {
        return beantrgArb;
    }
    
    public void setBeantrgArb(BigDecimal beantrgArb) {
        this.beantrgArb = beantrgArb;
    }
    
    public BigDecimal getBeantrgSpez() {
        return beantrgSpez;
    }
    
    public void setBeantrgSpez(BigDecimal beantrgSpez) {
        this.beantrgSpez = beantrgSpez;
    }
    
    public Integer getClaimArt() {
        return claimArt;
    }
    
    public void setClaimArt(Integer claimArt) {
        this.claimArt = claimArt;
    }
    
    public Integer getvRepDatum() {
        return vRepDatum;
    }
    
    public void setvRepDatum(Integer vRepDatum) {
        this.vRepDatum = vRepDatum;
    }
    
    public Integer getvKmStand() {
        return vKmStand;
    }
    
    public void setvKmStand(Integer vKmStand) {
        this.vKmStand = vKmStand;
    }
    
    public Integer getFeldtestNr() {
        return feldtestNr;
    }
    
    public void setFeldtestNr(Integer feldtestNr) {
        this.feldtestNr = feldtestNr;
    }
    
    public String getKampagnenNr() {
        return kampagnenNr;
    }
    
    public void setKampagnenNr(String kampagnenNr) {
        this.kampagnenNr = kampagnenNr;
    }
    
    public String getEps() {
        return eps;
    }
    
    public void setEps(String eps) {
        this.eps = eps;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public Integer getVariantCode() {
        return variantCode;
    }
    
    public void setVariantCode(Integer variantCode) {
        this.variantCode = variantCode;
    }
    
    public Integer getActionCode() {
        return actionCode;
    }
    
    public void setActionCode(Integer actionCode) {
        this.actionCode = actionCode;
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
    
    public String getFehlerNrSde() {
        return fehlerNrSde;
    }
    
    public void setFehlerNrSde(String fehlerNrSde) {
        this.fehlerNrSde = fehlerNrSde;
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
    
    public String getErgCode() {
        return ergCode;
    }
    
    public void setErgCode(String ergCode) {
        this.ergCode = ergCode;
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