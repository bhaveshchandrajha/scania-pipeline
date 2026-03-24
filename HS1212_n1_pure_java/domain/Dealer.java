package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "S3F003")
public class Dealer {

    @Id
    @Column(name = "Dist Wrnty Cust No", length = 48)
    private String distWarrantyCustNo;

    @Column(name = "G/A Cust. Number", length = 10, nullable = false)
    private String gaCustNumber;

    @Column(name = "Dist Name", length = 30, nullable = false)
    private String distName;

    @Column(name = "Dist Short Name 1", length = 15, nullable = false)
    private String distShortName1;

    @Column(name = "Dist Short Name 2", length = 6, nullable = false)
    private String distShortName2;

    @Column(name = "Dist Locn /Town", length = 15, nullable = false)
    private String distLocation;

    @Column(name = "Parts Dist. Number", length = 10, nullable = false)
    private String partsDistNumber;

    @Column(name = "Curr Claim Rg Start No", precision = 8, scale = 0, nullable = false)
    private Integer currClaimRgStartNo;

    @Column(name = "Curr Claim Rg End No", precision = 8, scale = 0, nullable = false)
    private Integer currClaimRgEndNo;

    @Column(name = "Prev Claim Rg Start No", precision = 8, scale = 0, nullable = false)
    private Integer prevClaimRgStartNo;

    @Column(name = "Prev Claim Rg End No", precision = 8, scale = 0, nullable = false)
    private Integer prevClaimRgEndNo;

    @Column(name = "Stnd Labour Rt/Hr", precision = 8, scale = 2, nullable = false)
    private BigDecimal stndLabourRtHr;

    @Column(name = "Labour Rt/Hr 2", precision = 8, scale = 2, nullable = false)
    private BigDecimal labourRtHr2;

    @Column(name = "Labour Rt/Hr 3", precision = 8, scale = 2, nullable = false)
    private BigDecimal labourRtHr3;

    @Column(name = "Eff Per Start Date 1", precision = 8, scale = 0, nullable = false)
    private Integer effPerStartDate1;

    @Column(name = "Eff Per End Date 1", precision = 8, scale = 0, nullable = false)
    private Integer effPerEndDate1;

    @Column(name = "Eff Per Start Date 2", precision = 8, scale = 0, nullable = false)
    private Integer effPerStartDate2;

    @Column(name = "Eff Per End Date 2", precision = 8, scale = 0, nullable = false)
    private Integer effPerEndDate2;

    @Column(name = "Eff Per Start Date 3", precision = 8, scale = 0, nullable = false)
    private Integer effPerStartDate3;

    @Column(name = "Eff Per End Date 3", precision = 8, scale = 0, nullable = false)
    private Integer effPerEndDate3;

    @Column(name = "Dist Lab Uplift", precision = 5, scale = 4, nullable = false)
    private BigDecimal distLabUplift;

    @Column(name = "Purch Split S/Order", precision = 3, scale = 0, nullable = false)
    private Integer purchSplitSOrder;

    @Column(name = "Purch Split VOR", precision = 3, scale = 0, nullable = false)
    private Integer purchSplitVor;

    @Column(name = "L/Val Comp Uplift %", precision = 5, scale = 4, nullable = false)
    private BigDecimal lValCompUplift;

    @Column(name = "BML/ Hand. Uplift %", precision = 5, scale = 4, nullable = false)
    private BigDecimal bmlHandUplift;

    @Column(name = "Dist. Labour Uplift Factor 2", precision = 5, scale = 4, nullable = false)
    private BigDecimal distLabourUpliftFactor2;

    @Column(name = "Dist. Labour Uplift Factor 3", precision = 5, scale = 4, nullable = false)
    private BigDecimal distLabourUpliftFactor3;

    @Column(name = "Dist. compensation forcore uplift.", length = 1, nullable = false)
    private String distCompensationForCoreUplift;

    @Column(name = "Online dlr access all claims", length = 1, nullable = false)
    private String onlineDlrAccessAllClaims;

    @Column(name = "VAT code in Leg System", length = 10, nullable = false)
    private String vatCodeInLegSystem;

    @Column(name = "VAT %", precision = 5, scale = 3, nullable = false)
    private BigDecimal vatPercent;

    public Dealer() {
    }

    public String getDistWarrantyCustNo() {
        return distWarrantyCustNo;
    }

    public void setDistWarrantyCustNo(String distWarrantyCustNo) {
        this.distWarrantyCustNo = distWarrantyCustNo;
    }

    public String getGaCustNumber() {
        return gaCustNumber;
    }

