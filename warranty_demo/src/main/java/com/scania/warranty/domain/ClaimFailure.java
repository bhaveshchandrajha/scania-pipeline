package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
public class ClaimFailure {
    
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
    private String jobNumber;
    
    @Column(name = "BEREICH", length = 1)
    private String area;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNumber;
    
    @Column(name = "FEHLER-NR.", length = 2)
    private String failureNumber;
    
    @Column(name = "FOLGE-NR.", length = 2)
    private String sequenceNumber;
    
    @Column(name = "FEHLER-TEIL", length = 18)
    private String failurePart;
    
    @Column(name = "HAUPTGRUPPE", length = 2)
    private String mainGroup;
    
    @Column(name = "NEBENGRUPPE", length = 2)
    private String subGroup;
    
    @Column(name = "SCHAD.-C1", length = 2)
    private String damageCode1;
    
    @Column(name = "SCHAD.-C2", length = 2)
    private String damageCode2;
    
    @Column(name = "TEXT1", length = 65)
    private String text1;
    
    @Column(name = "TEXT2", length = 65)
    private String text2;
    
    @Column(name = "STEUER CODE", length = 2)
    private String controlCode;
    
    @Column(name = "BEW. CODE1", length = 2)
    private String assessmentCode1;
    
    @Column(name = "BEW. CODE2", precision = 4, scale = 0)
    private Integer assessmentCode2;
    
    @Column(name = "BEW. DATUM", precision = 8, scale = 0)
    private Integer assessmentDate;
    
    @Column(name = "VERG. MAT.", precision = 3, scale = 0)
    private Integer compensatedMaterial;
    
    @Column(name = "VERG. ARB.", precision = 3, scale = 0)
    private Integer compensatedLabor;
    
    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0)
    private Integer compensatedSpecial;
    
    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2)
    private BigDecimal requestedMaterial;
    
    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2)
    private BigDecimal requestedLabor;
    
    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2)
    private BigDecimal requestedSpecial;
    
    @Column(name = "CLAIM-ART", precision = 1, scale = 0)
    private Integer claimType;
    
    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0)
    private Integer previousRepairDate;
    
    @Column(name = "V.-KM-STAND", precision = 3, scale = 0)
    private Integer previousMileage;
    
    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0)
    private Integer fieldTestNumber;
    
    @Column(name = "KAMPAGNEN-NR.", length = 8)
    private String campaignNumber;
    
    @Column(name = "EPS", length = 20)
    private String eps;
    
    @Column(name = "STATUS CODE", precision = 2, scale = 0)
    private Integer statusCode;
    
    @Column(name = "VARIANT CODE", precision = 2, scale = 0)
    private Integer variantCode;
    
    @Column(name = "ACTION CODE", precision = 2, scale = 0)
    private Integer actionCode;
    
    @Column(name = "TEXT3", length = 65)
    private String text3;
    
    @Column(name = "TEXT4", length = 65)
    private String text4;
    
    @Column(name = "FEHLER-NR. SDE", length = 2)
    private String failureNumberSde;
    
    @Column(name = "ANHANG", length = 1)
    private String attachment;
    
    @Column(name = "SOURCE", length = 5)
    private String source;
    
    @Column(name = "COMPLAIN", length = 5)
    private String complain;
    
    @Column(name = "SYMPTOM", length = 5)
    private String symptom;
    
    @Column(name = "FAILURE", length = 5)
    private String failure;
    
    @Column(name = "LOCATION", length = 5)
    private String location;
    
    @Column(name = "REPAIR", length = 5)
    private String repair;
    
    @Column(name = "ERG.CODE", length = 2)
    private String resultCode;
    
    @Column(name = "RESULT1", length = 2)
    private String result1;
    
    @Column(name = "RESULT2", length = 5)
    private String result2;
    
    @Column(name = "FAULT1", length = 2)
    private String fault1;
    
    @Column(name = "FAULT2", length = 5)
    private String fault2;
    
    @Column(name = "REPLY1", length = 2)
    private String reply1;
    
    @Column(name = "REPLY2", length = 5)
    private String reply2;
    
    @Column(name = "EXPLANATION1", length = 2)
    private String explanation1;
    
    @Column(name = "EXPLANATION2", length = 5)
    private String explanation2;

    // Constructors
    public ClaimFailure() {
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

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getFailureNumber() {
        return failureNumber;
    }

    public void setFailureNumber(String failureNumber) {
        this.failureNumber = failureNumber;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    // Additional getters/setters for remaining fields
}