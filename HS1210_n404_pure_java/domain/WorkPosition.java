package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSAHWPF")
public class WorkPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String companyCode;
    
    @Column(name = "RNR", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RG-NR. 10A", length = 10)
    private String invoiceNumber10;
    
    @Column(name = "RDAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "KZ S", length = 1)
    private String indicatorS;
    
    @Column(name = "ANR", length = 5)
    private String orderNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "W/T", length = 1)
    private String workshopCounter;
    
    @Column(name = "SPLITT", length = 2)
    private String split;
    
    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer position;
    
    @Column(name = "EC", length = 2)
    private String entryCode;
    
    @Column(name = "LNR PAK", precision = 3, scale = 0)
    private Integer lineNumberPackage;
    
    @Column(name = "PAKET-NR.", length = 8)
    private String packageNumber;
    
    @Column(name = "SORT RZ", precision = 3, scale = 0)
    private Integer sortInvoice;
    
    @Column(name = "LNR RZ", precision = 3, scale = 0)
    private Integer lineNumberInvoice;
    
    @Column(name = "AG", length = 8)
    private String operationCode;
    
    @Column(name = "L.NR.", length = 3)
    private String lineNumber;
    
    @Column(name = "BEZ.", length = 40)
    private String description;
    
    @Column(name = "WERKSZEIT", precision = 5, scale = 2)
    private BigDecimal factoryTime;
    
    @Column(name = "AW-STUNDEN", precision = 5, scale = 2)
    private BigDecimal actualWorkHours;
    
    @Column(name = "ZE", precision = 5, scale = 0)
    private Integer timeUnits;
    
    @Column(name = "PE", precision = 5, scale = 0)
    private Integer priceUnits;
    
    @Column(name = "SATZ-PE", precision = 5, scale = 2)
    private BigDecimal ratePriceUnit;
    
    @Column(name = "GEW-ZE", length = 1)
    private String weightedTime;
    
    @Column(name = "PREIS", precision = 9, scale = 2)
    private BigDecimal price;
    
    @Column(name = "MONTEUR", length = 3)
    private String mechanic;
    
    @Column(name = "BC", length = 2)
    private String billingCode;
    
    @Column(name = "V-SATZ", precision = 5, scale = 2)
    private BigDecimal calculationRate;
    
    @Column(name = "M-STUNDEN", precision = 5, scale = 2)
    private BigDecimal mechanicHours;
    
    @Column(name = "V-DM-NETTO", precision = 9, scale = 2)
    private BigDecimal calculationNetAmount;
    
    @Column(name = "V-DM BRUTTO", precision = 9, scale = 2)
    private BigDecimal calculationGrossAmount;
    
    @Column(name = "V-STUNDEN", precision = 9, scale = 2)
    private BigDecimal calculationHours;
    
    @Column(name = "ZUSCHLAG", precision = 5, scale = 2)
    private BigDecimal surcharge;
    
    @Column(name = "RABATT", precision = 5, scale = 2)
    private BigDecimal discount;
    
    @Column(name = "KZ S/AW", length = 1)
    private String indicatorSAw;
    
    @Column(name = "KZ-MWST", length = 1)
    private String indicatorVat;
    
    @Column(name = "VERDICHTEN", length = 1)
    private String consolidate;
    
    @Column(name = "TXT-KEY", length = 3)
    private String textKey;
    
    @Column(name = "RG BRUTTO", precision = 9, scale = 2)
    private BigDecimal invoiceGross;
    
    @Column(name = "RG RABATT", precision = 9, scale = 2)
    private BigDecimal invoiceDiscount;
    
    @Column(name = "RG NETTO", precision = 9, scale = 2)
    private BigDecimal invoiceNet;
    
    @Column(name = "KEN.RE2SUM", length = 1)
    private String identifierInvoiceToSum;
    
    @Column(name = "URSPR-FAK/H MON", precision = 5, scale = 2)
    private BigDecimal originalFactorHourMechanic;
    
    @Column(name = "URSPR-NETTO MON", precision = 9, scale = 2)
    private BigDecimal originalNetMechanic;
    
    @Column(name = "EINSTANDSPREIS", precision = 9, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "EPS NAME", length = 20)
    private String epsName;
    
    @Column(name = "EPS MINDERUNG %", precision = 5, scale = 2)
    private BigDecimal epsReductionPercent;
    
    @Column(name = "VARIANTE", length = 500)
    private String variant;
    
    @Column(name = "ARBEITSBESCHREIBUNG", length = 2000)
    private String workDescription;
    
    @Column(name = "RECHNUNGSTEXT", length = 2000)
    private String invoiceText;

    public WorkPosition() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceNumber10() {
        return invoiceNumber10;
    }

    public void setInvoiceNumber10(String invoiceNumber10) {
        this.invoiceNumber10 = invoiceNumber10;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getIndicatorS() {
        return indicatorS;
    }

    public void setIndicatorS(String indicatorS) {
        this.indicatorS = indicatorS;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getWorkshopCounter() {
        return workshopCounter;
    }

    public void setWorkshopCounter(String workshopCounter) {
        this.workshopCounter = workshopCounter;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getEntryCode() {
        return entryCode;
    }

    public void setEntryCode(String entryCode) {
        this.entryCode = entryCode;
    }

    public Integer getLineNumberPackage() {
        return lineNumberPackage;
    }

    public void setLineNumberPackage(Integer lineNumberPackage) {
        this.lineNumberPackage = lineNumberPackage;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public Integer getSortInvoice() {
        return sortInvoice;
    }

    public void setSortInvoice(Integer sortInvoice) {
        this.sortInvoice = sortInvoice;
    }

    public Integer getLineNumberInvoice() {
        return lineNumberInvoice;
    }

    public void setLineNumberInvoice(Integer lineNumberInvoice) {
        this.lineNumberInvoice = lineNumberInvoice;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFactoryTime() {
        return factoryTime;
    }

    public void setFactoryTime(BigDecimal factoryTime) {
        this.factoryTime = factoryTime;
    }

    public BigDecimal getActualWorkHours() {
        return actualWorkHours;
    }

    public void setActualWorkHours(BigDecimal actualWorkHours) {
        this.actualWorkHours = actualWorkHours;
    }

    public Integer getTimeUnits() {
        return timeUnits;
    }

    public void setTimeUnits(Integer timeUnits) {
        this.timeUnits = timeUnits;
    }

    public Integer getPriceUnits() {
        return priceUnits;
    }

    public void setPriceUnits(Integer priceUnits) {
        this.priceUnits = priceUnits;
    }

    public BigDecimal getRatePriceUnit() {
        return ratePriceUnit;
    }

    public void setRatePriceUnit(BigDecimal ratePriceUnit) {
        this.ratePriceUnit = ratePriceUnit;
    }

    public String getWeightedTime() {
        return weightedTime;
    }

    public void setWeightedTime(String weightedTime) {
        this.weightedTime = weightedTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getMechanic() {
        return mechanic;
    }

    public void setMechanic(String mechanic) {
        this.mechanic = mechanic;
    }

    public String getBillingCode() {
        return billingCode;
    }

    public void setBillingCode(String billingCode) {
        this.billingCode = billingCode;
    }

    public BigDecimal getCalculationRate() {
        return calculationRate;
    }

    public void setCalculationRate(BigDecimal calculationRate) {
        this.calculationRate = calculationRate;
    }

    public BigDecimal getMechanicHours() {
        return mechanicHours;
    }

    public void setMechanicHours(BigDecimal mechanicHours) {
        this.mechanicHours = mechanicHours;
    }

    public BigDecimal getCalculationNetAmount() {
        return calculationNetAmount;
    }

    public void setCalculationNetAmount(BigDecimal calculationNetAmount) {
        this.calculationNetAmount = calculationNetAmount;
    }

    public BigDecimal getCalculationGrossAmount() {
        return calculationGrossAmount;
    }

    public void setCalculationGrossAmount(BigDecimal calculationGrossAmount) {
        this.calculationGrossAmount = calculationGrossAmount;
    }

    public BigDecimal getCalculationHours() {
        return calculationHours;
    }

    public void setCalculationHours(BigDecimal calculationHours) {
        this.calculationHours = calculationHours;
    }

    public BigDecimal getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getIndicatorSAw() {
        return indicatorSAw;
    }

    public void setIndicatorSAw(String indicatorSAw) {
        this.indicatorSAw = indicatorSAw;
    }

    public String getIndicatorVat() {
        return indicatorVat;
    }

    public void setIndicatorVat(String indicatorVat) {
        this.indicatorVat = indicatorVat;
    }

    public String getConsolidate() {
        return consolidate;
    }

    public void setConsolidate(String consolidate) {
        this.consolidate = consolidate;
    }

    public String getTextKey() {
        return textKey;
    }

    public void setTextKey(String textKey) {
        this.textKey = textKey;
    }

    public BigDecimal getInvoiceGross() {
        return invoiceGross;
    }

    public void setInvoiceGross(BigDecimal invoiceGross) {
        this.invoiceGross = invoiceGross;
    }

    public BigDecimal getInvoiceDiscount() {
        return invoiceDiscount;
    }

    public void setInvoiceDiscount(BigDecimal invoiceDiscount) {
        this.invoiceDiscount = invoiceDiscount;
    }

    public BigDecimal getInvoiceNet() {
        return invoiceNet;
    }

    public void setInvoiceNet(BigDecimal invoiceNet) {
        this.invoiceNet = invoiceNet;
    }

    public String getIdentifierInvoiceToSum() {
        return identifierInvoiceToSum;
    }

    public void setIdentifierInvoiceToSum(String identifierInvoiceToSum) {
        this.identifierInvoiceToSum = identifierInvoiceToSum;
    }

    public BigDecimal getOriginalFactorHourMechanic() {
        return originalFactorHourMechanic;
    }

    public void setOriginalFactorHourMechanic(BigDecimal originalFactorHourMechanic) {
        this.originalFactorHourMechanic = originalFactorHourMechanic;
    }

    public BigDecimal getOriginalNetMechanic() {
        return originalNetMechanic;
    }

    public void setOriginalNetMechanic(BigDecimal originalNetMechanic) {
        this.originalNetMechanic = originalNetMechanic;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public String getEpsName() {
        return epsName;
    }

    public void setEpsName(String epsName) {
        this.epsName = epsName;
    }

    public BigDecimal getEpsReductionPercent() {
        return epsReductionPercent;
    }

    public void setEpsReductionPercent(BigDecimal epsReductionPercent) {
        this.epsReductionPercent = epsReductionPercent;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public String getInvoiceText() {
        return invoiceText;
    }

    public void setInvoiceText(String invoiceText) {
        this.invoiceText = invoiceText;
    }
}