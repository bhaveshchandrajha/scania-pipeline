package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Audit row when claim creation fails validation (e.g. repair date &gt; 19 days old).
 */
@Entity
@Table(name = "WRT_FAILED_CLAIM")
public class FailedClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPANY_CODE", length = 3, nullable = false)
    private String companyCode;

    @Column(name = "INVOICE_NR", length = 5, nullable = false)
    private String invoiceNr;

    @Column(name = "INVOICE_DATE", length = 8, nullable = false)
    private String invoiceDate;

    @Column(name = "WORKSHOP_CODE", length = 5, nullable = false)
    private String workshopCode;

    @Column(name = "FAILURE_REASON", length = 512, nullable = false)
    private String failureReason;

    @Column(name = "REPAIR_AGE_DAYS")
    private Integer repairAgeDays;

    @Column(name = "FAILED_AT", nullable = false)
    private Instant failedAt;

    public FailedClaim() {}

    public FailedClaim(String companyCode, String invoiceNr, String invoiceDate, String workshopCode,
                       String failureReason, Integer repairAgeDays, Instant failedAt) {
        this.companyCode = companyCode;
        this.invoiceNr = invoiceNr;
        this.invoiceDate = invoiceDate;
        this.workshopCode = workshopCode;
        this.failureReason = failureReason;
        this.repairAgeDays = repairAgeDays;
        this.failedAt = failedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getInvoiceNr() {
        return invoiceNr;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public String getWorkshopCode() {
        return workshopCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Integer getRepairAgeDays() {
        return repairAgeDays;
    }

    public Instant getFailedAt() {
        return failedAt;
    }
}
