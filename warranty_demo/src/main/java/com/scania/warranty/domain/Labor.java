package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSAHWPF")
public class Labor {
    
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
    private String cancellationFlag;
    
    @Column(name = "ANR", length = 5)
    private String jobNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "W/T", length = 1)
    private String workshopType;
    
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
    private Integer sortSequence;
    
    @Column(name = "LNR RZ", precision = 3, scale = 0)
    private Integer lineNumberSequence;
    
    @Column(name = "AG", length = 8)
    private String operationCode;
    
    @Column(name = "L.NR.", length = 3)
    private String lineNumber;
    
    @Column(name = "BEZ.", length = 40)
    private String description;
    
    @Column(name = "WERKSZEIT", precision = 5, scale = 2)
    private BigDecimal factoryTime;
    
    @Column(name = "AW-STUNDEN", precision = 5, scale = 2)
    private BigDecimal actualHours;
    
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
    private String businessCode;
    
    @Column(name = "V-SATZ", precision = 5, scale = 2)
    private BigDecimal calculationRate;
    
    @Column(name = "M-STUNDEN", precision = 5, scale = 2)
    private BigDecimal mechanicHours;
    
    @Column(name = "V-DM-NETTO", precision = 9, scale = 2)
    private BigDecimal calculationNet;
    
    @Column(name = "V-DM BRUTTO", precision = 9, scale = 2)
    private BigDecimal calculationGross;
    
    @Column(name = "V-STUNDEN", precision = 9, scale = 2)
    private BigDecimal calculationHours;
    
    @Column(name = "ZUSCHLAG", precision = 5, scale = 2)
    private BigDecimal surcharge;
    
    @Column(name = "RABATT", precision = 5, scale = 2)
    private BigDecimal discount;
    
    @Column(name = "KZ S/AW", length = 1)
    private String flagStandardActual;
    
    @Column(name = "KZ-MWST", length = 1)
    private String vatFlag;
    
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

    // Constructors
    public Labor() {
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

    public String getCancellationFlag() {
        return cancellationFlag;
    }

    public void setCancellationFlag(String cancellationFlag) {
        this.cancellationFlag = cancellationFlag;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getWorkshopType() {
        return workshopType;
    }

    public void setWorkshopType(String workshopType) {
        this.workshopType = workshopType;
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

    // Additional getters/setters for remaining 40 fields
}