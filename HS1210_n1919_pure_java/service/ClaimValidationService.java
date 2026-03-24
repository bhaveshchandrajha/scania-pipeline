package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimPosition;
import com.scania.warranty.domain.ClaimValidationError;
import com.scania.warranty.domain.RecordType;
import com.scania.warranty.dto.ClaimValidationResultDto;
import com.scania.warranty.repository.ClaimPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimValidationService {

    private final ClaimPositionRepository claimPositionRepository;
    private final ClaimValueService claimValueService;

    public ClaimValidationService(
        ClaimPositionRepository claimPositionRepository,
        ClaimValueService claimValueService
    ) {
        this.claimPositionRepository = claimPositionRepository;
        this.claimValueService = claimValueService;
    }

    @Transactional(readOnly = true)
    public ClaimValidationResultDto validateClaim(
        String kuerzel,
        String claimNumber,
        String errorNumber,
        String sequenceNumber,
        String customerCauseCode,
        Long campaignNumber,
        String customerMainCode,
        String customerSubCode1,
        String workshopCauseCode,
        String workshopMainCode,
        String workshopSubCode1,
        String damageCausingPartNumber
    ) {
        List<ClaimValidationError> errors = new ArrayList<>();

        errors.addAll(validateCustomerCoding(customerCauseCode, campaignNumber, customerMainCode, customerSubCode1));
        errors.addAll(validateWorkshopCoding(workshopCauseCode, workshopMainCode, workshopSubCode1));
        errors.addAll(validateDamageCausingPart(damageCausingPartNumber));
        errors.addAll(validateSpecialCosts(kuerzel, claimNumber, errorNumber));
        errors.addAll(validateClaimValue(kuerzel, claimNumber, errorNumber, sequenceNumber));

        return new ClaimValidationResultDto(
            errors.isEmpty(),
            errors
        );
    }

    private List<ClaimValidationError> validateCustomerCoding(
        String customerCauseCode,
        Long campaignNumber,
        String customerMainCode,
        String customerSubCode1
    ) {
        List<ClaimValidationError> errors = new ArrayList<>();

        if ("3".equals(customerCauseCode != null ? customerCauseCode.trim() : "")) {
            if (campaignNumber == null || campaignNumber == 0) {
                errors.add(ClaimValidationError.of(
                    "Ursache Kampagne aber keine Kampagnen-Nr. eingetragen."
                ));
            }
            if (isNotBlank(customerMainCode) || isNotBlank(customerSubCode1)) {
                errors.add(ClaimValidationError.of(
                    "Schadenscodierung Kunde ungültig."
                ));
            }
        }

        if (isBlank(customerCauseCode)) {
            errors.add(ClaimValidationError.of(
                "Schadenscodierung Kunde ungültig."
            ));
        }

        return errors;
    }

    private List<ClaimValidationError> validateWorkshopCoding(
        String workshopCauseCode,
        String workshopMainCode,
        String workshopSubCode1
    ) {
        List<ClaimValidationError> errors = new ArrayList<>();

        if (isBlank(workshopCauseCode)) {
            errors.add(ClaimValidationError.of(
                "Schadenscodierung Werkstatt ungültig."
            ));
        }

        if ("97".equals(workshopCauseCode != null ? workshopCauseCode.trim() : "")) {
            if (isNotBlank(workshopMainCode) || isNotBlank(workshopSubCode1)) {
                errors.add(ClaimValidationError.of(
                    "Schadenscodierung Werkstatt ungültig."
                ));
            }
        }

        return errors;
    }

    private List<ClaimValidationError> validateDamageCausingPart(String damageCausingPartNumber) {
        List<ClaimValidationError> errors = new ArrayList<>();

        if (isBlank(damageCausingPartNumber)) {
            errors.add(ClaimValidationError.of(
                "Es ist keine schadensverursachende Teilenummer angegeben."
            ));
        }

        return errors;
    }

    private List<ClaimValidationError> validateSpecialCosts(
        String kuerzel,
        String claimNumber,
        String errorNumber
    ) {
        List<ClaimValidationError> errors = new ArrayList<>();

        List<ClaimPosition> positions = claimPositionRepository.findByClaimKey(
            kuerzel,
            claimNumber,
            errorNumber
        );

        boolean hasUncodedSpecialCosts = positions.stream()
            .filter(pos -> RecordType.SMA.getCode().equals(pos.getRecordType()))
            .anyMatch(pos -> isBlank(pos.getResultCode()) || isBlank(pos.getCodeId()));

        if (hasUncodedSpecialCosts) {
            errors.add(ClaimValidationError.of(
                "Es sind Sonderkosten ohne Codierung vorhanden."
            ));
        }

        return errors;
    }

    private List<ClaimValidationError> validateClaimValue(
        String kuerzel,
        String claimNumber,
        String errorNumber,
        String sequenceNumber
    ) {
        List<ClaimValidationError> errors = new ArrayList<>();

        BigDecimal requestedValue = claimValueService.getClaimValues(
            "Requested",
            kuerzel,
            claimNumber,
            errorNumber,
            sequenceNumber
        );

        if (requestedValue.compareTo(BigDecimal.ZERO) == 0) {
            errors.add(ClaimValidationError.of(
                "Antrag mit Wert 0 kann nicht versendet werden."
            ));
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}