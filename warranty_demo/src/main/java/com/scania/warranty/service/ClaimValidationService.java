/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimPosition;
import com.scania.warranty.domain.ClaimValidationResult;
import com.scania.warranty.domain.DamageCauseType;
import com.scania.warranty.domain.SpecialCostType;
import com.scania.warranty.repository.ClaimPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for warranty claim validation business logic.
 */
@Service
public class ClaimValidationService {

    private final ClaimPositionRepository claimPositionRepository;

    public ClaimValidationService(ClaimPositionRepository claimPositionRepository) {
        this.claimPositionRepository = claimPositionRepository;
    }

    @Transactional(readOnly = true)
    public ClaimValidationResult validateClaim(String kuerzel, 
                                               String claimNumber, 
                                               String errorNumber,
                                               String sequenceNumber,
                                               String customerDamageCause,
                                               Long campaignNumber,
                                               String customerMainCause,
                                               String customerSubCause,
                                               String workshopDamageCause,
                                               String workshopMainCause,
                                               String workshopSubCause,
                                               String damageCausingPartNumber) {
        
        List<String> errors = new ArrayList<>();

        // Validation 1: Campaign cause requires campaign number
        // @origin HS1210 L830-833 (IF)
        if (isCampaignCause(customerDamageCause) && isBlank(campaignNumber)) {
            errors.add("Ursache Kampagne aber keine Kampagnen-Nr. eingetragen.");
        }

        // Validation 2: Customer damage coding must be valid (not blank)
        if (isBlank(customerDamageCause)) {
            errors.add("Schadenscodierung Kunde ungültig.");
        }

        // Validation 3: Campaign cause should not have main/sub causes
        if (isCampaignCause(customerDamageCause) && 
            (!isBlank(customerMainCause) || !isBlank(customerSubCause))) {
            errors.add("Schadenscodierung Kunde ungültig.");
        }

        // Validation 4: Workshop damage coding must be valid (not blank)
        if (isBlank(workshopDamageCause)) {
            errors.add("Schadenscodierung Werkstatt ungültig.");
        }

        // Validation 5: Workshop cause '97' should not have main/sub causes
        if (isWorkshopCause97(workshopDamageCause) && 
            (!isBlank(workshopMainCause) || !isBlank(workshopSubCause))) {
            errors.add("Schadenscodierung Werkstatt ungültig.");
        }

        // Validation 6: Damage-causing part number is required
        if (isBlank(damageCausingPartNumber)) {
            errors.add("Es ist keine schadensverursachende Teilenummer angegeben.");
        }

        // Validation 7: Special costs must have coding
        boolean hasUncodedSpecialCosts = checkUncodedSpecialCosts(kuerzel, claimNumber, errorNumber);
        if (hasUncodedSpecialCosts) {
            errors.add("Es sind Sonderkosten ohne Codierung vorhanden.");
        }

        // Validation 8: Claim value must be greater than zero
        BigDecimal totalValue = claimPositionRepository.calculateTotalValueByClaimKey(
            kuerzel, claimNumber, errorNumber, sequenceNumber);
        if (totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Antrag mit Wert 0 kann nicht versendet werden.");
        }

        if (errors.isEmpty()) {
            return ClaimValidationResult.success();
        } else {
            return ClaimValidationResult.failure(errors);
        }
    }

    private boolean isCampaignCause(String customerDamageCause) {
        if (customerDamageCause == null) {
            return false;
        }
        return "3".equals(customerDamageCause.trim());
    }

    private boolean isWorkshopCause97(String workshopDamageCause) {
        if (workshopDamageCause == null) {
            return false;
        }
        return "97".equals(workshopDamageCause.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isBlank(Long value) {
        return value == null || value == 0L;
    }

    private boolean checkUncodedSpecialCosts(String kuerzel, String claimNumber, String errorNumber) {
        // @origin HS1210 L941-941 (CHAIN)
        List<ClaimPosition> positions = claimPositionRepository.findByCompanyCodeAndClaimNumberAndErrorNumber(
            kuerzel, claimNumber, errorNumber);
        
        // @origin HS1210 L884-1012 (DOW)
        for (ClaimPosition position : positions) {
            // @origin HS1210 L914-916 (IF)
            if (SpecialCostType.SPECIAL_COST.getCode().equals(position.getRecordType())) {
                if (isBlank(position.getResultCode()) || isBlank(position.getCodeType())) {
                    return true;
                }
            }
        }
        return false;
    }
}