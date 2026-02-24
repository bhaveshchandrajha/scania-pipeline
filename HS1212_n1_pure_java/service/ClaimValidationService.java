package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimType;
import com.scania.warranty.repository.ClaimErrorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ClaimValidationService {

    private final ClaimErrorRepository claimErrorRepository;

    public ClaimValidationService(ClaimErrorRepository claimErrorRepository) {
        this.claimErrorRepository = claimErrorRepository;
    }

    public boolean validateClaimError(String claimNumber, String errorNumber,
                                     String damagingPart, String mainGroup,
                                     String damageCode, String controlCode,
                                     String claimType, Integer previousRepairDate,
                                     Integer previousMileage) {
        
        if (claimType == null || claimType.isEmpty()) {
            return false;
        }
        
        ClaimType type = ClaimType.fromCode(claimType);
        if (type == ClaimType.ORIGINAL_PART) {
            if (previousRepairDate == null || previousRepairDate == 0) {
                return false;
            }
            if (previousMileage == null || previousMileage == 0) {
                return false;
            }
        }
        
        if (mainGroup == null || mainGroup.isEmpty()) {
            return false;
        }
        
        if (damageCode == null || damageCode.isEmpty()) {
            return false;
        }
        
        if (controlCode == null || controlCode.isEmpty()) {
            return false;
        }
        
        if (damagingPart == null || damagingPart.isEmpty()) {
            if (!claimType.equals("4") && !claimType.equals("6")) {
                return false;
            }
        }
        
        return true;
    }

    public boolean validateControlCode(String controlCode, String chassisNumber,
                                      Integer productType, Integer repairDate) {
        
        if (controlCode == null || controlCode.isEmpty()) {
            return false;
        }
        
        if (controlCode.equals("AR") && (chassisNumber == null || chassisNumber.isEmpty())) {
            return false;
        }
        
        return true;
    }

    public boolean checkPreviousRepair(String claimNumber, Integer previousRepairDate,
                                      Integer currentRepairDate, Integer previousMileage,
                                      Integer currentMileage) {
        
        if (previousRepairDate == null || previousRepairDate == 0) {
            return true;
        }
        
        if (previousRepairDate > currentRepairDate) {
            return false;
        }
        
        if (previousMileage != null && previousMileage > 0) {
            if (currentMileage != null && previousMileage > currentMileage) {
                return false;
            }
        }
        
        return true;
    }

    public boolean validateDescriptions(String description1, String description2,
                                       String description3, String description4) {
        
        if ((description1 == null || description1.isEmpty()) &&
            (description2 == null || description2.isEmpty()) &&
            (description3 == null || description3.isEmpty()) &&
            (description4 == null || description4.isEmpty())) {
            return false;
        }
        
        return true;
    }

    public boolean checkErrorStatus(String claimNumber, String errorNumber, Integer status) {
        Optional<ClaimError> errorOpt = claimErrorRepository
            .findByClaimNumberAndErrorNumberAndReconNumber(claimNumber, errorNumber, "00");
        
        if (errorOpt.isEmpty()) {
            return false;
        }
        
        ClaimError error = errorOpt.get();
        
        if (error.getStatus() > 0 && error.getStatus() < 30 && error.getStatus() != 16) {
            return false;
        }
        
        return true;
    }

    public boolean validateCampaignClaim(String campaignNumber, String claimType) {
        if (campaignNumber != null && !campaignNumber.isEmpty() &&
            !campaignNumber.equals("00000000")) {
            
            if (!claimType.equals("1")) {
                return false;
            }
        }
        
        return true;
    }

    public boolean checkMinimumStatus(String sdeClaimNumber, Integer status) {
        if (sdeClaimNumber != null && sdeClaimNumber.equals("00000000")) {
            if (status != null && status == 20) {
                return false;
            }
        }
        
        return true;
    }
}