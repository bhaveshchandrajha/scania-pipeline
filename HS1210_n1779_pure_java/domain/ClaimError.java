package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
public class ClaimError {

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

    @Column(name = "BEREICH", length = 1, nullable = false)
    private String scope;

    @Column(name = "CLAIM-NR.", length = 8, nullable = false)
    private String claimNumber;

    @Id
    @Column(name = "FEHLER-NR.", length = 2, nullable = false)
    private String errorNumber;

    @Column(name = "FOLGE-NR.", length = 2, nullable = false)
    private String sequenceNumber;

    @Column(name = "FEHLER-TEIL", length = 18, nullable = false)
    private String errorPart;

    @Column(name = "HAUPTGRUPPE", length = 2, nullable = false)
    private String mainGroup;

    @Column(name = "NEBENGRUPPE", length = 2, nullable = false)
    private String subGroup;

    @Column(name = "SCHAD.-C1", length = 2, nullable = false)
    private String damageCode1;

    @Column(name = "SCHAD.-C2", length = 2, nullable = false)
    private String damageCode2;

    @Column(name = "TEXT1", length = 65, nullable = false)
    private String text1;

    @Column(name = "TEXT2", length = 65, nullable = false)
    private String text2;

    @Column(name = "STEUER CODE", length = 2, nullable = false)
    private String taxCode;

    @Column(name = "BEW. CODE1", length = 2, nullable = false)
    private String evaluationCode1;

    @Column(name = "BEW. CODE2", precision = 4, scale = 0, nullable = false)
    private Integer evaluationCode2;

    @Column(name = "BEW. DATUM", precision = 8, scale = 0, nullable = false)
    private Integer evaluationDate;

    @Column(name = "VERG. MAT.", precision = 3, scale = 0, nullable = false)
    private Integer compensationMaterial;

