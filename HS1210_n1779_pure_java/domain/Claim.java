package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG71PF")
public class Claim {

    @Id
    @Column(name = "PAKZ", length = 3, nullable = false)
    private String companyCode;

    @Id
    @Column(name = "RECH.-NR.", length = 5, nullable = false)
    private String invoiceNumber;

    @Column(name = "RECH.-DATUM", length = 8, nullable = false)
    private String invoiceDate;

    @Column(name = "AUFTRAGS-NR.", length = 5, nullable = false)
    private String orderNumber;

    @Column(name = "WETE", length = 1, nullable = false)
    private String wete;

    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNumber;

    @Column(name = "CHASSIS-NR.", length = 7, nullable = false)
    private String chassisNumber;

    @Column(name = "KENNZEICHEN", length = 10, nullable = false)
    private String licensePlate;

    @Column(name = "ZUL.-DATUM", precision = 8, scale = 0, nullable = false)
    private Integer registrationDate;

    @Column(name = "REP.-DATUM", precision = 8, scale = 0, nullable = false)
    private Integer repairDate;

    @Column(name = "KM-STAND", precision = 3, scale = 0, nullable = false)
    private Integer mileage;

    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0, nullable = false)
    private Integer productType;

    @Column(name = "ANHANG", length = 1, nullable = false)
    private String attachment;

    @Column(name = "AUSL#NDER", length = 1, nullable = false)
    private String foreigner;

    @Column(name = "KD-NR.", length = 6, nullable = false)
    private String customerNumber;

    @Column(name = "KD-NAME", length = 30, nullable = false)
    private String customerName;

    @Column(name = "CLAIM-NR. SDE", length = 8, nullable = false)
    private String claimNumberSde;

    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0, nullable = false)
    private Integer statusCodeSde;

    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0, nullable = false)
    private Integer errorCount;

    @Column(name = "BEREICH", length = 1, nullable = false)
    private String scope;

    @Column(name = "AUF.NR.", length = 10, nullable = false)
    private String aufNr;

    // Constructors
    public Claim() {
    }

    // Getters and Setters
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

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getWete() {
        return wete;
    }

    public void setWete(String wete) {
        this.wete = wete;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Integer registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Integer getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(Integer repairDate) {
        this.repairDate = repairDate;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getForeigner() {
        return foreigner;
    }

    public void setForeigner(String foreigner) {
        this.foreigner = foreigner;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getClaimNumberSde() {
        return claimNumberSde;
    }

    public void setClaimNumberSde(String claimNumberSde) {
        this.claimNumberSde = claimNumberSde;
    }

    public Integer getStatusCodeSde() {
        return statusCodeSde;
    }

    public void setStatusCodeSde(Integer statusCodeSde) {
        this.statusCodeSde = statusCodeSde;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAufNr() {
        return aufNr;
    }

    public void setAufNr(String aufNr) {
        this.aufNr = aufNr;
    }
}