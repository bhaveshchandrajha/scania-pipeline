/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSGPSLF3")
@IdClass(ClaimPositionId.class)
public class ClaimPosition {
    
    @Id
    @Column(name = "KUERZEL", length = 3, nullable = false)
    private String kuerzel; // @rpg-trace: schema
    
    @Id
    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "FEHLER-NR.", length = 2, nullable = false)
    private String fehlerNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "FOLGE-NR.", length = 2, nullable = false)
    private String folgeNr; // @rpg-trace: schema
    
    @Id
    @Column(name = "ZEILE", precision = 3, scale = 0, nullable = false)
    private Integer zeile; // @rpg-trace: schema
    
    @Id
    @Column(name = "AUFTEILUNG", precision = 3, scale = 0, nullable = false)
    private Integer aufteilung; // @rpg-trace: schema
    
    @Column(name = "SATZART", length = 3, nullable = false)
    private String satzart; // @rpg-trace: schema
    
    @Column(name = "NR.", length = 18, nullable = false)
    private String nr; // @rpg-trace: schema
    
    @Column(name = "MENGE", precision = 5, scale = 0, nullable = false)
    private Integer menge; // @rpg-trace: schema
    
    @Column(name = "WERT", precision = 11, scale = 2, nullable = false)
    private BigDecimal wert; // @rpg-trace: schema
    
    @Column(name = "STEUER-CODE", length = 40, nullable = false)
    private String steuerCode; // @rpg-trace: schema
    
    @Column(name = "HAUPTGRUPPE", length = 2, nullable = false)
    private String hauptgruppe; // @rpg-trace: schema
    
    @Column(name = "ZEIT", precision = 3, scale = 1, nullable = false)
    private BigDecimal zeit; // @rpg-trace: schema
    
    @Column(name = "GRUND", precision = 4, scale = 0, nullable = false)
    private Integer grund; // @rpg-trace: schema
    
    @Column(name = "VERGÜTUNG", precision = 3, scale = 0, nullable = false)
    private Integer verguetung; // @rpg-trace: schema
    
    @Column(name = "MANUELL", length = 1, nullable = false)
    private String manuell; // @rpg-trace: schema
    
    @Column(name = "KAMPAGNE", precision = 8, scale = 0, nullable = false)
    private Integer kampagne; // @rpg-trace: schema
    
    @Column(name = "POS.-NR.", precision = 3, scale = 0, nullable = false)
    private Integer posNr; // @rpg-trace: schema
    
    @Column(name = "RESULTCODE", length = 2, nullable = false)
    private String resultcode; // @rpg-trace: schema
    
    @Column(name = "CODE TYPE", length = 2, nullable = false)
    private String codeType; // @rpg-trace: schema
    
    @Column(name = "CODE ID", length = 5, nullable = false)
    private String codeId; // @rpg-trace: schema
    
    @Column(name = "COMPFAC.", precision = 3, scale = 0, nullable = false)
    private Integer compfac; // @rpg-trace: schema
    
    @Column(name = "GROSSPRICE", precision = 13, scale = 2, nullable = false)
    private BigDecimal grossprice; // @rpg-trace: schema
    
    @Column(name = "DISCOUNT", precision = 5, scale = 2, nullable = false)
    private BigDecimal discount; // @rpg-trace: schema
    
    @Column(name = "COMPAMOUNT", precision = 15, scale = 2, nullable = false)
    private BigDecimal compamount; // @rpg-trace: schema
    
    @Column(name = "COMPQTY", precision = 7, scale = 0, nullable = false)
    private Integer compqty; // @rpg-trace: schema
    
    @Column(name = "TYPE", length = 5, nullable = false)
    private String type; // @rpg-trace: schema
    
    @Column(name = "MIA STATUS", length = 1, nullable = false)
    private String miaStatus; // @rpg-trace: schema
    
    @Column(name = "CATEGORYS", length = 2, nullable = false)
    private String categorys; // @rpg-trace: schema
    
    @Column(name = "TEXT", length = 2000, nullable = false)
    private String text; // @rpg-trace: schema
    
    public ClaimPosition() {
    }
    
    // Getters and Setters
    public String getKuerzel() {
        return kuerzel;
    }
    
    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
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
    
    public Integer getZeile() {
        return zeile;
    }
    
    public void setZeile(Integer zeile) {
        this.zeile = zeile;
    }
    
    public Integer getAufteilung() {
        return aufteilung;
    }
    
    public void setAufteilung(Integer aufteilung) {
        this.aufteilung = aufteilung;
    }
    
    public String getSatzart() {
        return satzart;
    }
    
    public void setSatzart(String satzart) {
        this.satzart = satzart;
    }
    
    public String getNr() {
        return nr;
    }
    
    public void setNr(String nr) {
        this.nr = nr;
    }
    
    public Integer getMenge() {
        return menge;
    }
    
    public void setMenge(Integer menge) {
        this.menge = menge;
    }
    
    public BigDecimal getWert() {
        return wert;
    }
    
    public void setWert(BigDecimal wert) {
        this.wert = wert;
    }
    
    public String getSteuerCode() {
        return steuerCode;
    }
    
    public void setSteuerCode(String steuerCode) {
        this.steuerCode = steuerCode;
    }
    
    public String getHauptgruppe() {
        return hauptgruppe;
    }
    
    public void setHauptgruppe(String hauptgruppe) {
        this.hauptgruppe = hauptgruppe;
    }
    
    public BigDecimal getZeit() {
        return zeit;
    }
    
    public void setZeit(BigDecimal zeit) {
        this.zeit = zeit;
    }
    
    public Integer getGrund() {
        return grund;
    }
    
    public void setGrund(Integer grund) {
        this.grund = grund;
    }
    
    public Integer getVerguetung() {
        return verguetung;
    }
    
    public void setVerguetung(Integer verguetung) {
        this.verguetung = verguetung;
    }
    
    public String getManuell() {
        return manuell;
    }
    
    public void setManuell(String manuell) {
        this.manuell = manuell;
    }
    
    public Integer getKampagne() {
        return kampagne;
    }
    
    public void setKampagne(Integer kampagne) {
        this.kampagne = kampagne;
    }
    
    public Integer getPosNr() {
        return posNr;
    }
    
    public void setPosNr(Integer posNr) {
        this.posNr = posNr;
    }
    
    public String getResultcode() {
        return resultcode;
    }
    
    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }
    
    public String getCodeType() {
        return codeType;
    }
    
    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }
    
    public String getCodeId() {
        return codeId;
    }
    
    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }
    
    public Integer getCompfac() {
        return compfac;
    }
    
    public void setCompfac(Integer compfac) {
        this.compfac = compfac;
    }
    
    public BigDecimal getGrossprice() {
        return grossprice;
    }
    
    public void setGrossprice(BigDecimal grossprice) {
        this.grossprice = grossprice;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    public BigDecimal getCompamount() {
        return compamount;
    }
    
    public void setCompamount(BigDecimal compamount) {
        this.compamount = compamount;
    }
    
    public Integer getCompqty() {
        return compqty;
    }
    
    public void setCompqty(Integer compqty) {
        this.compqty = compqty;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMiaStatus() {
        return miaStatus;
    }
    
    public void setMiaStatus(String miaStatus) {
        this.miaStatus = miaStatus;
    }
    
    public String getCategorys() {
        return categorys;
    }
    
    public void setCategorys(String categorys) {
        this.categorys = categorys;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getRecordType() {
        return satzart;
    }
    
    public String getResultCode() {
        return resultcode;
    }
}