package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HSG73PR")
public class WarrantyClaimError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "G73000")
    private String companyCode;

    @Column(name = "G73010")
    private String claimNumber;

    @Column(name = "G73020")
    private String claimSequence;

    @Column(name = "G73030")
    private String invoiceNumber;

    @Column(name = "G73040")
    private String invoiceSequence;

    @Column(name = "G73050")
    private String claimLineNumber;

    @Column(name = "G73060")
    private String failureCode;

    @Column(name = "G73070")
    private String partNumber;

    @Column(name = "G73080")
    private String mainGroup;

    @Column(name = "G73090")
    private String subGroup;

    @Column(name = "G73100")
    private String serviceCode;

    @Column(name = "G73120")
    private String textLine1;

    @Column(name = "G73130")
    private String textLine2;

    @Column(name = "G73140")
    private String controlCode;

    @Column(name = "G73180")
    private BigDecimal materialCostPercentage;

    @Column(name = "G73190")
    private BigDecimal laborCostPercentage;

    @Column(name = "G73200")
    private BigDecimal specialCostPercentage;

    @Column(name = "G73210")
    private BigDecimal materialValue;

    @Column(name = "G73220")
    private BigDecimal laborValue;

    @Column(name = "G73230")
    private BigDecimal specialValue;

    @Column(name = "G73240")
    private Integer quantity;

    @Column(name = "G73290")
    private Integer statusFlag;

    @Column(name = "G73320")
    private String textLine3;

    @Column(name = "G73330")
    private String textLine4;

    @Column(name = "G73340")
    private String failureCodeCopy;

    // Constructors
    public WarrantyClaimError() {
    }

    // Getters and Setters
    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getClaimSequence() {
        return claimSequence;
    }

    public void setClaimSequence(String claimSequence) {
        this.claimSequence = claimSequence;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceSequence() {
        return invoiceSequence;
    }

    public void setInvoiceSequence(String invoiceSequence) {
        this.invoiceSequence = invoiceSequence;
    }

    public String getClaimLineNumber() {
        return claimLineNumber;
    }

    public void setClaimLineNumber(String claimLineNumber) {
        this.claimLineNumber = claimLineNumber;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getTextLine1() {
        return textLine1;
    }

    public void setTextLine1(String textLine1) {
        this.textLine1 = textLine1;
    }

    public String getTextLine2() {
        return textLine2;
    }

    public void setTextLine2(String textLine2) {
        this.textLine2 = textLine2;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public BigDecimal getMaterialCostPercentage() {
        return materialCostPercentage;
    }

    public void setMaterialCostPercentage(BigDecimal materialCostPercentage) {
        this.materialCostPercentage = materialCostPercentage;
    }

    public BigDecimal getLaborCostPercentage() {
        return laborCostPercentage;
    }

    public void setLaborCostPercentage(BigDecimal laborCostPercentage) {
        this.laborCostPercentage = laborCostPercentage;
    }

    public BigDecimal getSpecialCostPercentage() {
        return specialCostPercentage;
    }

    public void setSpecialCostPercentage(BigDecimal specialCostPercentage) {
        this.specialCostPercentage = specialCostPercentage;
    }

    public BigDecimal getMaterialValue() {
        return materialValue;
    }

    public void setMaterialValue(BigDecimal materialValue) {
        this.materialValue = materialValue;
    }

    public BigDecimal getLaborValue() {
        return laborValue;
    }

    public void setLaborValue(BigDecimal laborValue) {
        this.laborValue = laborValue;
    }

    public BigDecimal getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(BigDecimal specialValue) {
        this.specialValue = specialValue;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(Integer statusFlag) {
        this.statusFlag = statusFlag;
    }

    public String getTextLine3() {
        return textLine3;
    }

    public void setTextLine3(String textLine3) {
        this.textLine3 = textLine3;
    }

    public String getTextLine4() {
        return textLine4;
    }

    public void setTextLine4(String textLine4) {
        this.textLine4 = textLine4;
    }

    public String getFailureCodeCopy() {
        return failureCodeCopy;
    }

    public void setFailureCodeCopy(String failureCodeCopy) {
        this.failureCodeCopy = failureCodeCopy;
    }
}