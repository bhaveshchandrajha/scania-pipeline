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
    private String besDat;
    
    @Column(name = "BES-NR", length = 5)
    private String besNr;
    
    @Column(name = "LNR-FL", precision = 3, scale = 0)
    private Integer lnrFl;
    
    @Column(name = "KEN-FL", length = 8)
    private String kenFl;
    
    @Column(name = "LNR", precision = 3, scale = 0)
    private Integer lnr;
    
    @Column(name = "BESCHREIBUNG", length = 40)
    private String beschreibung;
    
    @Column(name = "TEXTZEILEN", precision = 3, scale = 0)
    private Integer textzeilen;
    
    @Column(name = "EK-PREIS", precision = 7, scale = 2)
    private BigDecimal ekPreis;
    
    @Column(name = "MENGE", precision = 5, scale = 0)
    private Integer menge;
    
    @Column(name = "EK-RENR", length = 10)
    private String ekRenr;
    
    @Column(name = "EK-REDAT", length = 8)
    private String ekRedat;
    
    @Column(name = "EK BEMERKUNGEN 1", length = 60)
    private String ekBemerkungen1;
    
    @Column(name = "EK BEMERKUNGEN 2", length = 60)
    private String ekBemerkungen2;
    
    @Column(name = "EK-WERT", precision = 9, scale = 2)
    private BigDecimal ekWert;
    
    @Column(name = "ZUS %", length = 3)
    private String zusPercent;
    
    @Column(name = "VK-WERT", precision = 9, scale = 2)
    private BigDecimal vkWert;
    
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
    private String erlGrp;
    
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
    
    public HSFLALF1() {
    }
    
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
    
    public String getBesDat() {
        return besDat;
    }
    
    public void setBesDat(String besDat) {
        this.besDat = besDat;
    }
    
    public String getBesNr() {
        return besNr;
    }
    
    public void setBesNr(String besNr) {
        this.besNr = besNr;
    }
    
    public Integer getLnrFl() {
        return lnrFl;
    }
    
    public void setLnrFl(Integer lnrFl) {
        this.lnrFl = lnrFl;
    }
    
    public String getKenFl() {
        return kenFl;
    }
    
    public void setKenFl(String kenFl) {
        this.kenFl = kenFl;
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
    
    public BigDecimal getEkPreis() {
        return ekPreis;
    }
    
    public void setEkPreis(BigDecimal ekPreis) {
        this.ekPreis = ekPreis;
    }
    
    public Integer getMenge() {
        return menge;
    }
    
    public void setMenge(Integer menge) {
        this.menge = menge;
    }
    
    public String getEkRenr() {
        return ekRenr;
    }
    
    public void setEkRenr(String ekRenr) {
        this.ekRenr = ekRenr;
    }
    
    public String getEkRedat() {
        return ekRedat;
    }
    
    public void setEkRedat(String ekRedat) {
        this.ekRedat = ekRedat;
    }
    
    public String getEkBemerkungen1() {
        return ekBemerkungen1;
    }
    
    public void setEkBemerkungen1(String ekBemerkungen1) {
        this.ekBemerkungen1 = ekBemerkungen1;
    }
    
    public String getEkBemerkungen2() {
        return ekBemerkungen2;
    }
    
    public void setEkBemerkungen2(String ekBemerkungen2) {
        this.ekBemerkungen2 = ekBemerkungen2;
    }
    
    public BigDecimal getEkWert() {
        return ekWert;
    }
    
    public void setEkWert(BigDecimal ekWert) {
        this.ekWert = ekWert;
    }
    
    public String getZusPercent() {
        return zusPercent;
    }
    
    public void setZusPercent(String zusPercent) {
        this.zusPercent = zusPercent;
    }
    
    public BigDecimal getVkWert() {
        return vkWert;
    }
    
    public void setVkWert(BigDecimal vkWert) {
        this.vkWert = vkWert;
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
    
    public String getErlGrp() {
        return erlGrp;
    }
    
    public void setErlGrp(String erlGrp) {
        this.erlGrp = erlGrp;
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