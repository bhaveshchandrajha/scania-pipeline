package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSFLALF1")
public class HSFLALF1 {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PKZ", length = 3)
    private String pkz;
    
    @Column(name = "BES-DAT", length = 8)
    private String besdat;
    
    @Column(name = "BES-NR", length = 5)
    private String besnr;
    
    @Column(name = "LNR-FL", precision = 3, scale = 0)
    private Integer lnrfl;
    
    @Column(name = "KEN-FL", length = 8)
    private String kenfl;
    
    @Column(name = "LNR", precision = 3, scale = 0)
    private Integer lnr;
    
    @Column(name = "BESCHREIBUNG", length = 40)
    private String beschreibung;
    
    @Column(name = "TEXTZEILEN", precision = 3, scale = 0)
    private Integer textzeilen;
    
    @Column(name = "EK-PREIS", precision = 7, scale = 2)
    private BigDecimal ekpreis;
    
    @Column(name = "MENGE", precision = 5, scale = 0)
    private Integer menge;
    
    @Column(name = "EK-RENR", length = 10)
    private String ekrenr;
    
    @Column(name = "EK-REDAT", length = 8)
    private String ekredat;
    
    @Column(name = "EK BEMERKUNGEN 1", length = 60)
    private String ekbemerkungen1;
    
    @Column(name = "EK BEMERKUNGEN 2", length = 60)
    private String ekbemerkungen2;
    
    @Column(name = "EK-WERT", precision = 9, scale = 2)
    private BigDecimal ekwert;
    
    @Column(name = "ZUS %", length = 3)
    private String zuspercent;
    
    @Column(name = "VK-WERT", precision = 9, scale = 2)
    private BigDecimal vkwert;
    
    @Column(name = "AUFNR", length = 5)
    private String aufnr;
    
    @Column(name = "BEREI", length = 1)
    private String berei;
    
    @Column(name = "WT", length = 1)
    private String wt;
    
    @Column(name = "SPL", length = 2)
    private String spl;
    
    @Column(name = "AUFDAT", length = 8)
    private String aufdat;
    
    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer pos;
    
    @Column(name = "ERL-GRP", length = 2)
    private String erlgrp;
    
    @Column(name = "RECNR", length = 5)
    private String recnr;
    
    @Column(name = "RECDAT", length = 8)
    private String recdat;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "SDPS JOB UUID", length = 40)
    private String sdpsJobUuid;
    
    @Column(name = "SDPS FLA UUID", length = 40)
    private String sdpsFlaUuid;

    // Constructors
    public HSFLALF1() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkz() {
        return pkz;
    }

    public void setPkz(String pkz) {
        this.pkz = pkz;
    }

    public String getBesdat() {
        return besdat;
    }

    public void setBesdat(String besdat) {
        this.besdat = besdat;
    }

    public String getBesnr() {
        return besnr;
    }

    public void setBesnr(String besnr) {
        this.besnr = besnr;
    }

    public Integer getLnrfl() {
        return lnrfl;
    }

    public void setLnrfl(Integer lnrfl) {
        this.lnrfl = lnrfl;
    }

    public String getKenfl() {
        return kenfl;
    }

    public void setKenfl(String kenfl) {
        this.kenfl = kenfl;
    }

    public Integer getLnr() {
        return lnr;
    }

    public void setLnr(Integer lnr) {
        this.lnr = lnr;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public Integer getTextzeilen() {
        return textzeilen;
    }

    public void setTextzeilen(Integer textzeilen) {
        this.textzeilen = textzeilen;
    }

    public BigDecimal getEkpreis() {
        return ekpreis;
    }

    public void setEkpreis(BigDecimal ekpreis) {
        this.ekpreis = ekpreis;
    }

    public Integer getMenge() {
        return menge;
    }

    public void setMenge(Integer menge) {
        this.menge = menge;
    }

    public String getEkrenr() {
        return ekrenr;
    }

    public void setEkrenr(String ekrenr) {
        this.ekrenr = ekrenr;
    }

    public String getEkredat() {
        return ekredat;
    }

    public void setEkredat(String ekredat) {
        this.ekredat = ekredat;
    }

    public String getEkbemerkungen1() {
        return ekbemerkungen1;
    }

    public void setEkbemerkungen1(String ekbemerkungen1) {
        this.ekbemerkungen1 = ekbemerkungen1;
    }

    public String getEkbemerkungen2() {
        return ekbemerkungen2;
    }

    public void setEkbemerkungen2(String ekbemerkungen2) {
        this.ekbemerkungen2 = ekbemerkungen2;
    }

    public BigDecimal getEkwert() {
        return ekwert;
    }

    public void setEkwert(BigDecimal ekwert) {
        this.ekwert = ekwert;
    }

    public String getZuspercent() {
        return zuspercent;
    }

    public void setZuspercent(String zuspercent) {
        this.zuspercent = zuspercent;
    }

    public BigDecimal getVkwert() {
        return vkwert;
    }

    public void setVkwert(BigDecimal vkwert) {
        this.vkwert = vkwert;
    }

    public String getAufnr() {
        return aufnr;
    }

    public void setAufnr(String aufnr) {
        this.aufnr = aufnr;
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

    public String getSpl() {
        return spl;
    }

    public void setSpl(String spl) {
        this.spl = spl;
    }

    public String getAufdat() {
        return aufdat;
    }

    public void setAufdat(String aufdat) {
        this.aufdat = aufdat;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public String getErlgrp() {
        return erlgrp;
    }

    public void setErlgrp(String erlgrp) {
        this.erlgrp = erlgrp;
    }

    public String getRecnr() {
        return recnr;
    }

    public void setRecnr(String recnr) {
        this.recnr = recnr;
    }

    public String getRecdat() {
        return recdat;
    }

    public void setRecdat(String recdat) {
        this.recdat = recdat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSdpsJobUuid() {
        return sdpsJobUuid;
    }

    public void setSdpsJobUuid(String sdpsJobUuid) {
        this.sdpsJobUuid = sdpsJobUuid;
    }

    public String getSdpsFlaUuid() {
        return sdpsFlaUuid;
    }

    public void setSdpsFlaUuid(String sdpsFlaUuid) {
        this.sdpsFlaUuid = sdpsFlaUuid;
    }
}