    public void setGaCustNumber(String gaCustNumber) {
        this.gaCustNumber = gaCustNumber;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String getDistShortName1() {
        return distShortName1;
    }

    public void setDistShortName1(String distShortName1) {
        this.distShortName1 = distShortName1;
    }

    public String getDistShortName2() {
        return distShortName2;
    }

    public void setDistShortName2(String distShortName2) {
        this.distShortName2 = distShortName2;
    }

    public String getDistLocation() {
        return distLocation;
    }

    public void setDistLocation(String distLocation) {
        this.distLocation = distLocation;
    }

    public String getPartsDistNumber() {
        return partsDistNumber;
    }

    public void setPartsDistNumber(String partsDistNumber) {
        this.partsDistNumber = partsDistNumber;
    }

    public Integer getCurrClaimRgStartNo() {
        return currClaimRgStartNo;
    }

    public void setCurrClaimRgStartNo(Integer currClaimRgStartNo) {
        this.currClaimRgStartNo = currClaimRgStartNo;
    }

    public Integer getCurrClaimRgEndNo() {
        return currClaimRgEndNo;
    }

    public void setCurrClaimRgEndNo(Integer currClaimRgEndNo) {
        this.currClaimRgEndNo = currClaimRgEndNo;
    }

    public Integer getPrevClaimRgStartNo() {
        return prevClaimRgStartNo;
    }

    public void setPrevClaimRgStartNo(Integer prevClaimRgStartNo) {
        this.prevClaimRgStartNo = prevClaimRgStartNo;
    }

    public Integer getPrevClaimRgEndNo() {
        return prevClaimRgEndNo;
    }

    public void setPrevClaimRgEndNo(Integer prevClaimRgEndNo) {
        this.prevClaimRgEndNo = prevClaimRgEndNo;
    }

    public BigDecimal getStndLabourRtHr() {
        return stndLabourRtHr;
    }

    public void setStndLabourRtHr(BigDecimal stndLabourRtHr) {
        this.stndLabourRtHr = stndLabourRtHr;
    }

    public BigDecimal getLabourRtHr2() {
        return labourRtHr2;
    }

    public void setLabourRtHr2(BigDecimal labourRtHr2) {
        this.labourRtHr2 = labourRtHr2;
    }

    public BigDecimal getLabourRtHr3() {
        return labourRtHr3;
    }

    public void setLabourRtHr3(BigDecimal labourRtHr3) {
        this.labourRtHr3 = labourRtHr3;
    }

    public Integer getEffPerStartDate1() {
        return effPerStartDate1;
    }

    public void setEffPerStartDate1(Integer effPerStartDate1) {
        this.effPerStartDate1 = effPerStartDate1;
    }

    public Integer getEffPerEndDate1() {
        return effPerEndDate1;
    }

    public void setEffPerEndDate1(Integer effPerEndDate1) {
        this.effPerEndDate1 = effPerEndDate1;
    }

    public Integer getEffPerStartDate2() {
        return effPerStartDate2;
    }

    public void setEffPerStartDate2(Integer effPerStartDate2) {
        this.effPerStartDate2 = effPerStartDate2;
    }

    public Integer getEffPerEndDate2() {
        return effPerEndDate2;
    }

    public void setEffPerEndDate2(Integer effPerEndDate2) {
        this.effPerEndDate2 = effPerEndDate2;
    }

    public Integer getEffPerStartDate3() {
        return effPerStartDate3;
    }

    public void setEffPerStartDate3(Integer effPerStartDate3) {
        this.effPerStartDate3 = effPerStartDate3;
    }

    public Integer getEffPerEndDate3() {
        return effPerEndDate3;
    }

    public void setEffPerEndDate3(Integer effPerEndDate3) {
        this.effPerEndDate3 = effPerEndDate3;
    }

    public BigDecimal getDistLabUplift() {
        return distLabUplift;
    }

    public void setDistLabUplift(BigDecimal distLabUplift) {
        this.distLabUplift = distLabUplift;
    }

    public Integer getPurchSplitSOrder() {
        return purchSplitSOrder;
    }

    public void setPurchSplitSOrder(Integer purchSplitSOrder) {
        this.purchSplitSOrder = purchSplitSOrder;
    }

    public Integer getPurchSplitVor() {
        return purchSplitVor;
    }

    public void setPurchSplitVor(Integer purchSplitVor) {
        this.purchSplitVor = purchSplitVor;
    }

    public BigDecimal getlValCompUplift() {
        return lValCompUplift;
    }

    public void setlValCompUplift(BigDecimal lValCompUplift) {
        this.lValCompUplift = lValCompUplift;
    }

    public BigDecimal getBmlHandUplift() {
        return bmlHandUplift;
    }

    public void setBmlHandUplift(BigDecimal bmlHandUplift) {
        this.bmlHandUplift = bmlHandUplift;
    }

    public BigDecimal getDistLabourUpliftFactor2() {
        return distLabourUpliftFactor2;
    }

    public void setDistLabourUpliftFactor2(BigDecimal distLabourUpliftFactor2) {
        this.distLabourUpliftFactor2 = distLabourUpliftFactor2;
    }

    public BigDecimal getDistLabourUpliftFactor3() {
        return distLabourUpliftFactor3;
    }

    public void setDistLabourUpliftFactor3(BigDecimal distLabourUpliftFactor3) {
        this.distLabourUpliftFactor3 = distLabourUpliftFactor3;
    }

    public String getDistCompensationForCoreUplift() {
        return distCompensationForCoreUplift;
    }

    public void setDistCompensationForCoreUplift(String distCompensationForCoreUplift) {
        this.distCompensationForCoreUplift = distCompensationForCoreUplift;
    }

    public String getOnlineDlrAccessAllClaims() {
        return onlineDlrAccessAllClaims;
    }

    public void setOnlineDlrAccessAllClaims(String onlineDlrAccessAllClaims) {
        this.onlineDlrAccessAllClaims = onlineDlrAccessAllClaims;
    }

    public String getVatCodeInLegSystem() {
        return vatCodeInLegSystem;
    }

    public void setVatCodeInLegSystem(String vatCodeInLegSystem) {
        this.vatCodeInLegSystem = vatCodeInLegSystem;
    }

    public BigDecimal getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(BigDecimal vatPercent) {
        this.vatPercent = vatPercent;
    }
}