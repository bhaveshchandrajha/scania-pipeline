package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG71LF2")
public class Claim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String companyCode;
    
    @Column(name = "RECH.-NR.", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RECH.-DATUM", length = 8)
    private String invoiceDate;
    
    @Column(name = "AUFTRAGS-NR.", length = 5)
    private String orderNumber;
    
    @Column(name = "WETE", length = 1)
    private String workshopCounter;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNumber;
    
    @Column(name = "CHASSIS-NR.", length = 7)
    private String chassisNumber;
    
    @Column(name = "KENNZEICHEN", length = 10)
    private String licensePlate;
    
    @Column(name = "ZUL.-DATUM", precision = 8, scale = 0)
    private Integer registrationDate;
    
    @Column(name = "REP.-DATUM", precision = 8, scale = 0)
    private Integer repairDate;
    
    @Column(name = "KM-STAND", precision = 3, scale = 0)
    private Integer mileage;
    
    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0)
    private Integer productType;
    
    @Column(name = "ANHANG", length = 1)
    private String attachment;
    
    @Column(name = "AUSLÄNDER", length = 1)
    private String foreigner;
    
    @Column(name = "KD-NR.", length = 6)
    private String customerNumber;
    
    @Column(name = "KD-NAME", length = 30)
    private String customerName;
    
    @Column(name = "CLAIM-NR. SDE", length = 8)
    private String claimNumberSde;
    
    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0)
    private Integer statusCodeSde;
    
    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0)
    private Integer errorCount;
    
    @Column(name = "BEREICH", length = 1)
    private String area;
    
    @Column(name = "AUF.NR.", length = 10)
    private String jobNumber;

    public Claim() {
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

    public String getWorkshopCounter() {
        return workshopCounter;
    }

    public void setWorkshopCounter(String workshopCounter) {
        this.workshopCounter = workshopCounter;
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }
}