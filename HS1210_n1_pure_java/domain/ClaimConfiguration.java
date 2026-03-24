package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "S3F002")
public class ClaimConfiguration {

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
    private BigDecimal sssMatRecvUplFct;

    @Column(name = "SSS Low Value Upl Fct", precision = 5, scale = 4, nullable = false)
    private BigDecimal sssLowValueUplFct;

    @Column(name = "SSS Lab Recv Rt/Hr", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLabRecvRtHr;

    @Column(name = "SSS Lab Recv Rt/Hr 2", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLabRecvRtHr2;

    @Column(name = "SSS Lab Recv Rt/Hr 3", precision = 8, scale = 2, nullable = false)
    private BigDecimal sssLabRecvRtHr3;

    @Column(name = "Eff Per Start Date 1", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerStartDate1;

    @Column(name = "Eff Per Start Date 2", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerStartDate2;

    @Column(name = "Eff Per Start Date 3", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerStartDate3;

    @Column(name = "Eff Per End Date 1", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerEndDate1;

    @Column(name = "Eff Per End Date 2", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerEndDate2;

    @Column(name = "Eff Per End Date 3", precision = 8, scale = 0, nullable = false)
    private BigDecimal effPerEndDate3;

    @Column(name = "Foreign Trucks Comm %", precision = 5, scale = 2, nullable = false)
    private BigDecimal foreignTrucksCommPercent;

    @Column(name = "Effect Retail Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectRetailDate;

    @Column(name = "Effect Cost Date", precision = 8, scale = 0, nullable = false)
    private BigDecimal effectCostDate;

    @Column(name = "Uplift Percentage", precision = 4, scale = 2, nullable = false)
    private BigDecimal upliftPercentage;

    @Column(name = "Auth Option", precision = 4, scale = 2, nullable = false)
    private BigDecimal authOption;

    @Column(name = "Goodwill BML Limit", precision = 9, scale = 2, nullable = false)
    private BigDecimal goodwillBmlLimit;

    @Column(name = "Max Age of Claim (mths)", precision = 9, scale = 2, nullable = false)
    private BigDecimal maxAgeOfClaimMonths;

    @Column(name = "G/A No", precision = 5, scale = 0, nullable = false)
    private BigDecimal gaNo;

    @Column(name = "SSS Supp. No.", length = 10, nullable = false)
    private String sssSuppNo;

    @Column(name = "Dmd codeForeign Veh. Uplift", length = 2, nullable = false)
    private String dmdCodeForeignVehUplift;

    @Column(name = "Dmd codeBus & Coach Uplift", length = 2, nullable = false)
    private String dmdCodeBusCoachUplift;

    @Column(name = "SSS Low Value Article Limit", precision = 3, scale = 0, nullable = false)
    private BigDecimal sssLowValueArticleLimit;

    @Column(name = "G/A Low Value Article Limit", precision = 9, scale = 2, nullable = false)
    private BigDecimal gaLowValueArticleLimit;

    @Column(name = "Parts Order   Type", length = 2, nullable = false)
    private String partsOrderType;

    @Column(name = "Cust Order Type Code(Stock Order", length = 2, nullable = false)
    private String custOrderTypeCodeStockOrder;

    @Column(name = "Cust Order Type Code(VOR)", length = 2, nullable = false)
    private String custOrderTypeCodeVor;

    @Column(name = "Sp Csts Batch line Value limit", precision = 11, scale = 2, nullable = false)
    private BigDecimal spCstsBatchLineValueLimit;

    @Column(name = "Customer Company Code", length = 3, nullable = false)
    private String customerCompanyCode;

    @Column(name = "Modify Reason for Claim", length = 1, nullable = false)
    private String modifyReasonForClaim;

    @Column(name = "Check Agreement Code", length = 1, nullable = false)
    private String checkAgreementCode;

    // Constructors
    public ClaimConfiguration() {
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

    public BigDecimal getSssMatRecvUplFct() {
        return sssMatRecvUplFct;
    }

    public void setSssMatRecvUplFct(BigDecimal sssMatRecvUplFct) {
        this.sssMatRecvUplFct = sssMatRecvUplFct;
    }

    public BigDecimal getSssLowValueUplFct() {
        return sssLowValueUplFct;
    }

    public void setSssLowValueUplFct(BigDecimal sssLowValueUplFct) {
        this.sssLowValueUplFct = sssLowValueUplFct;
    }

    public BigDecimal getSssLabRecvRtHr() {
        return sssLabRecvRtHr;
    }

    public void setSssLabRecvRtHr(BigDecimal sssLabRecvRtHr) {
        this.sssLabRecvRtHr = sssLabRecvRtHr;
    }

    public BigDecimal getSssLabRecvRtHr2() {
        return sssLabRecvRtHr2;
    }

    public void setSssLabRecvRtHr2(BigDecimal sssLabRecvRtHr2) {
        this.sssLabRecvRtHr2 = sssLabRecvRtHr2;
    }

    public BigDecimal getSssLabRecvRtHr3() {
        return sssLabRecvRtHr3;
    }

    public void setSssLabRecvRtHr3(BigDecimal sssLabRecvRtHr3) {
        this.sssLabRecvRtHr3 = sssLabRecvRtHr3;
    }

    public BigDecimal getEffPerStartDate1() {
        return effPerStartDate1;
    }

    public void setEffPerStartDate1(BigDecimal effPerStartDate1) {
        this.effPerStartDate1 = effPerStartDate1;
    }

    public BigDecimal getEffPerStartDate2() {
        return effPerStartDate2;
    }

    public void setEffPerStartDate2(BigDecimal effPerStartDate2) {
        this.effPerStartDate2 = effPerStartDate2;
    }

    public BigDecimal getEffPerStartDate3() {
        return effPerStartDate3;
    }

    public void setEffPerStartDate3(BigDecimal effPerStartDate3) {
        this.effPerStartDate3 = effPerStartDate3;
    }

    public BigDecimal getEffPerEndDate1() {
        return effPerEndDate1;
    }

    public void setEffPerEndDate1(BigDecimal effPerEndDate1) {
        this.effPerEndDate1 = effPerEndDate1;
    }

    public BigDecimal getEffPerEndDate2() {
        return effPerEndDate2;
    }

    public void setEffPerEndDate2(BigDecimal effPerEndDate2) {
        this.effPerEndDate2 = effPerEndDate2;
    }

    public BigDecimal getEffPerEndDate3() {
        return effPerEndDate3;
    }

    public void setEffPerEndDate3(BigDecimal effPerEndDate3) {
        this.effPerEndDate3 = effPerEndDate3;
    }

    public BigDecimal getForeignTrucksCommPercent() {
        return foreignTrucksCommPercent;
    }

    public void setForeignTrucksCommPercent(BigDecimal foreignTrucksCommPercent) {
        this.foreignTrucksCommPercent = foreignTrucksCommPercent;
    }

    public BigDecimal getEffectRetailDate() {
        return effectRetailDate;
    }

    public void setEffectRetailDate(BigDecimal effectRetailDate) {
        this.effectRetailDate = effectRetailDate;
    }

    public BigDecimal getEffectCostDate() {
        return effectCostDate;
    }

    public void setEffectCostDate(BigDecimal effectCostDate) {
        this.effectCostDate = effectCostDate;
    }

    public BigDecimal getUpliftPercentage() {
        return upliftPercentage;
    }

    public void setUpliftPercentage(BigDecimal upliftPercentage) {
        this.upliftPercentage = upliftPercentage;
    }

    public BigDecimal getAuthOption() {
        return authOption;
    }

    public void setAuthOption(BigDecimal authOption) {
        this.authOption = authOption;
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

    public BigDecimal getGaNo() {
        return gaNo;
    }

    public void setGaNo(BigDecimal gaNo) {
        this.gaNo = gaNo;
    }

    public String getSssSuppNo() {
        return sssSuppNo;
    }

    public void setSssSuppNo(String sssSuppNo) {
        this.sssSuppNo = sssSuppNo;
    }

    public String getDmdCodeForeignVehUplift() {
        return dmdCodeForeignVehUplift;
    }

    public void setDmdCodeForeignVehUplift(String dmdCodeForeignVehUplift) {
        this.dmdCodeForeignVehUplift = dmdCodeForeignVehUplift;
    }

    public String getDmdCodeBusCoachUplift() {
        return dmdCodeBusCoachUplift;
    }

    public void setDmdCodeBusCoachUplift(String dmdCodeBusCoachUplift) {
        this.dmdCodeBusCoachUplift = dmdCodeBusCoachUplift;
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

    public String getCustOrderTypeCodeStockOrder() {
        return custOrderTypeCodeStockOrder;
    }

    public void setCustOrderTypeCodeStockOrder(String custOrderTypeCodeStockOrder) {
        this.custOrderTypeCodeStockOrder = custOrderTypeCodeStockOrder;
    }

    public String getCustOrderTypeCodeVor() {
        return custOrderTypeCodeVor;
    }

    public void setCustOrderTypeCodeVor(String custOrderTypeCodeVor) {
        this.custOrderTypeCodeVor = custOrderTypeCodeVor;
    }

    public BigDecimal getSpCstsBatchLineValueLimit() {
        return spCstsBatchLineValueLimit;
    }

    public void setSpCstsBatchLineValueLimit(BigDecimal spCstsBatchLineValueLimit) {
        this.spCstsBatchLineValueLimit = spCstsBatchLineValueLimit;
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