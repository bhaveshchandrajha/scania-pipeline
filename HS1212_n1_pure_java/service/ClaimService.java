package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.ClaimDto;
import com.scania.warranty.dto.ClaimErrorDto;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.repository.DealerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final DealerRepository dealerRepository;

    public ClaimService(ClaimRepository claimRepository,
                        ClaimErrorRepository claimErrorRepository,
                        DealerRepository dealerRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.dealerRepository = dealerRepository;
    }

    public Optional<ClaimDto> findClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
            .map(ClaimDto::fromEntity);
    }

    public List<ClaimListItemDto> searchClaims(ClaimSearchCriteria criteria) {
        return claimRepository.searchClaims(
            criteria.dealerId(),
            criteria.invoiceNumber(),
            criteria.invoiceDate(),
            criteria.orderNumber(),
            criteria.chassisNumber(),
            criteria.registrationNumber()
        ).stream()
            .filter(claim -> claim.getSdeStatusCode() >= criteria.statusFrom() &&
                           claim.getSdeStatusCode() <= criteria.statusTo())
            .map(ClaimListItemDto::fromEntity)
            .collect(Collectors.toList());
    }

    public ClaimDto createClaim(String dealerId, String invoiceNumber, String invoiceDate,
                                String orderNumber, String wete, String chassisNumber,
                                String registrationNumber, Integer registrationDate,
                                Integer repairDate, Integer mileage, Integer productType) {
        
        String claimNumber = generateClaimNumber(dealerId, invoiceNumber, invoiceDate, orderNumber);
        
        Claim claim = new Claim(
            claimNumber,
            dealerId,
            invoiceNumber,
            invoiceDate,
            orderNumber,
            wete,
            chassisNumber != null ? chassisNumber : "",
            registrationNumber != null ? registrationNumber : "",
            registrationDate != null ? registrationDate : 0,
            repairDate,
            mileage != null ? mileage : 0,
            productType,
            "",
            "",
            "",
            "",
            "",
            ClaimStatus.CREATED.getCode(),
            0,
            determineArea(productType),
            orderNumber
        );
        
        Claim savedClaim = claimRepository.save(claim);
        return ClaimDto.fromEntity(savedClaim);
    }

    public ClaimDto updateClaim(String claimNumber, Integer registrationDate, Integer repairDate,
                               Integer mileage, String chassisNumber, String registrationNumber) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
        
        if (registrationDate != null) {
            claim.setRegistrationDate(registrationDate);
        }
        if (repairDate != null) {
            claim.setRepairDate(repairDate);
        }
        if (mileage != null) {
            claim.setMileage(mileage);
        }
        if (chassisNumber != null) {
            claim.setChassisNumber(chassisNumber);
        }
        if (registrationNumber != null) {
            claim.setRegistrationNumber(registrationNumber);
        }
        
        Claim updatedClaim = claimRepository.save(claim);
        return ClaimDto.fromEntity(updatedClaim);
    }

    public ClaimErrorDto createClaimError(String claimNumber, String errorNumber,
                                         String damagingPart, String mainGroup,
                                         String damageCode, String controlCode,
                                         String claimType, String description1,
                                         String description2) {
        
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
        
        ClaimError error = new ClaimError();
        error.setClaim(claim);
        error.setDealerId(claim.getDealerId());
        error.setClaimNumber(claimNumber);
        error.setErrorNumber(errorNumber);
        error.setReconNumber("00");
        error.setDamagingPart(damagingPart != null ? damagingPart : "");
        error.setMainGroup(mainGroup != null ? mainGroup : "");
        error.setDamageCode(damageCode != null ? damageCode : "");
        error.setSubGroup("");
        error.setDescription1(description1 != null ? description1 : "");
        error.setDescription2(description2 != null ? description2 : "");
        error.setDescription3("");
        error.setDescription4("");
        error.setControlCode(controlCode != null ? controlCode : "");
        error.setMaterialPercentage(100);
        error.setLabourPercentage(100);
        error.setSpecialPercentage(100);
        error.setClaimType(claimType != null ? claimType : "1");
        error.setPreviousRepairDate(0);
        error.setPreviousMileage(0);
        error.setPreviousRepairNumber(0);
        error.setCampaignNumber("");
        error.setEpsName("");
        error.setStatus(ClaimStatus.CREATED.getCode());
        error.setAlternateErrorNumber("");
        error.setCodeC1("");
        error.setCodeC2("");
        error.setCodeC3("");
        error.setCodeC4("");
        error.setCodeC5("");
        error.setCodeC6("");
        error.setResultCode("");
        error.setResultType("");
        error.setResultId("");
        error.setFaultType("");
        error.setFaultId("");
        error.setRepairType("");
        error.setRepairId("");
        error.setExplanationType("");
        error.setExplanationId("");
        
        ClaimError savedError = claimErrorRepository.save(error);
        
        claim.setErrorCount(claim.getErrorCount() + 1);
        claimRepository.save(claim);
        
        return ClaimErrorDto.fromEntity(savedError);
    }

    public ClaimErrorDto updateClaimError(String claimNumber, String errorNumber,
                                         String damagingPart, String mainGroup,
                                         String damageCode, String controlCode,
                                         String description1, String description2,
                                         String description3, String description4) {
        
        ClaimError error = claimErrorRepository.findByClaimNumberAndErrorNumberAndReconNumber(
            claimNumber, errorNumber, "00")
            .orElseThrow(() -> new RuntimeException("Claim error not found"));
        
        if (damagingPart != null) {
            error.setDamagingPart(damagingPart);
        }
        if (mainGroup != null) {
            error.setMainGroup(mainGroup);
        }
        if (damageCode != null) {
            error.setDamageCode(damageCode);
        }
        if (controlCode != null) {
            error.setControlCode(controlCode);
        }
        if (description1 != null) {
            error.setDescription1(description1);
        }
        if (description2 != null) {
            error.setDescription2(description2);
        }
        if (description3 != null) {
            error.setDescription3(description3);
        }
        if (description4 != null) {
            error.setDescription4(description4);
        }
        
        ClaimError updatedError = claimErrorRepository.save(error);
        return ClaimErrorDto.fromEntity(updatedError);
    }

    public void deleteClaimError(String claimNumber, String errorNumber) {
        claimErrorRepository.deleteByClaimNumberAndErrorNumber(claimNumber, errorNumber);
        
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
        
        if (claim.getErrorCount() > 0) {
            claim.setErrorCount(claim.getErrorCount() - 1);
            claimRepository.save(claim);
        }
    }

    public List<ClaimErrorDto> getClaimErrors(String claimNumber) {
        return claimErrorRepository.findByClaimNumber(claimNumber).stream()
            .map(ClaimErrorDto::fromEntity)
            .collect(Collectors.toList());
    }

    public ClaimDto copyClaimError(String sourceClaimNumber, String sourceErrorNumber,
                                  String targetClaimNumber) {
        
        ClaimError sourceError = claimErrorRepository.findByClaimNumberAndErrorNumberAndReconNumber(
            sourceClaimNumber, sourceErrorNumber, "00")
            .orElseThrow(() -> new RuntimeException("Source error not found"));
        
        Claim targetClaim = claimRepository.findByClaimNumber(targetClaimNumber)
            .orElseThrow(() -> new RuntimeException("Target claim not found"));
        
        String nextErrorNumber = claimErrorRepository.findMaxErrorNumber(targetClaimNumber)
            .map(max -> String.format("%02d", Integer.parseInt(max) + 1))
            .orElse("01");
        
        ClaimError newError = new ClaimError();
        newError.setClaim(targetClaim);
        newError.setDealerId(targetClaim.getDealerId());
        newError.setClaimNumber(targetClaimNumber);
        newError.setErrorNumber(nextErrorNumber);
        newError.setReconNumber("00");
        newError.setDamagingPart(sourceError.getDamagingPart());
        newError.setMainGroup(sourceError.getMainGroup());
        newError.setDamageCode(sourceError.getDamageCode());
        newError.setSubGroup(sourceError.getSubGroup());
        newError.setDescription1(sourceError.getDescription1());
        newError.setDescription2(sourceError.getDescription2());
        newError.setDescription3(sourceError.getDescription3());
        newError.setDescription4(sourceError.getDescription4());
        newError.setControlCode(sourceError.getControlCode());
        newError.setMaterialPercentage(sourceError.getMaterialPercentage());
        newError.setLabourPercentage(sourceError.getLabourPercentage());
        newError.setSpecialPercentage(sourceError.getSpecialPercentage());
        newError.setClaimType(sourceError.getClaimType());
        newError.setPreviousRepairDate(0);
        newError.setPreviousMileage(0);
        newError.setPreviousRepairNumber(0);
        newError.setCampaignNumber(sourceError.getCampaignNumber());
        newError.setEpsName(sourceError.getEpsName());
        newError.setStatus(ClaimStatus.CREATED.getCode());
        newError.setAlternateErrorNumber("");
        newError.setCodeC1(sourceError.getCodeC1());
        newError.setCodeC2(sourceError.getCodeC2());
        newError.setCodeC3(sourceError.getCodeC3());
        newError.setCodeC4(sourceError.getCodeC4());
        newError.setCodeC5(sourceError.getCodeC5());
        newError.setCodeC6(sourceError.getCodeC6());
        newError.setResultCode("");
        newError.setResultType("");
        newError.setResultId("");
        newError.setFaultType("");
        newError.setFaultId("");
        newError.setRepairType("");
        newError.setRepairId("");
        newError.setExplanationType("");
        newError.setExplanationId("");
        
        claimErrorRepository.save(newError);
        
        targetClaim.setErrorCount(targetClaim.getErrorCount() + 1);
        Claim updatedClaim = claimRepository.save(targetClaim);
        
        return ClaimDto.fromEntity(updatedClaim);
    }

    public boolean validateClaimData(String chassisNumber, Integer productType,
                                    Integer registrationDate, Integer repairDate,
                                    Integer mileage) {
        
        if (productType == null || (productType < 1 || productType > 5)) {
            return false;
        }
        
        if (chassisNumber != null && !chassisNumber.isEmpty()) {
            if (mileage == null || mileage <= 0) {
                return false;
            }
            if (mileage > 2000 && (registrationDate == null || registrationDate == 0)) {
                return false;
            }
        } else {
            if (mileage != null && mileage != 0) {
                return false;
            }
        }
        
        if (repairDate == null || repairDate == 0) {
            return false;
        }
        
        String repairDateStr = String.valueOf(repairDate);
        if (repairDateStr.length() != 8) {
            return false;
        }
        
        try {
            int year = Integer.parseInt(repairDateStr.substring(0, 4));
            int month = Integer.parseInt(repairDateStr.substring(4, 6));
            int day = Integer.parseInt(repairDateStr.substring(6, 8));
            
            if (year < 1980 || month < 1 || month > 12 || day < 1 || day > 31) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }

    private String generateClaimNumber(String dealerId, String invoiceNumber,
                                      String invoiceDate, String orderNumber) {
        
        Optional<String> maxClaimNumber = claimRepository.findMaxClaimNumber(
            dealerId, invoiceNumber, invoiceDate, orderNumber);
        
        if (maxClaimNumber.isPresent()) {
            String max = maxClaimNumber.get();
            int nextNumber = Integer.parseInt(max) + 1;
            return String.format("%08d", nextNumber);
        } else {
            Dealer dealer = dealerRepository.findByDistWarrantyCustNo(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found: " + dealerId));
            
            Integer startNo = dealer.getCurrClaimRgStartNo();
            if (startNo == null || startNo == 0) {
                startNo = 10000000;
            }
            return String.format("%08d", startNo);
        }
    }

    private String determineArea(Integer productType) {
        if (productType == null) {
            return "1";
        }
        
        ProductType type = ProductType.fromCode(productType);
        return switch (type) {
            case BUS -> "3";
            case ENGINE -> "6";
            default -> "1";
        };
    }

    public void updateClaimStatus(String claimNumber, Integer newStatus) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
        
        claim.setSdeStatusCode(newStatus);
        claimRepository.save(claim);
    }

    public boolean checkMinimumClaim(String claimNumber) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
            .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
        
        return claim.getSdeClaimNumber() != null &&
               claim.getSdeClaimNumber().equals("00000000");
    }

    public List<ClaimErrorDto> getCampaignErrors(String claimNumber) {
        return claimErrorRepository.findCampaignErrors(claimNumber).stream()
            .map(ClaimErrorDto::fromEntity)
            .collect(Collectors.toList());
    }
}