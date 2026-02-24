package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClaimSearchService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkPositionRepository workPositionRepository;
    private final HSFLALF1Repository hsflalf1Repository;
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Autowired
    public ClaimSearchService(
            ClaimRepository claimRepository,
            ClaimErrorRepository claimErrorRepository,
            InvoiceRepository invoiceRepository,
            WorkPositionRepository workPositionRepository,
            HSFLALF1Repository hsflalf1Repository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.workPositionRepository = workPositionRepository;
        this.hsflalf1Repository = hsflalf1Repository;
    }
    
    public List<ClaimListItemDto> searchClaims(ClaimSearchCriteria criteria, boolean ascending) {
        List<Claim> claims;
        
        // If pakz is provided, filter by pakz; otherwise get all claims
        if (criteria.pakz() != null && !criteria.pakz().isBlank()) {
            if (ascending) {
                claims = claimRepository.findByPakzOrderByClaimNrAsc(criteria.pakz());
            } else {
                claims = claimRepository.findByPakzOrderByClaimNrDesc(criteria.pakz());
            }
        } else {
            // Get all claims when pakz is not specified
            if (ascending) {
                claims = claimRepository.findAllOrderByClaimNrAsc();
            } else {
                claims = claimRepository.findAllOrderByClaimNrDesc();
            }
        }
        
        return claims.stream()
                .filter(claim -> applyFilters(claim, criteria))
                .map(this::toListItemDto)
                .collect(Collectors.toList());
    }
    
    public List<ClaimListItemDto> getAllClaims(boolean ascending) {
        List<Claim> claims;
        if (ascending) {
            claims = claimRepository.findAllOrderByClaimNrAsc();
        } else {
            claims = claimRepository.findAllOrderByClaimNrDesc();
        }
        
        return claims.stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());
    }
    
    private boolean applyFilters(Claim claim, ClaimSearchCriteria criteria) {
        if (criteria.filterDays() != null && criteria.filterDays() > 0) {
            if (!checkClaimAge(claim, criteria.filterDays())) {
                return false;
            }
        }
        
        if (criteria.claimType() != null && !criteria.claimType().isBlank()) {
            if (!checkClaimType(claim, criteria.claimType())) {
                return false;
            }
        }
        
        if (criteria.openClaimsOnly()) {
            if (!isOpenClaim(claim)) {
                return false;
            }
        }
        
        if (criteria.status() != null && !criteria.status().isBlank()) {
            if (!matchesStatusFilter(claim, criteria.status(), criteria.filterOperator())) {
                return false;
            }
        }
        
        if (criteria.vehicleNumber() != null && !criteria.vehicleNumber().isBlank()) {
            if (!claim.getChassisNr().equals(criteria.vehicleNumber())) {
                return false;
            }
        }
        
        if (criteria.customerNumber() != null && !criteria.customerNumber().isBlank()) {
            if (!claim.getKdNr().equals(criteria.customerNumber())) {
                return false;
            }
        }
        
        if (criteria.claimNumberSde() != null && !criteria.claimNumberSde().isBlank()) {
            if (!claim.getClaimNrSde().equals(criteria.claimNumberSde())) {
                return false;
            }
        }
        
        if (criteria.minimumOnly()) {
            if (!"00000000".equals(claim.getClaimNrSde())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkClaimAge(Claim claim, int maxDays) {
        try {
            LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getRepDatum()), ISO_DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(repairDate, today);
            return daysBetween <= maxDays;
        } catch (Exception e) {
            return true;
        }
    }
    
    private boolean checkClaimType(Claim claim, String claimType) {
        List<ClaimError> errors = claimErrorRepository.findByPakzAndClaimNr(claim.getPakz(), claim.getClaimNr());
        
        for (ClaimError error : errors) {
            String scope = determineScope(error);
            if (claimType.equals(scope)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String determineScope(ClaimError error) {
        if (error.getHauptgruppe() != null && !error.getHauptgruppe().isBlank()) {
            return error.getHauptgruppe();
        }
        if (error.getNebengruppe() != null && !error.getNebengruppe().isBlank()) {
            return error.getNebengruppe();
        }
        if (error.getClaimArt() != null) {
            return error.getClaimArt() == 1 ? "G" : "T";
        }
        return "G";
    }
    
    private boolean isOpenClaim(Claim claim) {
        Integer statusCode = claim.getStatusCodeSde();
        
        if (statusCode != null && statusCode < 20 && statusCode != 5) {
            return true;
        }
        
        List<ClaimError> errors = claimErrorRepository.findByPakzAndClaimNr(claim.getPakz(), claim.getClaimNr());
        
        if (errors.isEmpty()) {
            return true;
        }
        
        for (ClaimError error : errors) {
            if (error.getStatusCode() == null || error.getStatusCode() == 0) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matchesStatusFilter(Claim claim, String statusFilter, String operator) {
        try {
            int filterStatus = Integer.parseInt(statusFilter);
            Integer claimStatus = claim.getStatusCodeSde();
            
            if (claimStatus == null) {
                return false;
            }
            
            if (operator == null || operator.isBlank() || operator.equals("=") || operator.equals("*")) {
                return claimStatus == filterStatus;
            } else if (operator.equals(">")) {
                return claimStatus > filterStatus;
            } else if (operator.equals("<")) {
                return claimStatus < filterStatus;
            }
            
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private ClaimListItemDto toListItemDto(Claim claim) {
        String statusText = getStatusText(claim);
        String colorIndicator = determineColorIndicator(claim);
        
        return new ClaimListItemDto(
                claim.getPakz(),
                claim.getRechNr(),
                formatDate(claim.getRechDatum()),
                claim.getAuftragsNr(),
                claim.getClaimNr(),
                claim.getChassisNr(),
                claim.getKennzeichen(),
                formatDate(String.valueOf(claim.getRepDatum())),
                claim.getKmStand(),
                claim.getKdNr(),
                claim.getKdName(),
                claim.getClaimNrSde(),
                claim.getStatusCodeSde(),
                statusText,
                claim.getAnzFehler(),
                colorIndicator
        );
    }
    
    private String getStatusText(Claim claim) {
        if ("00000000".equals(claim.getClaimNrSde())) {
            Integer statusCode = claim.getStatusCodeSde();
            if (statusCode != null) {
                if (statusCode == 5) {
                    return "Minimumantrag";
                } else if (statusCode == 20) {
                    return "Minimum ausgebucht";
                }
            }
            return "Minimumantrag";
        }
        
        return "";
    }
    
    private String determineColorIndicator(Claim claim) {
        List<ClaimError> errors = claimErrorRepository.findByPakzAndClaimNr(claim.getPakz(), claim.getClaimNr());
        
        boolean hasError = false;
        boolean hasWarning = false;
        boolean hasInfo = false;
        
        for (ClaimError error : errors) {
            Integer statusCode = error.getStatusCode();
            
            if (statusCode != null) {
                if (statusCode == 16 || statusCode == 30 || statusCode == 0) {
                    hasError = true;
                } else if (statusCode == 11) {
                    hasWarning = true;
                } else if (statusCode == 3 || statusCode == 11) {
                    hasInfo = true;
                }
            }
        }
        
        if (hasError) {
            return "ROT";
        } else if (hasWarning) {
            return "GELB";
        } else if (hasInfo) {
            return "BLAU";
        }
        
        return "";
    }
    
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() != 8) {
            return "";
        }
        
        try {
            String day = isoDate.substring(6, 8);
            String month = isoDate.substring(4, 6);
            String year = isoDate.substring(0, 4);
            return day + "." + month + "." + year;
        } catch (Exception e) {
            return isoDate;
        }
    }
    
    public Optional<Claim> findClaimByNumber(String pakz, String claimNumber) {
        return claimRepository.findByPakzAndClaimNr(pakz, claimNumber);
    }
    
    @Transactional
    public void deleteClaim(String pakz, String claimNumber) {
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNumber);
        
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            claim.setStatusCodeSde(99);
            claimRepository.save(claim);
            
            claimErrorRepository.deleteByPakzAndClaimNr(pakz, claimNumber);
        }
    }
}