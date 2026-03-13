/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HSG71PF")
@IdClass(ClaimHeaderId.class)
public class ClaimHeader {
    
    @Id
    @Column(name = "PAKZ", length = 3, nullable = false)
    private String companyCode; // @rpg-trace: schema
    
    @Id
    @Column(name = "RECH.-NR.", length = 5, nullable = false)
    private String invoiceNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "RECH.-DATUM", length = 8, nullable = false)
    private String invoiceDate; // @rpg-trace: schema
    
    @Id
    @Column(name = "AUFTRAGS-NR.", length = 5, nullable = false)
    private String orderNumber; // @rpg-trace: schema
    
    @Id
    @Column(name = "BEREICH", length = 1, nullable = false)
    private String area; // @rpg-trace: schema
    
    @Column(name = "WETE", length = 1, nullable = false)
    private String wete; // @rpg-trace: schema
    
    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNumber; // @rpg-trace: schema
    
    @Column(name = "CHASSIS-NR.", length = 7, nullable = false)
    private String chassisNumber; // @rpg-trace: schema
    
    @Column(name = "KENNZEICHEN", length = 10, nullable = false)
    private String licensePlate; // @rpg-trace: schema
    
    @Column(name = "ZUL.-DATUM", precision = 8, scale = 0, nullable = false)
    private BigDecimal registrationDate; // @rpg-trace: schema
    
    @Column(name = "REP.-DATUM", precision = 8, scale = 0, nullable = false)
    private BigDecimal repairDate; // @rpg-trace: schema
    
    @Column(name = "KM-STAND", precision = 3, scale = 0, nullable = false)
    private BigDecimal mileage; // @rpg-trace: schema
    
    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0, nullable = false)
    private BigDecimal productType; // @rpg-trace: schema
    
    @Column(name = "ANHANG", length = 1, nullable = false)
    private String attachment; // @rpg-trace: schema
    
    @Column(name = "AUSL#NDER", length = 1, nullable = false)
    private String foreigner; // @rpg-trace: schema
    
    @Column(name = "KD-NR.", length = 6, nullable = false)
    private String customerNumber; // @rpg-trace: schema
    
    @Column(name = "KD-NAME", length = 30, nullable = false)
    private String customerName; // @rpg-trace: schema
    
    @Column(name = "CLAIM-NR. SDE", length = 8, nullable = false)
    private String claimNumberSde; // @rpg-trace: schema
    
    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0, nullable = false)
    private BigDecimal statusCodeSde; // @rpg-trace: schema
    
    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0, nullable = false)
    private BigDecimal failureCount; // @rpg-trace: schema
    
    @Column(name = "AUF.NR.", length = 10, nullable = false)
    private String aufNr; // @rpg-trace: schema
    
    public ClaimHeader() {
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
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
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
    
    public BigDecimal getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(BigDecimal registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public BigDecimal getRepairDate() {
        return repairDate;
    }
    
    public void setRepairDate(BigDecimal repairDate) {
        this.repairDate = repairDate;
    }
    
    public BigDecimal getMileage() {
        return mileage;
    }
    
    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }
    
    public BigDecimal getProductType() {
        return productType;
    }
    
    public void setProductType(BigDecimal productType) {
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
    
    public BigDecimal getStatusCodeSde() {
        return statusCodeSde;
    }
    
    public void setStatusCodeSde(BigDecimal statusCodeSde) {
        this.statusCodeSde = statusCodeSde;
    }
    
    public BigDecimal getFailureCount() {
        return failureCount;
    }
    
    public void setFailureCount(BigDecimal failureCount) {
        this.failureCount = failureCount;
    }
    
    public String getAufNr() {
        return aufNr;
    }
    
    public void setAufNr(String aufNr) {
        this.aufNr = aufNr;
    }
}