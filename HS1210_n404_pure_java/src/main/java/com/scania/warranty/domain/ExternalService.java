package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HSFLALF1")
public class ExternalService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PKZ", length = 3)
    private String companyCode;
    
    @Column(name = "BES-DAT", length = 8)
    private String orderDate;
    
    @Column(name = "BES-NR", length = 5)
    private String orderNumber;
    
    @Column(name = "LNR-FL", precision = 3, scale = 0)
    private Integer lineNumberExternal;
    
    @Column(name = "KEN-FL", length = 8)
    private String externalServiceId;
    
    @Column(name = "LNR", precision = 3, scale = 0)
    private Integer lineNumber;
    
    @Column(name = "BESCHREIBUNG", length = 40)
    private String description;
    
    @Column(name = "TEXTZEILEN", precision = 3, scale = 0)
    private Integer textLines;
    
    @Column(name = "EK-PREIS", precision = 7, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "MENGE", precision = 5, scale = 0)
    private Integer quantity;
    
    @Column(name = "EK-RENR", length = 10)
    private String purchaseInvoiceNumber;
    
    @Column(name = "EK-REDAT", length = 8)
    private String purchaseInvoiceDate;
    
    @Column(name = "EK BEMERKUNGEN 1", length = 60)
    private String purchaseRemarks1;
    
    @Column(name = "EK BEMERKUNGEN 2", length = 60)
    private String purchaseRemarks2;
    
    @Column(name = "EK-WERT", precision = 9, scale = 2)
    private BigDecimal purchaseValue;
    
    @Column(name = "ZUS %", length = 3)
    private String surchargePercent;
    
    @Column(name = "VK-WERT", precision = 9, scale = 2)
    private BigDecimal salesValue;
    
    @Column(name = "AUFNR", length = 5)
    private String jobNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "WT", length = 1)
    private String workshopType;
    
    @Column(name = "SPL", length = 2)
    private String split;
    
    @Column(name = "AUFDAT", length = 8)
    private String jobDate;
    
    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer position;
    
    @Column(name = "ERL-GRP", length = 2)
    private String revenueGroup;
    
    @Column(name = "RECNR", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RECDAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "SDPS JOB UUID", length = 40)
    private String sdpsJobUuid;
    
    @Column(name = "SDPS FLA UUID", length = 40)
    private String sdpsFlaUuid;

    // Constructors
    public ExternalService() {
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

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getLineNumberExternal() {
        return lineNumberExternal;
    }

    public void setLineNumberExternal(Integer lineNumberExternal) {
        this.lineNumberExternal = lineNumberExternal;
    }

    public String getExternalServiceId() {
        return externalServiceId;
    }

    public void setExternalServiceId(String externalServiceId) {
        this.externalServiceId = externalServiceId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTextLines() {
        return textLines;
    }

    public void setTextLines(Integer textLines) {
        this.textLines = textLines;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPurchaseInvoiceNumber() {
        return purchaseInvoiceNumber;
    }

    public void setPurchaseInvoiceNumber(String purchaseInvoiceNumber) {
        this.purchaseInvoiceNumber = purchaseInvoiceNumber;
    }

    public String getPurchaseInvoiceDate() {
        return purchaseInvoiceDate;
    }

    public void setPurchaseInvoiceDate(String purchaseInvoiceDate) {
        this.purchaseInvoiceDate = purchaseInvoiceDate;
    }

    public String getPurchaseRemarks1() {
        return purchaseRemarks1;
    }

    public void setPurchaseRemarks1(String purchaseRemarks1) {
        this.purchaseRemarks1 = purchaseRemarks1;
    }

    public String getPurchaseRemarks2() {
        return purchaseRemarks2;
    }

    public void setPurchaseRemarks2(String purchaseRemarks2) {
        this.purchaseRemarks2 = purchaseRemarks2;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public String getSurchargePercent() {
        return surchargePercent;
    }

    public void setSurchargePercent(String surchargePercent) {
        this.surchargePercent = surchargePercent;
    }

    public BigDecimal getSalesValue() {
        return salesValue;
    }

    public void setSalesValue(BigDecimal salesValue) {
        this.salesValue = salesValue;
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

    public String getJobDate() {
        return jobDate;
    }

    public void setJobDate(String jobDate) {
        this.jobDate = jobDate;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getRevenueGroup() {
        return revenueGroup;
    }

    public void setRevenueGroup(String revenueGroup) {
        this.revenueGroup = revenueGroup;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
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