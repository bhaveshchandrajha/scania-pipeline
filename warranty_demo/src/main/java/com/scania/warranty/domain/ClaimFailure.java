/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
@IdClass(ClaimFailureId.class)
public class ClaimFailure {
    
    @Id
    @Column(name = "PAKZ", length = 3, nullable = false)
    private String pakz; // @rpg-trace: schema
    
    @Id
    @Column(name = "RECH.-NR.", length = 5, nullable = false)
    private String rechNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "RECH.-DATUM", length = 8, nullable = false)
    private String rechDatum; // @rpg-trace: schema
    
    @Id
    @Column(name = "AUFTRAGS-NR.", length = 5, nullable = false)
    private String auftragsNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "BEREICH", length = 1, nullable = false)
    private String bereich; // @rpg-trace: schema
    
    @Id
    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "FEHLER-NR.", length = 2, nullable = false)
    private String fehlerNr; // @rpg-trace: schema
    
    @Column(name = "FOLGE-NR.", length = 2, nullable = false)
    private String folgeNr; // @rpg-trace: schema
    
    @Column(name = "FEHLER-TEIL", length = 18, nullable = false)
    private String fehlerTeil; // @rpg-trace: schema
    
    @Column(name = "HAUPTGRUPPE", length = 2, nullable = false)
    private String hauptgruppe; // @rpg-trace: schema
    
    @Column(name = "NEBENGRUPPE", length = 2, nullable = false)
    private String nebengruppe; // @rpg-trace: schema
    
    @Column(name = "SCHAD.-C1", length = 2, nullable = false)
    private String schadC1; // @rpg-trace: schema
    
    @Column(name = "SCHAD.-C2", length = 2, nullable = false)
    private String schadC2; // @rpg-trace: schema
    
    @Column(name = "TEXT1", length = 65, nullable = false)
    private String text1; // @rpg-trace: schema
    
    @Column(name = "TEXT2", length = 65, nullable = false)
    private String text2; // @rpg-trace: schema
    
    @Column(name = "STEUER CODE", length = 2, nullable = false)
    private String steuerCode; // @rpg-trace: schema
    
    @Column(name = "BEW. CODE1", length = 2, nullable = false)
    private String bewCode1; // @rpg-trace: schema
    
    @Column(name = "BEW. CODE2", precision = 4, scale = 0, nullable = false)
    private Integer bewCode2; // @rpg-trace: schema
    
    @Column(name = "BEW. DATUM", precision = 8, scale = 0, nullable = false)
    private Integer bewDatum; // @rpg-trace: schema
    
    @Column(name = "VERG. MAT.", precision = 3, scale = 0, nullable = false)
    private Integer vergMat; // @rpg-trace: schema
    
    @Column(name = "VERG. ARB.", precision = 3, scale = 0, nullable = false)
    private Integer vergArb; // @rpg-trace: schema
    
    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0, nullable = false)
    private Integer vergSpez; // @rpg-trace: schema
    
    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2, nullable = false)
    private BigDecimal beantrMat; // @rpg-trace: schema
    
    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2, nullable = false)
    private BigDecimal beantrArb; // @rpg-trace: schema
    
    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2, nullable = false)
    private BigDecimal beantrSpez; // @rpg-trace: schema
    
    @Column(name = "CLAIM-ART", precision = 1, scale = 0, nullable = false)
    private Integer claimArt; // @rpg-trace: schema
    
    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0, nullable = false)
    private Integer vRepDatum; // @rpg-trace: schema
    
    @Column(name = "V.-KM-STAND", precision = 3, scale = 0, nullable = false)
    private Integer vKmStand; // @rpg-trace: schema
    
    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0, nullable = false)
    private Integer feldtestNr; // @rpg-trace: schema
    
    @Column(name = "KAMPAGNEN-NR.", length = 8, nullable = false)
    private String kampagnenNr; // @rpg-trace: schema
    
    @Column(name = "EPS", length = 20, nullable = false)
    private String eps; // @rpg-trace: schema
    
    @Column(name = "STATUS CODE", precision = 2, scale = 0, nullable = false)
    private Integer statusCode; // @rpg-trace: schema
    
    @Column(name = "VARIANT CODE", precision = 2, scale = 0, nullable = false)
    private Integer variantCode; // @rpg-trace: schema
    
    @Column(name = "ACTION CODE", precision = 2, scale = 0, nullable = false)
    private Integer actionCode; // @rpg-trace: schema
    
    @Column(name = "TEXT3", length = 65, nullable = false)
    private String text3; // @rpg-trace: schema
    
    @Column(name = "TEXT4", length = 65, nullable = false)
    private String text4; // @rpg-trace: schema
    
    @Column(name = "FEHLER-NR. SDE", length = 2, nullable = false)
    private String fehlerNrSde; // @rpg-trace: schema
    
    @Column(name = "ANHANG", length = 1, nullable = false)
    private String anhang; // @rpg-trace: schema
    
    @Column(name = "SOURCE", length = 5, nullable = false)
    private String source; // @rpg-trace: schema
    
    @Column(name = "COMPLAIN", length = 5, nullable = false)
    private String complain; // @rpg-trace: schema
    
    @Column(name = "SYMPTOM", length = 5, nullable = false)
    private String symptom; // @rpg-trace: schema
    
    @Column(name = "FAILURE", length = 5, nullable = false)
    private String failure; // @rpg-trace: schema
    
    @Column(name = "LOCATION", length = 5, nullable = false)
    private String location; // @rpg-trace: schema
    
    @Column(name = "REPAIR", length = 5, nullable = false)
    private String repair; // @rpg-trace: schema
    
    @Column(name = "ERG.CODE", length = 2, nullable = false)
    private String ergCode; // @rpg-trace: schema
    
    @Column(name = "RESULT1", length = 2, nullable = false)
    private String result1; // @rpg-trace: schema
    
    @Column(name = "RESULT2", length = 5, nullable = false)
    private String result2; // @rpg-trace: schema
    
    @Column(name = "FAULT1", length = 2, nullable = false)
    private String fault1; // @rpg-trace: schema
    
    @Column(name = "FAULT2", length = 5, nullable = false)
    private String fault2; // @rpg-trace: schema
    
    @Column(name = "REPLY1", length = 2, nullable = false)
    private String reply1; // @rpg-trace: schema
    
    @Column(name = "REPLY2", length = 5, nullable = false)
    private String reply2; // @rpg-trace: schema
    
    @Column(name = "EXPLANATION1", length = 2, nullable = false)
    private String explanation1; // @rpg-trace: schema
    
    @Column(name = "EXPLANATION2", length = 5, nullable = false)
    private String explanation2; // @rpg-trace: schema
    
    public ClaimFailure() {
    }
    
    // Getters and Setters
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
    
    public BigDecimal getBeantrArb() {
        return beantrArb;
    }
    
    public void setBeantrArb(BigDecimal beantrArb) {
        this.beantrArb = beantrArb;
    }
    
    public BigDecimal getBeantrSpez() {
        return beantrSpez;
    }
    
    public void setBeantrSpez(BigDecimal beantrSpez) {
        this.beantrSpez = beantrSpez;
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