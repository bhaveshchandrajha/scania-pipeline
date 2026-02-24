package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG70F")
public class WarrantyRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "KZL", length = 3, nullable = false)
    private String companyCode;

    @Column(name = "R.NR.", length = 5, nullable = false)
    private String invoiceNumber;

    @Column(name = "R.DAT", length = 8, nullable = false)
    private String invoiceDate;

    @Column(name = "FGNR.", length = 17, nullable = false)
    private String vehicleNumber;

    @Column(name = "REP.DAT.", length = 8, nullable = false)
    private String repairDate;

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status;

    @Column(name = "CUS.NO.", precision = 5, scale = 0, nullable = false)
    private Integer customerNumber;

    @Column(name = "D.C.NO.", precision = 8, scale = 0, nullable = false)
    private Integer dealerClaimNumber;

    @Column(name = "D.C.FN.", length = 5, nullable = false)
    private String dealerClaimFailureNumber;

    public WarrantyRelease() {
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

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(String repairDate) {
        this.repairDate = repairDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(Integer customerNumber) {
        this.customerNumber = customerNumber;
    }

    public Integer getDealerClaimNumber() {
        return dealerClaimNumber;
    }

    public void setDealerClaimNumber(Integer dealerClaimNumber) {
        this.dealerClaimNumber = dealerClaimNumber;
    }

    public String getDealerClaimFailureNumber() {
        return dealerClaimFailureNumber;
    }

    public void setDealerClaimFailureNumber(String dealerClaimFailureNumber) {
        this.dealerClaimFailureNumber = dealerClaimFailureNumber;
    }
}