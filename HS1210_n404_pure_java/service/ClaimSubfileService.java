package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import com.scania.warranty.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClaimSubfileService {
    
    private final HSG71LF2Repository claimRepository;
    private final HSG73PFRepository claimDetailRepository;
    private final HSAHKLF3Repository invoiceRepository;
    private final HSAHWPFRepository workRepository;
    private final HSFLALF1Repository externalServiceRepository;
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Autowired
    public ClaimSubfileService(
            HSG71LF2Repository claimRepository,
            HSG73PFRepository claimDetailRepository,
            HSAHKLF3Repository invoiceRepository,
            HSAHWPFRepository workRepository,
            HSFLALF1Repository externalServiceRepository) {
        this.claimRepository = claimRepository;
        this.claimDetailRepository = claimDetailRepository;
        this.invoiceRepository = invoiceRepository;
        this.workRepository = workRepository;
        this.externalServiceRepository = externalServiceRepository;
    }
    
    public List<ClaimListItemDto> buildClaimSubfile(ClaimSearchCriteria criteria) {
        List<HSG71LF2> claims;
        
        if (criteria.sortAscending()) {
            claims = claimRepository.findByPakzOrderByClaimnrAsc(criteria.pakz());
        } else {
            claims = claimRepository.findByPakzOrderByClaimnrDesc(criteria.pakz());
        }
        
        return claims.stream()
            .filter(claim -> applyFilters(claim, criteria))
            .map(this::toListItemDto)
            .collect(Collectors.toList());
    }
    
    private boolean applyFilters(HSG71LF2 claim, ClaimSearchCriteria criteria) {
        if (criteria.filterClaimAge() != null && criteria.filterClaimAge() > 0) {
            if (!isClaimWithinAgeLimit(claim, criteria.filterClaimAge())) {
                return false;
            }
        }
        
        if (criteria.filterClaimType() != null && !criteria.filterClaimType().isBlank()) {
            if (!matchesClaimType(claim, criteria.filterClaimType())) {
                return false;
            }
        }
        
        if (criteria.filterOpenOnly() && !isOpenClaim(claim)) {
            return false;
        }
        
        if (criteria.statusFilter() != null) {
            if (!matchesStatusFilter(claim, criteria.statusFilter(), criteria.statusComparison())) {
                return false;
            }
        }
        
        if (criteria.searchString() != null && !criteria.searchString().isBlank()) {
            if (!matchesSearchString(claim, criteria.searchString())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isClaimWithinAgeLimit(HSG71LF2 claim, int maxDays) {
        if (claim.getRepdatum() == null || claim.getRepdatum() == 0) {
            return true;
        }
        
        try {
            LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getRepdatum()), ISO_DATE_FORMATTER);
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(repairDate, currentDate);
            return daysBetween <= maxDays;
        } catch (Exception e) {
            return true;
        }
    }
    
    private boolean matchesClaimType(HSG71LF2 claim, String claimType) {
        List<HSG73PF> details = claimDetailRepository.findByPakzAndClaimnr(
            claim.getPakz(), claim.getClaimnr());
        
        return details.stream()
            .anyMatch(detail -> matchesTypeCode(detail, claimType));
    }
    
    private boolean matchesTypeCode(HSG73PF detail, String claimType) {
        return true;
    }
    
    private boolean isOpenClaim(HSG71LF2 claim) {
        if (claim.getStatuscodesde() != null && 
            (claim.getStatuscodesde() < 20 && claim.getStatuscodesde() != 5)) {
            return true;
        }
        
        List<HSG73PF> details = claimDetailRepository.findByPakzAndClaimnr(
            claim.getPakz(), claim.getClaimnr());
        
        if (details.isEmpty()) {
            return true;
        }
        
        return details.stream().anyMatch(detail -> detail.getStatuscode() == null || detail.getStatuscode() == 0);
    }
    
    private boolean matchesStatusFilter(HSG71LF2 claim, Integer statusFilter, String comparison) {
        Integer claimStatus = claim.getStatuscodesde();
        if (claimStatus == null) {
            return false;
        }
        
        if (comparison == null || comparison.isBlank()) {
            comparison = "=";
        }
        
        switch (comparison) {
            case "=":
            case "*":
                return claimStatus.equals(statusFilter);
            case ">":
                return claimStatus > statusFilter;
            case "<":
                return claimStatus < statusFilter;
            default:
                return claimStatus.equals(statusFilter);
        }
    }
    
    private boolean matchesSearchString(HSG71LF2 claim, String searchString) {
        String searchUpper = searchString.toUpperCase();
        
        String combinedFields = String.format("%s%s%s%s%s%s%s%s",
            claim.getPakz() != null ? claim.getPakz() : "",
            claim.getAuftragsnr() != null ? claim.getAuftragsnr() : "",
            claim.getRechdatum() != null ? claim.getRechdatum() : "",
            claim.getClaimnrsde() != null ? claim.getClaimnrsde() : "",
            claim.getClaimnr() != null ? claim.getClaimnr() : "",
            claim.getRechnr() != null ? claim.getRechnr() : "",
            claim.getChassisnr() != null ? claim.getChassisnr() : "",
            claim.getKdname() != null ? claim.getKdname() : ""
        ).toUpperCase();
        
        return combinedFields.contains(searchUpper);
    }
    
    private ClaimListItemDto toListItemDto(HSG71LF2 claim) {
        String statusText = getStatusText(claim);
        String colorIndicator = determineColorIndicator(claim);
        
        return new ClaimListItemDto(
            claim.getPakz(),
            claim.getRechnr(),
            formatDate(claim.getRechdatum()),
            claim.getAuftragsnr(),
            claim.getClaimnr(),
            claim.getChassisnr(),
            claim.getKennzeichen(),
            formatDate(String.valueOf(claim.getRepdatum())),
            claim.getKmstand(),
            claim.getKdnr(),
            claim.getKdname(),
            claim.getClaimnrsde(),
            claim.getStatuscodesde(),
            statusText,
            claim.getAnzfehler(),
            colorIndicator
        );
    }
    
    private String getStatusText(HSG71LF2 claim) {
        if (claim.getClaimnrsde() == null || claim.getClaimnrsde().equals("00000000")) {
            if (claim.getStatuscodesde() != null) {
                if (claim.getStatuscodesde() == 5) {
                    return "Minimumantrag";
                } else if (claim.getStatuscodesde() == 20) {
                    return "Minimum ausgebucht";
                }
            }
            return "Minimumantrag";
        }
        return "";
    }
    
    private String determineColorIndicator(HSG71LF2 claim) {
        if (claim.getClaimnrsde() == null || claim.getClaimnrsde().isBlank()) {
            return "";
        }
        
        List<HSG73PF> details = claimDetailRepository.findByPakzAndClaimnr(
            claim.getPakz(), claim.getClaimnr());
        
        if (details.isEmpty() && claim.getStatuscodesde() != null && claim.getStatuscodesde() == 20) {
            return "ROT";
        }
        
        boolean hasError = details.stream()
            .anyMatch(d -> d.getStatuscode() != null && 
                (d.getStatuscode() == 16 || d.getStatuscode() == 30 || d.getStatuscode() == 0));
        
        if (hasError) {
            return "ROT";
        }
        
        boolean hasRejection = details.stream()
            .anyMatch(d -> d.getStatuscode() != null && d.getStatuscode() == 11);
        
        if (hasRejection) {
            return "GELB";
        }
        
        boolean hasWaiting = details.stream()
            .anyMatch(d -> d.getStatuscode() != null && 
                (d.getStatuscode() == 3 || d.getStatuscode() == 11));
        
        if (hasWaiting) {
            return "BLAU";
        }
        
        return "";
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.length() != 8) {
            return "";
        }
        
        try {
            String day = dateString.substring(6, 8);
            String month = dateString.substring(4, 6);
            String year = dateString.substring(0, 4);
            return day + "." + month + "." + year;
        } catch (Exception e) {
            return dateString;
        }
    }
    
    public void updateClaimStatus(String pakz, String claimnr, Integer newStatus) {
        HSG71LF2 claim = claimRepository.findByPakzAndClaimnr(pakz, claimnr)
            .orElseThrow(() -> new ClaimNotFoundException(pakz, claimnr));
        
        claim.setStatuscodesde(newStatus);
        claimRepository.save(claim);
    }
    
    public void deleteClaimWithDetails(String pakz, String claimnr) {
        HSG71LF2 claim = claimRepository.findByPakzAndClaimnr(pakz, claimnr)
            .orElseThrow(() -> new ClaimNotFoundException(pakz, claimnr));
        
        claim.setStatuscodesde(99);
        claimRepository.save(claim);
        
        List<HSG73PF> details = claimDetailRepository.findByPakzAndClaimnr(pakz, claimnr);
        claimDetailRepository.deleteAll(details);
    }
}