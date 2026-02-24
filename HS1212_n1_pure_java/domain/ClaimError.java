package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG73PF")
public class ClaimError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "G73050", referencedColumnName = "CLAIM-NR.")
    private Claim claim;

    @Column(name = "G73000", length = 3, nullable = false)
    private String dealerId;

    @Column(name = "G73050", length = 8, nullable = false, insertable = false, updatable = false)
    private String claimNumber;

    @Column(name = "G73060", length = 2, nullable = false)
    private String errorNumber;

    @Column(name = "G73065", length = 2, nullable = false)
    private String reconNumber;

    @Column(name = "G73070", length = 18, nullable = false)
    private String damagingPart;

    @Column(name = "G73080", length = 2, nullable = false)
    private String mainGroup;

    @Column(name = "G73100", length = 2, nullable = false)
    private String damageCode;

    @Column(name = "G73110", length = 1, nullable = false)
    private String subGroup;

    @Column(name = "G73120", length = 65, nullable = false)
    private String description1;

    @Column(name = "G73130", length = 65, nullable = false)
    private String description2;

    @Column(name = "G73140", length = 2, nullable = false)
    private String controlCode;

    @Column(name = "G73180", precision = 3, scale = 0, nullable = false)
    private Integer materialPercentage;

    @Column(name = "G73190", precision = 3, scale = 0, nullable = false)
    private Integer labourPercentage;

    @Column(name = "G73200", precision = 3, scale = 0, nullable = false)
    private Integer specialPercentage;

    @Column(name = "G73240", length = 1, nullable = false)
    private String claimType;

    @Column(name = "G73250", precision = 8, scale = 0, nullable = false)
    private Integer previousRepairDate;

    @Column(name = "G73260", precision = 5, scale = 0, nullable = false)
    private Integer previousMileage;

    @Column(name = "G73270", precision = 5, scale = 0, nullable = false)
    private Integer previousRepairNumber;

    @Column(name = "G73280", length = 8, nullable = false)
    private String campaignNumber;

    @Column(name = "G73285", length = 20, nullable = false)
    private String epsName;

    @Column(name = "G73290", precision = 2, scale = 0, nullable = false)
    private Integer status;

    @Column(name = "G73320", length = 65, nullable = false)
    private String description3;

    @Column(name = "G73330", length = 65, nullable = false)
    private String description4;

    @Column(name = "G73340", length = 2, nullable = false)
    private String alternateErrorNumber;

    @Column(name = "G73360", length = 5, nullable = false)
    private String codeC1;

    @Column(name = "G73370", length = 5, nullable = false)
    private String codeC2;

    @Column(name = "G73380", length = 5, nullable = false)
    private String codeC3;

    @Column(name = "G73390", length = 5, nullable = false)
    private String codeC4;

    @Column(name = "G73400", length = 5, nullable = false)
    private String codeC5;

    @Column(name = "G73410", length = 5, nullable = false)
    private String codeC6;

    @Column(name = "G73420", length = 2, nullable = false)
    private String resultCode;

    @Column(name = "G73430", length = 2, nullable = false)
    private String resultType;

    @Column(name = "G73440", length = 5, nullable = false)
    private String resultId;

    @Column(name = "G73450", length = 2, nullable = false)
    private String faultType;

    @Column(name = "G73460", length = 5, nullable = false)
    private String faultId;

    @Column(name = "G73470", length = 2, nullable = false)
    private String repairType;

    @Column(name = "G73480", length = 5, nullable = false)
    private String repairId;

    @Column(name = "G73490", length = 2, nullable = false)
    private String explanationType;

    @Column(name = "G73500", length = 5, nullable = false)
    private String explanationId;

    public ClaimError() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
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

    public String getReconNumber() {
        return reconNumber;
    }

    public void setReconNumber(String reconNumber) {
        this.reconNumber = reconNumber;
    }

    public String getDamagingPart() {
        return damagingPart;
    }

    public void setDamagingPart(String damagingPart) {
        this.damagingPart = damagingPart;
    }

    public String getMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }

    public String getDamageCode() {
        return damageCode;
    }

    public void setDamageCode(String damageCode) {
        this.damageCode = damageCode;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getDescription1() {
        return description1;
    }

    public void setDescription1(String description1) {
        this.description1 = description1;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public Integer getMaterialPercentage() {
        return materialPercentage;
    }

    public void setMaterialPercentage(Integer materialPercentage) {
        this.materialPercentage = materialPercentage;
    }

    public Integer getLabourPercentage() {
        return labourPercentage;
    }

    public void setLabourPercentage(Integer labourPercentage) {
        this.labourPercentage = labourPercentage;
    }

    public Integer getSpecialPercentage() {
        return specialPercentage;
    }

    public void setSpecialPercentage(Integer specialPercentage) {
        this.specialPercentage = specialPercentage;
    }

    public String getClaimType() {
        return claimType;
    }

    public void setClaimType(String claimType) {
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

    public Integer getPreviousRepairNumber() {
        return previousRepairNumber;
    }

    public void setPreviousRepairNumber(Integer previousRepairNumber) {
        this.previousRepairNumber = previousRepairNumber;
    }

    public String getCampaignNumber() {
        return campaignNumber;
    }

    public void setCampaignNumber(String campaignNumber) {
        this.campaignNumber = campaignNumber;
    }

    public String getEpsName() {
        return epsName;
    }

    public void setEpsName(String epsName) {
        this.epsName = epsName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription3() {
        return description3;
    }

    public void setDescription3(String description3) {
        this.description3 = description3;
    }

    public String getDescription4() {
        return description4;
    }

    public void setDescription4(String description4) {
        this.description4 = description4;
    }

    public String getAlternateErrorNumber() {
        return alternateErrorNumber;
    }

    public void setAlternateErrorNumber(String alternateErrorNumber) {
        this.alternateErrorNumber = alternateErrorNumber;
    }

    public String getCodeC1() {
        return codeC1;
    }

    public void setCodeC1(String codeC1) {
        this.codeC1 = codeC1;
    }

    public String getCodeC2() {
        return codeC2;
    }

    public void setCodeC2(String codeC2) {
        this.codeC2 = codeC2;
    }

    public String getCodeC3() {
        return codeC3;
    }

    public void setCodeC3(String codeC3) {
        this.codeC3 = codeC3;
    }

    public String getCodeC4() {
        return codeC4;
    }

    public void setCodeC4(String codeC4) {
        this.codeC4 = codeC4;
    }

    public String getCodeC5() {
        return codeC5;
    }

    public void setCodeC5(String codeC5) {
        this.codeC5 = codeC5;
    }

    public String getCodeC6() {
        return codeC6;
    }

    public void setCodeC6(String codeC6) {
        this.codeC6 = codeC6;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public String getRepairId() {
        return repairId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public String getExplanationType() {
        return explanationType;
    }

    public void setExplanationType(String explanationType) {
        this.explanationType = explanationType;
    }

    public String getExplanationId() {
        return explanationId;
    }

    public void setExplanationId(String explanationId) {
        this.explanationId = explanationId;
    }
}