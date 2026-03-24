package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HSG71PF")
public class Claim {

    @Id
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNumber;

    @Column(name = "PAKZ", length = 3, nullable = false)
    private String dealerId;

    @Column(name = "RECH.-NR.", length = 5, nullable = false)
    private String invoiceNumber;

    @Column(name = "RECH.-DATUM", length = 8, nullable = false)
    private String invoiceDate;

    @Column(name = "AUFTRAGS-NR.", length = 5, nullable = false)
    private String orderNumber;

    @Column(name = "WETE", length = 1, nullable = false)
    private String wete;

    @Column(name = "CHASSIS-NR.", length = 7, nullable = false)
    private String chassisNumber;

    @Column(name = "KENNZEICHEN", length = 10, nullable = false)
    private String registrationNumber;

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
    private String sdeClaimNumber;

    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0, nullable = false)
    private Integer sdeStatusCode;

    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0, nullable = false)
    private Integer errorCount;

    @Column(name = "BEREICH", length = 1, nullable = false)
    private String area;

    @Column(name = "AUF.NR.", length = 10, nullable = false)
    private String workOrderNumber;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimError> errors = new ArrayList<>();

    public Claim() {
    }

    public Claim(String claimNumber, String dealerId, String invoiceNumber, String invoiceDate,
                 String orderNumber, String wete, String chassisNumber, String registrationNumber,
                 Integer registrationDate, Integer repairDate, Integer mileage, Integer productType,
                 String attachment, String foreigner, String customerNumber, String customerName,
                 String sdeClaimNumber, Integer sdeStatusCode, Integer errorCount, String area,
                 String workOrderNumber) {
        this.claimNumber = claimNumber;
        this.dealerId = dealerId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.orderNumber = orderNumber;
        this.wete = wete;
        this.chassisNumber = chassisNumber;
        this.registrationNumber = registrationNumber;
        this.registrationDate = registrationDate;
        this.repairDate = repairDate;
        this.mileage = mileage;
        this.productType = productType;
        this.attachment = attachment;
        this.foreigner = foreigner;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.sdeClaimNumber = sdeClaimNumber;
        this.sdeStatusCode = sdeStatusCode;
        this.errorCount = errorCount;
        this.area = area;
        this.workOrderNumber = workOrderNumber;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
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

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
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

    public String getSdeClaimNumber() {
        return sdeClaimNumber;
    }

    public void setSdeClaimNumber(String sdeClaimNumber) {
        this.sdeClaimNumber = sdeClaimNumber;
    }

    public Integer getSdeStatusCode() {
        return sdeStatusCode;
    }

    public void setSdeStatusCode(Integer sdeStatusCode) {
        this.sdeStatusCode = sdeStatusCode;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public List<ClaimError> getErrors() {
        return errors;
    }

    public void setErrors(List<ClaimError> errors) {
        this.errors = errors;
    }

    public void addError(ClaimError error) {
        errors.add(error);
        error.setClaim(this);
    }

    public void removeError(ClaimError error) {
        errors.remove(error);
        error.setClaim(null);
    }
}