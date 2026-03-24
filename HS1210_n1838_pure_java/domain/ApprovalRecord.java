package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "RWAPRL1")
public class ApprovalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "COMPANY_CODE")
    private String companyCode;

    @Column(name = "INVOICE_KEY")
    private String invoiceKey;

    @Column(name = "APR_NR")
    private String approvalNumber;

    @Column(name = "APR_FGNR")
    private String approvalReleaseNumber;

    // Constructors
    public ApprovalRecord() {
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

    public String getInvoiceKey() {
        return invoiceKey;
    }

    public void setInvoiceKey(String invoiceKey) {
        this.invoiceKey = invoiceKey;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public void setApprovalNumber(String approvalNumber) {
        this.approvalNumber = approvalNumber;
    }

    public String getApprovalReleaseNumber() {
        return approvalReleaseNumber;
    }

    public void setApprovalReleaseNumber(String approvalReleaseNumber) {
        this.approvalReleaseNumber = approvalReleaseNumber;
    }
}