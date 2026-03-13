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
@IdClass(ClaimPositionLineId.class)
public class ClaimPositionLine {
    
    @Id
    @Column(name = "KUERZEL", length = 3, nullable = false)
    private String abbreviation; // @rpg-trace: schema
    
    @Id
    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "FEHLER-NR.", length = 2, nullable = false)
    private String failureNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "FOLGE-NR.", length = 2, nullable = false)
    private String sequenceNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "ZEILE", precision = 3, scale = 0, nullable = false)
    private BigDecimal lineNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "AUFTEILUNG", precision = 3, scale = 0, nullable = false)
    private BigDecimal distribution; // @rpg-trace: schema
    
    @Column(name = "SATZART", length = 3, nullable = false)
    private String recordType; // @rpg-trace: schema
    
    @Column(name = "NR.", length = 18, nullable = false)
    private String number; // @rpg-trace: schema
    
    @Column(name = "MENGE", precision = 5, scale = 0, nullable = false)
    private BigDecimal quantity; // @rpg-trace: schema
    
    @Column(name = "WERT", precision = 11, scale = 2, nullable = false)
    private BigDecimal value; // @rpg-trace: schema
    
    @Column(name = "STEUER-CODE", length = 40, nullable = false)
    private String taxCode; // @rpg-trace: schema
    
    @Column(name = "HAUPTGRUPPE", length = 2, nullable = false)
    private String mainGroup; // @rpg-trace: schema
    
    @Column(name = "ZEIT", precision = 3, scale = 1, nullable = false)
    private BigDecimal time; // @rpg-trace: schema
    
    @Column(name = "GRUND", precision = 4, scale = 0, nullable = false)
    private BigDecimal reason; // @rpg-trace: schema
    
    @Column(name = "VERGÜTUNG", precision = 3, scale = 0, nullable = false)
    private BigDecimal compensation; // @rpg-trace: schema
    
    @Column(name = "MANUELL", length = 1, nullable = false)
    private String manual; // @rpg-trace: schema
    
    @Column(name = "KAMPAGNE", precision = 8, scale = 0, nullable = false)
    private BigDecimal campaign; // @rpg-trace: schema
    
    @Column(name = "POS.-NR.", precision = 3, scale = 0, nullable = false)
    private BigDecimal positionNumber; // @rpg-trace: schema
    
    @Column(name = "RESULTCODE", length = 2, nullable = false)
    private String resultCode; // @rpg-trace: schema
    
    @Column(name = "CODE TYPE", length = 2, nullable = false)
    private String codeType; // @rpg-trace: schema
    
    @Column(name = "CODE ID", length = 5, nullable = false)
    private String codeId; // @rpg-trace: schema
    
    @Column(name = "COMPFAC.", precision = 3, scale = 0, nullable = false)
    private BigDecimal compFactor; // @rpg-trace: schema
    
    @Column(name = "GROSSPRICE", precision = 13, scale = 2, nullable = false)
    private BigDecimal grossPrice; // @rpg-trace: schema
    
    @Column(name = "DISCOUNT", precision = 5, scale = 2, nullable = false)
    private BigDecimal discount; // @rpg-trace: schema
    
    @Column(name = "COMPAMOUNT", precision = 15, scale = 2, nullable = false)
    private BigDecimal compAmount; // @rpg-trace: schema
    
    @Column(name = "COMPQTY", precision = 7, scale = 0, nullable = false)
    private BigDecimal compQuantity; // @rpg-trace: schema
    
    @Column(name = "TYPE", length = 5, nullable = false)
    private String type; // @rpg-trace: schema
    
    @Column(name = "MIA STATUS", length = 1, nullable = false)
    private String miaStatus; // @rpg-trace: schema
    
    @Column(name = "CATEGORYS", length = 2, nullable = false)
    private String categories; // @rpg-trace: schema
    
    @Column(name = "TEXT", length = 2000, nullable = false)
    private String text; // @rpg-trace: schema
    
    public ClaimPositionLine() {
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }
    
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
    public String getClaimNumber() {
        return claimNumber;
    }
    
    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }
    
    public String getFailureNumber() {
        return failureNumber;
    }
    
    public void setFailureNumber(String failureNumber) {
        this.failureNumber = failureNumber;
    }
    
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public BigDecimal getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(BigDecimal lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public BigDecimal getDistribution() {
        return distribution;
    }
    
    public void setDistribution(BigDecimal distribution) {
        this.distribution = distribution;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public String getTaxCode() {
        return taxCode;
    }
    
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }
    
    public String getMainGroup() {
        return mainGroup;
    }
    
    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }
    
    public BigDecimal getTime() {
        return time;
    }
    
    public void setTime(BigDecimal time) {
        this.time = time;
    }
    
    public BigDecimal getReason() {
        return reason;
    }
    
    public void setReason(BigDecimal reason) {
        this.reason = reason;
    }
    
    public BigDecimal getCompensation() {
        return compensation;
    }
    
    public void setCompensation(BigDecimal compensation) {
        this.compensation = compensation;
    }
    
    public String getManual() {
        return manual;
    }
    
    public void setManual(String manual) {
        this.manual = manual;
    }
    
    public BigDecimal getCampaign() {
        return campaign;
    }
    
    public void setCampaign(BigDecimal campaign) {
        this.campaign = campaign;
    }
    
    public BigDecimal getPositionNumber() {
        return positionNumber;
    }
    
    public void setPositionNumber(BigDecimal positionNumber) {
        this.positionNumber = positionNumber;
    }
    
    public String getResultCode() {
        return resultCode;
    }
    
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
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
    
    public BigDecimal getCompFactor() {
        return compFactor;
    }
    
    public void setCompFactor(BigDecimal compFactor) {
        this.compFactor = compFactor;
    }
    
    public BigDecimal getGrossPrice() {
        return grossPrice;
    }
    
    public void setGrossPrice(BigDecimal grossPrice) {
        this.grossPrice = grossPrice;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    public BigDecimal getCompAmount() {
        return compAmount;
    }
    
    public void setCompAmount(BigDecimal compAmount) {
        this.compAmount = compAmount;
    }
    
    public BigDecimal getCompQuantity() {
        return compQuantity;
    }
    
    public void setCompQuantity(BigDecimal compQuantity) {
        this.compQuantity = compQuantity;
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
    
    public String getCategories() {
        return categories;
    }
    
    public void setCategories(String categories) {
        this.categories = categories;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}