package com.scania.warranty.dto;

import com.scania.warranty.domain.Dealer;

import java.math.BigDecimal;

public record DealerDto(
    String distWarrantyCustNo,
    String gaCustNumber,
    String distName,
    String distShortName1,
    String distShortName2,
    String distLocation,
    String partsDistNumber,
    Integer currClaimRgStartNo,
    Integer currClaimRgEndNo,
    BigDecimal stndLabourRtHr,
    BigDecimal labourRtHr2,
    BigDecimal labourRtHr3,
    Integer effPerStartDate1,
    Integer effPerEndDate1,
    Integer effPerStartDate2,
    Integer effPerEndDate2,
    Integer effPerStartDate3,
    Integer effPerEndDate3,
    BigDecimal distLabUplift,
    BigDecimal distLabourUpliftFactor2,
    BigDecimal distLabourUpliftFactor3,
    BigDecimal vatPercent
) {
    public static DealerDto fromEntity(Dealer dealer) {
        return new DealerDto(
            dealer.getDistWarrantyCustNo(),
            dealer.getGaCustNumber(),
            dealer.getDistName(),
            dealer.getDistShortName1(),
            dealer.getDistShortName2(),
            dealer.getDistLocation(),
            dealer.getPartsDistNumber(),
            dealer.getCurrClaimRgStartNo(),
            dealer.getCurrClaimRgEndNo(),
            dealer.getStndLabourRtHr(),
            dealer.getLabourRtHr2(),
            dealer.getLabourRtHr3(),
            dealer.getEffPerStartDate1(),
            dealer.getEffPerEndDate1(),
            dealer.getEffPerStartDate2(),
            dealer.getEffPerEndDate2(),
            dealer.getEffPerStartDate3(),
            dealer.getEffPerEndDate3(),
            dealer.getDistLabUplift(),
            dealer.getDistLabourUpliftFactor2(),
            dealer.getDistLabourUpliftFactor3(),
            dealer.getVatPercent()
        );
    }
}