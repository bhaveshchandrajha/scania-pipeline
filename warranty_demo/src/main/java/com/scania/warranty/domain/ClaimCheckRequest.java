/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.domain;

public record ClaimCheckRequest(
        String companyCode,
        String claimNumber,
        String claimSequence,
        String claimSubSequence,
        String customerDamageCode1,
        String customerDamageCode2,
        String customerDamageCode3,
        String workshopDamageCode1,
        String workshopDamageCode2,
        String workshopDamageCode3,
        String campaignNumber,
        String damageCausingPartNumber
) {
}