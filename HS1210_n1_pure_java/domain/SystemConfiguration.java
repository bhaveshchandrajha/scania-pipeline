package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "S3F002")
public class SystemConfiguration {

    @Id
    @Column(name = "Key", length = 1, nullable = false)
    private String key;

    @Column(name = "Curr Perd YYWW", precision = 6, scale = 0, nullable = false)
    private BigDecimal currentPeriodYyww;

    @Column(name = "SSS Claim Value", precision = 9, scale = 2, nullable = false)
    private BigDecimal sssClaimValue;

    @Column(name = "Curr Exch Rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal currentExchangeRate;

    @Column(name = "Curr Exch Rt Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal currentExchangeRateDate;

    @Column(name = "Prev Exch Rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal previousExchangeRate;

    @Column(name = "Prev Exch Rt Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal previousExchangeRateDate;

    @Column(name = "3rd Last Ex Rt", precision = 9, scale = 6, nullable = false)
    private BigDecimal thirdLastExchangeRate;

    @Column(name = "3rd L Exch Rt Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal thirdLastExchangeRateDate;

    @Column(name = "SSS Mat Recv Upl Fct", precision = 5, scale = 4, nullable = false)
    private BigDecimal sssMaterialRecoveryUpliftFactor;

    @Column(name = "SSS Low Value Upl Fct", precision = 5, scale = 4, nullable = false)
    private BigDecimal sssLowValueUpliftFactor;

    @Column(name = "SSS Lab Recv Rt/Hr", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLaborRecoveryRatePerHour;

    @Column(name = "SSS Lab Recv Rt/Hr 2", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLaborRecoveryRatePerHour2;

    @Column(name = "SSS Lab Recv Rt/Hr 3", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLaborRecoveryRatePerHour3;

    @Column(name = "Eff Per Start Date 1", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodStartDate1;

    @Column(name = "Eff Per Start Date 2", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodStartDate2;

    @Column(name = "Eff Per Start Date 3", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodStartDate3;

    @Column(name = "Eff Per End Date 1", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodEndDate1;

    @Column(name = "Eff Per End Date 2", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodEndDate2;

    @Column(name = "Eff Per End Date 3", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectivePeriodEndDate3;

    @Column(name = "Foreign Trucks Comm %", precision = 5, scale = 2, nullable = false)
    private BigDecimal foreignTrucksCommissionPercent;

    @Column(name = "Effect Retail Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectiveRetailDate;

    @Column(name = "Effect Cost Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectiveCostDate;

    @Column(name = "Uplift Percentage", precision = 4, scale = 2, nullable = false)
    private BigDecimal upliftPercentage;

    @Column(name = "Auth Option", precision = 4, scale = 2, nullable = false)
    private BigDecimal authorizationOption;

    @Column(name = "Goodwill BML Limit", precision = 9, scale = 2, nullable = false)
    private BigDecimal goodwillBmlLimit;

    @Column(name = "Max Age of Claim (mths)", precision = 9, scale = 2, nullable = false)
    private BigDecimal maxAgeOfClaimMonths;

    @Column(name = "G/A No", precision = 5, scale = 0, nullable = false)
    private BigDecimal gaNumber;

    @Column(name = "SSS Supp. No.", length = 10, nullable = false)
    private String sssSupplierNumber;

    @Column(name = "Dmd codeForeign Veh. Uplift", length = 2, nullable = false)
    private String demandCodeForeignVehicleUplift;

    @Column(name = "Dmd codeBus & Coach Uplift", length = 2, nullable = false)
    private String demandCodeBusCoachUplift;

    @Column(name = "SSS Low Value Article Limit", precision = 3, scale = 0, nullable = false)
    private BigDecimal sssLowValueArticleLimit;

    @Column(name = "G/A Low Value Article Limit", precision = 9, scale = 2, nullable = false)
    private BigDecimal gaLowValueArticleLimit;

    @Column(name = "Parts Order   Type", length = 2, nullable = false)
    private String partsOrderType;

    @Column(name = "Cust Order Type Code(Stock Order", length = 2, nullable = false)
    private String customerOrderTypeCodeStockOrder;

    @Column(name = "Cust Order Type Code(VOR)", length = 2, nullable = false)
    private String customerOrderTypeCodeVor;

    @Column(name = "Sp Csts Batch line Value limit", precision = 11, scale = 2, nullable = false)
    private BigDecimal specialCostsBatchLineValueLimit;

    @Column(name = "Customer Company Code", length = 3, nullable = false)
    private String customerCompanyCode;

    @Column(name = "Modify Reason for Claim", length = 1, nullable = false)
    private String modifyReasonForClaim;

    @Column(name = "Check Agreement Code", length = 1, nullable = false)
    private String checkAgreementCode;

    // Constructors
    public SystemConfiguration() {
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public BigDecimal getCurrentPeriodYyww() {
        return currentPeriodYyww;
    }

    public void setCurrentPeriodYyww(BigDecimal currentPeriodYyww) {
        this.currentPeriodYyww = currentPeriodYyww;
    }

    public BigDecimal getSssClaimValue() {
        return sssClaimValue;
    }

    public void setSssClaimValue(BigDecimal sssClaimValue) {
        this.sssClaimValue = sssClaimValue;
    }

    public BigDecimal getCurrentExchangeRate() {
        return currentExchangeRate;
    }

    public void setCurrentExchangeRate(BigDecimal currentExchangeRate) {
        this.currentExchangeRate = currentExchangeRate;
    }

    public BigDecimal getCurrentExchangeRateDate() {
        return currentExchangeRateDate;
    }

    public void setCurrentExchangeRateDate(BigDecimal currentExchangeRateDate) {
        this.currentExchangeRateDate = currentExchangeRateDate;
    }

    public BigDecimal getPreviousExchangeRate() {
        return previousExchangeRate;
    }

    public void setPreviousExchangeRate(BigDecimal previousExchangeRate) {
        this.previousExchangeRate = previousExchangeRate;
    }

    public BigDecimal getPreviousExchangeRateDate() {
        return previousExchangeRateDate;
    }

    public void setPreviousExchangeRateDate(BigDecimal previousExchangeRateDate) {
        this.previousExchangeRateDate = previousExchangeRateDate;
    }

    public BigDecimal getThirdLastExchangeRate() {
        return thirdLastExchangeRate;
    }

    public void setThirdLastExchangeRate(BigDecimal thirdLastExchangeRate) {
        this.thirdLastExchangeRate = thirdLastExchangeRate;
    }

    public BigDecimal getThirdLastExchangeRateDate() {
        return thirdLastExchangeRateDate;
    }

    public void setThirdLastExchangeRateDate(BigDecimal thirdLastExchangeRateDate) {
        this.thirdLastExchangeRateDate = thirdLastExchangeRateDate;
    }

    public BigDecimal getSssMaterialRecoveryUpliftFactor() {
        return sssMaterialRecoveryUpliftFactor;
    }

    public void setSssMaterialRecoveryUpliftFactor(BigDecimal sssMaterialRecoveryUpliftFactor) {
        this.sssMaterialRecoveryUpliftFactor = sssMaterialRecoveryUpliftFactor;
    }

    public BigDecimal getSssLowValueUpliftFactor() {
        return sssLowValueUpliftFactor;
    }

    public void setSssLowValueUpliftFactor(BigDecimal sssLowValueUpliftFactor) {
        this.sssLowValueUpliftFactor = sssLowValueUpliftFactor;
    }

    public BigDecimal getSssLaborRecoveryRatePerHour() {
        return sssLaborRecoveryRatePerHour;
    }

    public void setSssLaborRecoveryRatePerHour(BigDecimal sssLaborRecoveryRatePerHour) {
        this.sssLaborRecoveryRatePerHour = sssLaborRecoveryRatePerHour;
    }

    public BigDecimal getSssLaborRecoveryRatePerHour2() {
        return sssLaborRecoveryRatePerHour2;
    }

    public void setSssLaborRecoveryRatePerHour2(BigDecimal sssLaborRecoveryRatePerHour2) {
        this.sssLaborRecoveryRatePerHour2 = sssLaborRecoveryRatePerHour2;
    }

    public BigDecimal getSssLaborRecoveryRatePerHour3() {
        return sssLaborRecoveryRatePerHour3;
    }

    public void setSssLaborRecoveryRatePerHour3(BigDecimal sssLaborRecoveryRatePerHour3) {
        this.sssLaborRecoveryRatePerHour3 = sssLaborRecoveryRatePerHour3;
    }

    public BigDecimal getEffectivePeriodStartDate1() {
        return effectivePeriodStartDate1;
    }

    public void setEffectivePeriodStartDate1(BigDecimal effectivePeriodStartDate1) {
        this.effectivePeriodStartDate1 = effectivePeriodStartDate1;
    }

    public BigDecimal getEffectivePeriodStartDate2() {
        return effectivePeriodStartDate2;
    }

    public void setEffectivePeriodStartDate2(BigDecimal effectivePeriodStartDate2) {
        this.effectivePeriodStartDate2 = effectivePeriodStartDate2;
    }

    public BigDecimal getEffectivePeriodStartDate3() {
        return effectivePeriodStartDate3;
    }

    public void setEffectivePeriodStartDate3(BigDecimal effectivePeriodStartDate3) {
        this.effectivePeriodStartDate3 = effectivePeriodStartDate3;
    }

    public BigDecimal getEffectivePeriodEndDate1() {
        return effectivePeriodEndDate1;
    }

    public void setEffectivePeriodEndDate1(BigDecimal effectivePeriodEndDate1) {
        this.effectivePeriodEndDate1 = effectivePeriodEndDate1;
    }

    public BigDecimal getEffectivePeriodEndDate2() {
        return effectivePeriodEndDate2;
    }

    public void setEffectivePeriodEndDate2(BigDecimal effectivePeriodEndDate2) {
        this.effectivePeriodEndDate2 = effectivePeriodEndDate2;
    }

    public BigDecimal getEffectivePeriodEndDate3() {
        return effectivePeriodEndDate3;
    }

    public void setEffectivePeriodEndDate3(BigDecimal effectivePeriodEndDate3) {
        this.effectivePeriodEndDate3 = effectivePeriodEndDate3;
    }

    public BigDecimal getForeignTrucksCommissionPercent() {
        return foreignTrucksCommissionPercent;
    }

    public void setForeignTrucksCommissionPercent(BigDecimal foreignTrucksCommissionPercent) {
        this.foreignTrucksCommissionPercent = foreignTrucksCommissionPercent;
    }

    public BigDecimal getEffectiveRetailDate() {
        return effectiveRetailDate;
    }

    public void setEffectiveRetailDate(BigDecimal effectiveRetailDate) {
        this.effectiveRetailDate = effectiveRetailDate;
    }

    public BigDecimal getEffectiveCostDate() {
        return effectiveCostDate;
    }

    public void setEffectiveCostDate(BigDecimal effectiveCostDate) {
        this.effectiveCostDate = effectiveCostDate;
    }

    public BigDecimal getUpliftPercentage() {
        return upliftPercentage;
    }

    public void setUpliftPercentage(BigDecimal upliftPercentage) {
        this.upliftPercentage = upliftPercentage;
    }

    public BigDecimal getAuthorizationOption() {
        return authorizationOption;
    }

    public void setAuthorizationOption(BigDecimal authorizationOption) {
        this.authorizationOption = authorizationOption;
    }

    public BigDecimal getGoodwillBmlLimit() {
        return goodwillBmlLimit;
    }

    public void setGoodwillBmlLimit(BigDecimal goodwillBmlLimit) {
        this.goodwillBmlLimit = goodwillBmlLimit;
    }

    public BigDecimal getMaxAgeOfClaimMonths() {
        return maxAgeOfClaimMonths;
    }

    public void setMaxAgeOfClaimMonths(BigDecimal maxAgeOfClaimMonths) {
        this.maxAgeOfClaimMonths = maxAgeOfClaimMonths;
    }

    public BigDecimal getGaNumber() {
        return gaNumber;
    }

    public void setGaNumber(BigDecimal gaNumber) {
        this.gaNumber = gaNumber;
    }

    public String getSssSupplierNumber() {
        return sssSupplierNumber;
    }

    public void setSssSupplierNumber(String sssSupplierNumber) {
        this.sssSupplierNumber = sssSupplierNumber;
    }

    public String getDemandCodeForeignVehicleUplift() {
        return demandCodeForeignVehicleUplift;
    }

    public void setDemandCodeForeignVehicleUplift(String demandCodeForeignVehicleUplift) {
        this.demandCodeForeignVehicleUplift = demandCodeForeignVehicleUplift;
    }

    public String getDemandCodeBusCoachUplift() {
        return demandCodeBusCoachUplift;
    }

    public void setDemandCodeBusCoachUplift(String demandCodeBusCoachUplift) {
        this.demandCodeBusCoachUplift = demandCodeBusCoachUplift;
    }

    public BigDecimal getSssLowValueArticleLimit() {
        return sssLowValueArticleLimit;
    }

    public void setSssLowValueArticleLimit(BigDecimal sssLowValueArticleLimit) {
        this.sssLowValueArticleLimit = sssLowValueArticleLimit;
    }

    public BigDecimal getGaLowValueArticleLimit() {
        return gaLowValueArticleLimit;
    }

    public void setGaLowValueArticleLimit(BigDecimal gaLowValueArticleLimit) {
        this.gaLowValueArticleLimit = gaLowValueArticleLimit;
    }

    public String getPartsOrderType() {
        return partsOrderType;
    }

    public void setPartsOrderType(String partsOrderType) {
        this.partsOrderType = partsOrderType;
    }

    public String getCustomerOrderTypeCodeStockOrder() {
        return customerOrderTypeCodeStockOrder;
    }

    public void setCustomerOrderTypeCodeStockOrder(String customerOrderTypeCodeStockOrder) {
        this.customerOrderTypeCodeStockOrder = customerOrderTypeCodeStockOrder;
    }

    public String getCustomerOrderTypeCodeVor() {
        return customerOrderTypeCodeVor;
    }

    public void setCustomerOrderTypeCodeVor(String customerOrderTypeCodeVor) {
        this.customerOrderTypeCodeVor = customerOrderTypeCodeVor;
    }

    public BigDecimal getSpecialCostsBatchLineValueLimit() {
        return specialCostsBatchLineValueLimit;
    }

    public void setSpecialCostsBatchLineValueLimit(BigDecimal specialCostsBatchLineValueLimit) {
        this.specialCostsBatchLineValueLimit = specialCostsBatchLineValueLimit;
    }

    public String getCustomerCompanyCode() {
        return customerCompanyCode;
    }

    public void setCustomerCompanyCode(String customerCompanyCode) {
        this.customerCompanyCode = customerCompanyCode;
    }

    public String getModifyReasonForClaim() {
        return modifyReasonForClaim;
    }

    public void setModifyReasonForClaim(String modifyReasonForClaim) {
        this.modifyReasonForClaim = modifyReasonForClaim;
    }

    public String getCheckAgreementCode() {
        return checkAgreementCode;
    }

    public void setCheckAgreementCode(String checkAgreementCode) {
        this.checkAgreementCode = checkAgreementCode;
    }
}