    @Column(name = "VERG. ARB.", precision = 3, scale = 0, nullable = false)
    private Integer compensationLabor;

    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0, nullable = false)
    private Integer compensationSpecial;

    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2, nullable = false)
    private BigDecimal requestedMaterial;

    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2, nullable = false)
    private BigDecimal requestedLabor;

    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2, nullable = false)
    private BigDecimal requestedSpecial;

    @Column(name = "CLAIM-ART", precision = 1, scale = 0, nullable = false)
    private Integer claimType;

    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0, nullable = false)
    private Integer previousRepairDate;

    @Column(name = "V.-KM-STAND", precision = 3, scale = 0, nullable = false)
    private Integer previousMileage;

    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0, nullable = false)
    private Integer fieldTestNumber;

    @Column(name = "KAMPAGNEN-NR.", length = 8, nullable = false)
    private String campaignNumber;

    @Column(name = "EPS", length = 20, nullable = false)
    private String eps;

    @Column(name = "STATUS CODE", precision = 2, scale = 0, nullable = false)
    private Integer statusCode;

    @Column(name = "VARIANT CODE", precision = 2, scale = 0, nullable = false)
    private Integer variantCode;

    @Column(name = "ACTION CODE", precision = 2, scale = 0, nullable = false)
    private Integer actionCode;

    @Column(name = "TEXT3", length = 65, nullable = false)
    private String text3;

    @Column(name = "TEXT4", length = 65, nullable = false)
    private String text4;

    @Column(name = "FEHLER-NR. SDE", length = 2, nullable = false)
    private String errorNumberSde;

    @Column(name = "ANHANG", length = 1, nullable = false)
    private String attachment;

    @Column(name = "SOURCE", length = 5, nullable = false)
    private String source;

    @Column(name = "COMPLAIN", length = 5, nullable = false)
    private String complain;

    @Column(name = "SYMPTOM", length = 5, nullable = false)
    private String symptom;

    @Column(name = "FAILURE", length = 5, nullable = false)
    private String failure;

    @Column(name = "LOCATION", length = 5, nullable = false)
    private String location;

    @Column(name = "REPAIR", length = 5, nullable = false)
    private String repair;

    @Column(name = "ERG.CODE", length = 2, nullable = false)
    private String resultCode;

    @Column(name = "RESULT1", length = 2, nullable = false)
    private String result1;

    @Column(name = "RESULT2", length = 5, nullable = false)
    private String result2;

    @Column(name = "FAULT1", length = 2, nullable = false)
    private String fault1;

    @Column(name = "FAULT2", length = 5, nullable = false)
    private String fault2;

    @Column(name = "REPLY1", length = 2, nullable = false)
    private String reply1;

    @Column(name = "REPLY2", length = 5, nullable = false)
    private String reply2;

    @Column(name = "EXPLANATION1", length = 2, nullable = false)
    private String explanation1;

    @Column(name = "EXPLANATION2", length = 5, nullable = false)
    private String explanation2;

    // Constructors
    public ClaimError() {
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public String getErrorNumber() {
        return errorNumber;
    }

    public void setErrorNumber(String errorNumber) {
        this.errorNumber = errorNumber;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getErrorPart() {
        return errorPart;
    }

    public void setErrorPart(String errorPart) {
        this.errorPart = errorPart;
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

    public String getDamageCode1() {
        return damageCode1;
    }

    public void setDamageCode1(String damageCode1) {
        this.damageCode1 = damageCode1;
    }

    public String getDamageCode2() {
        return damageCode2;
    }

    public void setDamageCode2(String damageCode2) {
        this.damageCode2 = damageCode2;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getEvaluationCode1() {
        return evaluationCode1;
    }

    public void setEvaluationCode1(String evaluationCode1) {
        this.evaluationCode1 = evaluationCode1;
    }

    public Integer getEvaluationCode2() {
        return evaluationCode2;
    }

    public void setEvaluationCode2(Integer evaluationCode2) {
        this.evaluationCode2 = evaluationCode2;
    }

    public Integer getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Integer evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Integer getCompensationMaterial() {
        return compensationMaterial;
    }

    public void setCompensationMaterial(Integer compensationMaterial) {
        this.compensationMaterial = compensationMaterial;
    }

    public Integer getCompensationLabor() {
        return compensationLabor;
    }

    public void setCompensationLabor(Integer compensationLabor) {
        this.compensationLabor = compensationLabor;
    }

    public Integer getCompensationSpecial() {
        return compensationSpecial;
    }

    public void setCompensationSpecial(Integer compensationSpecial) {
        this.compensationSpecial = compensationSpecial;
    }

    public BigDecimal getRequestedMaterial() {
        return requestedMaterial;
    }

    public void setRequestedMaterial(BigDecimal requestedMaterial) {
        this.requestedMaterial = requestedMaterial;
    }

    public BigDecimal getRequestedLabor() {
        return requestedLabor;
    }

    public void setRequestedLabor(BigDecimal requestedLabor) {
        this.requestedLabor = requestedLabor;
    }

    public BigDecimal getRequestedSpecial() {
        return requestedSpecial;
    }

    public void setRequestedSpecial(BigDecimal requestedSpecial) {
        this.requestedSpecial = requestedSpecial;
    }

    public Integer getClaimType() {
        return claimType;
    }

    public void setClaimType(Integer claimType) {
        this.claimType = claimType;
    }

    public Integer getPreviousRepairDate() {
        return previousRepairDate;
    }

    public void setPreviousRepairDate(Integer previousRepairDate) {
        this.previousRepairDate = previousRepairDate;
    }

    public Integer getPreviousMileage() {
        return previousMileage;
    }

    public void setPreviousMileage(Integer previousMileage) {
        this.previousMileage = previousMileage;
    }

    public Integer getFieldTestNumber() {
        return fieldTestNumber;
    }

    public void setFieldTestNumber(Integer fieldTestNumber) {
        this.fieldTestNumber = fieldTestNumber;
    }

    public String getCampaignNumber() {
        return campaignNumber;
    }

    public void setCampaignNumber(String campaignNumber) {
        this.campaignNumber = campaignNumber;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getVariantCode() {
        return variantCode;
    }

    public void setVariantCode(Integer variantCode) {
        this.variantCode = variantCode;
    }

    public Integer getActionCode() {
        return actionCode;
    }

    public void setActionCode(Integer actionCode) {
        this.actionCode = actionCode;
    }

    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    public String getErrorNumberSde() {
        return errorNumberSde;
    }

    public void setErrorNumberSde(String errorNumberSde) {
        this.errorNumberSde = errorNumberSde;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getComplain() {
        return complain;
    }

    public void setComplain(String complain) {
        this.complain = complain;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRepair() {
        return repair;
    }

    public void setRepair(String repair) {
        this.repair = repair;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResult1() {
        return result1;
    }

    public void setResult1(String result1) {
        this.result1 = result1;
    }

    public String getResult2() {
        return result2;
    }

    public void setResult2(String result2) {
        this.result2 = result2;
    }

    public String getFault1() {
        return fault1;
    }

    public void setFault1(String fault1) {
        this.fault1 = fault1;
    }

    public String getFault2() {
        return fault2;
    }

    public void setFault2(String fault2) {
        this.fault2 = fault2;
    }

    public String getReply1() {
        return reply1;
    }

    public void setReply1(String reply1) {
        this.reply1 = reply1;
    }

    public String getReply2() {
        return reply2;
    }

    public void setReply2(String reply2) {
        this.reply2 = reply2;
    }

    public String getExplanation1() {
        return explanation1;
    }

    public void setExplanation1(String explanation1) {
        this.explanation1 = explanation1;
    }

    public String getExplanation2() {
        return explanation2;
    }

    public void setExplanation2(String explanation2) {
        this.explanation2 = explanation2;
    }
}