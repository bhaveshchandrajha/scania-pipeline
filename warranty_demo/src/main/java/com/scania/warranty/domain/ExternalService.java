/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * JPA entity for external service line items (HSFLALF1).
 */
@Entity
@Table(name = "HSFLALF1")
public class ExternalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PKZ", length = 3, nullable = false)
    private String pkz; // @rpg-trace: schema

    @Column(name = "BES-DAT", length = 8, nullable = false)
    private String besDat; // @rpg-trace: schema

    @Column(name = "BES-NR", length = 5, nullable = false)
    private String besNr; // @rpg-trace: schema

    @Column(name = "LNR-FL", precision = 3, scale = 0, nullable = false)
    private Integer lnrFl; // @rpg-trace: schema

    @Column(name = "KEN-FL", length = 8, nullable = false)
    private String kenFl; // @rpg-trace: schema

    @Column(name = "LNR", precision = 3, scale = 0, nullable = false)
    private Integer lnr; // @rpg-trace: schema

    @Column(name = "BESCHREIBUNG", length = 40, nullable = false)
    private String beschreibung; // @rpg-trace: schema

    @Column(name = "TEXTZEILEN", precision = 3, scale = 0, nullable = false)
    private Integer textzeilen; // @rpg-trace: schema

    @Column(name = "EK-PREIS", precision = 7, scale = 2, nullable = false)
    private BigDecimal ekPreis; // @rpg-trace: schema

    @Column(name = "MENGE", precision = 5, scale = 0, nullable = false)
    private Integer menge; // @rpg-trace: schema

    @Column(name = "EK-RENR", length = 10, nullable = false)
    private String ekRenr; // @rpg-trace: schema

    @Column(name = "EK-REDAT", length = 8, nullable = false)
    private String ekRedat; // @rpg-trace: schema

    @Column(name = "EK BEMERKUNGEN 1", length = 60, nullable = false)
    private String ekBemerkungen1; // @rpg-trace: schema

    @Column(name = "EK BEMERKUNGEN 2", length = 60, nullable = false)
    private String ekBemerkungen2; // @rpg-trace: schema

    @Column(name = "EK-WERT", precision = 9, scale = 2, nullable = false)
    private BigDecimal ekWert; // @rpg-trace: schema

    @Column(name = "ZUS %", length = 3, nullable = false)
    private String zusPercent; // @rpg-trace: schema

    @Column(name = "VK-WERT", precision = 9, scale = 2, nullable = false)
    private BigDecimal vkWert; // @rpg-trace: schema

    @Column(name = "AUFNR", length = 5, nullable = false)
    private String aufnr; // @rpg-trace: schema

    @Column(name = "BEREI", length = 1, nullable = false)
    private String berei; // @rpg-trace: schema

    @Column(name = "WT", length = 1, nullable = false)
    private String wt; // @rpg-trace: schema

    @Column(name = "SPL", length = 2, nullable = false)
    private String spl; // @rpg-trace: schema

    @Column(name = "AUFDAT", length = 8, nullable = false)
    private String aufdat; // @rpg-trace: schema

    @Column(name = "POS.", precision = 3, scale = 0, nullable = false)
    private Integer pos; // @rpg-trace: schema

    @Column(name = "ERL-GRP", length = 2, nullable = false)
    private String erlGrp; // @rpg-trace: schema

    @Column(name = "RECNR", length = 5, nullable = false)
    private String recnr; // @rpg-trace: schema

    @Column(name = "RECDAT", length = 8, nullable = false)
    private String recdat; // @rpg-trace: schema

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status; // @rpg-trace: schema

    @Column(name = "SDPS JOB UUID", length = 40, nullable = false)
    private String sdpsJobUuid; // @rpg-trace: schema

    @Column(name = "SDPS FLA UUID", length = 40, nullable = false)
    private String sdpsFlaUuid; // @rpg-trace: schema

    public ExternalService() {
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