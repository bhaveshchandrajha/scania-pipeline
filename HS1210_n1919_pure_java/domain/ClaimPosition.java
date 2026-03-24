package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSGPSPF")
public class ClaimPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "KUERZEL", length = 3, nullable = false)
    private String kuerzel;

    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNumber;

    @Column(name = "FEHLER-NR.", length = 2, nullable = false)
    private String errorNumber;

    @Column(name = "FOLGE-NR.", length = 2, nullable = false)
    private String sequenceNumber;

    @Column(name = "ZEILE", precision = 3, scale = 0, nullable = false)
    private Integer lineNumber;

    @Column(name = "AUFTEILUNG", precision = 3, scale = 0, nullable = false)
    private Integer distribution;

    @Column(name = "SATZART", length = 3, nullable = false)
    private String recordType;

    @Column(name = "NR.", length = 18, nullable = false)
    private String number;

    @Column(name = "MENGE", precision = 5, scale = 0, nullable = false)
    private Integer quantity;

    @Column(name = "WERT", precision = 11, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "STEUER-CODE", length = 40, nullable = false)
    private String taxCode;

    @Column(name = "HAUPTGRUPPE", length = 2, nullable = false)
    private String mainGroup;

    @Column(name = "ZEIT", precision = 3, scale = 1, nullable = false)
    private BigDecimal time;

    @Column(name = "GRUND", precision = 4, scale = 0, nullable = false)
    private Integer reason;

    @Column(name = "VERGÜTUNG", precision = 3, scale = 0, nullable = false)
    private Integer compensation;

    @Column(name = "MANUELL", length = 1, nullable = false)
    private String manual;

    @Column(name = "KAMPAGNE", precision = 8, scale = 0, nullable = false)
    private Long campaign;

    @Column(name = "POS.-NR.", precision = 3, scale = 0, nullable = false)
    private Integer positionNumber;

    @Column(name = "RESULTCODE", length = 2, nullable = false)
    private String resultCode;

    @Column(name = "CODE TYPE", length = 2, nullable = false)
    private String codeType;

    @Column(name = "CODE ID", length = 5, nullable = false)
    private String codeId;

    @Column(name = "COMPFAC.", precision = 3, scale = 0, nullable = false)
    private Integer compensationFactor;

    @Column(name = "GROSSPRICE", precision = 13, scale = 2, nullable = false)
    private BigDecimal grossPrice;

    @Column(name = "DISCOUNT", precision = 5, scale = 2, nullable = false)
    private BigDecimal discount;

    @Column(name = "COMPAMOUNT", precision = 15, scale = 2, nullable = false)
    private BigDecimal compensationAmount;

    @Column(name = "COMPQTY", precision = 7, scale = 0, nullable = false)
    private Integer compensationQuantity;

    @Column(name = "TYPE", length = 5, nullable = false)
    private String type;

    @Column(name = "MIA STATUS", length = 1, nullable = false)
    private String miaStatus;

    @Column(name = "CATEGORYS", length = 2, nullable = false)
    private String categories;

    @Column(name = "TEXT", length = 2000, nullable = false)
    private String text;

    public ClaimPosition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKuerzel() {
        return kuerzel;
    }

    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getErrorNumber() {
        return errorNumber;
    }

    public void setErrorNumber(String errorNumber) {
        this.errorNumber = errorNumber;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getDistribution() {
        return distribution;
    }

    public void setDistribution(Integer distribution) {
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
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

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getCompensation() {
        return compensation;
    }

    public void setCompensation(Integer compensation) {
        this.compensation = compensation;
    }

    public String getManual() {
        return manual;
    }

    public void setManual(String manual) {
        this.manual = manual;
    }

    public Long getCampaign() {
        return campaign;
    }

    public void setCampaign(Long campaign) {
        this.campaign = campaign;
    }

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
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

    public Integer getCompensationFactor() {
        return compensationFactor;
    }

    public void setCompensationFactor(Integer compensationFactor) {
        this.compensationFactor = compensationFactor;
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

    public BigDecimal getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(BigDecimal compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    public Integer getCompensationQuantity() {
        return compensationQuantity;
    }

    public void setCompensationQuantity(Integer compensationQuantity) {
        this.compensationQuantity = compensationQuantity;
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