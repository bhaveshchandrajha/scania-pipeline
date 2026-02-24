package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.dto.ClaimSearchResultDto;
import com.scania.warranty.repository.ClaimFailureRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClaimSearchService {
    
    private final ClaimRepository claimRepository;
    private final ClaimFailureRepository claimFailureRepository;
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ClaimSearchService(ClaimRepository claimRepository, 
                             ClaimFailureRepository claimFailureRepository) {
        this.claimRepository = claimRepository;
        this.claimFailureRepository = claimFailureRepository;
    }

    public ClaimSearchResultDto searchClaims(ClaimSearchCriteria criteria, boolean ascending) {
        try {
            // Handle empty company code
            String companyCode = criteria.companyCode();
            if (companyCode == null || companyCode.isBlank()) {
                // Return empty result instead of error
                return new ClaimSearchResultDto(
                    List.of(),
                    0,
                    criteria
                );
            }

            List<Claim> claims = ascending 
                ? claimRepository.findByCompanyCodeOrderByClaimNumberAsc(companyCode)
                : claimRepository.findByCompanyCodeOrderByClaimNumberDesc(companyCode);

            // Handle null result
            if (claims == null) {
                claims = List.of();
            }

            List<ClaimListItemDto> filteredClaims = claims.stream()
                .filter(claim -> applyFilters(claim, criteria))
                .map(this::toListItemDto)
                .collect(Collectors.toList());

            return new ClaimSearchResultDto(
                filteredClaims,
                filteredClaims.size(),
                criteria
            );
        } catch (Exception e) {
            // Log error and return empty result
            e.printStackTrace();
            return new ClaimSearchResultDto(
                List.of(),
                0,
                criteria
            );
        }
    }

    private boolean applyFilters(Claim claim, ClaimSearchCriteria criteria) {
        // Status filter
        if (criteria.statusFilter() != null && !criteria.statusFilter().isBlank()) {
            Integer statusCode = Integer.parseInt(criteria.statusFilter());
            if (!matchesStatusFilter(claim.getStatusCodeSde(), statusCode, criteria.statusOperator())) {
                return false;
            }
        }

        // Claim age filter
        if (criteria.claimAgeDays() != null && criteria.claimAgeDays() > 0) {
            if (!isClaimWithinAgeDays(claim, criteria.claimAgeDays())) {
                claim.setStatusCodeSde(ClaimStatus.EXCLUDED.getCode());
                return false;
            }
        }

        // Claim type filter
        if (criteria.claimType() != null && !criteria.claimType().isBlank()) {
            if (!matchesClaimType(claim, criteria.claimType())) {
                claim.setStatusCodeSde(ClaimStatus.EXCLUDED.getCode());
                return false;
            }
        }

        // Open claims only filter
        if (criteria.openClaimsOnly()) {
            if (!isOpenClaim(claim)) {
                claim.setStatusCodeSde(ClaimStatus.EXCLUDED.getCode());
                return false;
            }
        }

        // Minimum only filter
        if (criteria.minimumOnly()) {
            if (!"00000000".equals(claim.getClaimNumberSde())) {
                return false;
            }
        }

        // Vehicle number filter
        if (criteria.vehicleNumber() != null && !criteria.vehicleNumber().isBlank()) {
            if (!claim.getChassisNumber().contains(criteria.vehicleNumber())) {
                return false;
            }
        }

        // Customer number filter
        if (criteria.customerNumber() != null && !criteria.customerNumber().isBlank()) {
            if (!claim.getCustomerNumber().equals(criteria.customerNumber())) {
                return false;
            }
        }

        // SDE claim number filter
        if (criteria.claimNumberSde() != null && !criteria.claimNumberSde().isBlank()) {
            if (!claim.getClaimNumberSde().equals(criteria.claimNumberSde())) {
                return false;
            }
        }

        // Search text filter
        if (criteria.searchText() != null && !criteria.searchText().isBlank()) {
            String searchLower = criteria.searchText().toLowerCase();
            if (!matchesSearchText(claim, searchLower)) {
                return false;
            }
        }

        // Exclude claims with status 99
        return claim.getStatusCodeSde() != ClaimStatus.EXCLUDED.getCode();
    }

    private boolean matchesStatusFilter(Integer claimStatus, Integer filterStatus, String operator) {
        if (claimStatus == null) {
            return false;
        }
        
        return switch (operator) {
            case "=" -> claimStatus.equals(filterStatus);
            case ">" -> claimStatus > filterStatus;
            case "<" -> claimStatus < filterStatus;
            case "*" -> claimStatus.equals(filterStatus);
            default -> claimStatus.equals(filterStatus);
        };
    }

    private boolean isClaimWithinAgeDays(Claim claim, int maxDays) {
        try {
            LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getRepairDate()), ISO_DATE_FORMATTER);
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(repairDate, currentDate);
            return daysBetween <= maxDays;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean matchesClaimType(Claim claim, String claimType) {
        List<ClaimFailure> failures = claimFailureRepository.findByCompanyCodeAndClaimNumber(
            claim.getCompanyCode(), 
            claim.getClaimNumber()
        );
        
        // Implementation depends on business logic for claim type determination
        // This is a placeholder
        return true;
    }

    private boolean isOpenClaim(Claim claim) {
        Integer status = claim.getStatusCodeSde();
        if (status == null) {
            return false;
        }

        // Claim is open if status < 20 and not 5
        if (status < 20 && status != 5) {
            return true;
        }

        // Check if any failures have status 0
        List<ClaimFailure> failures = claimFailureRepository.findByCompanyCodeAndClaimNumber(
            claim.getCompanyCode(), 
            claim.getClaimNumber()
        );

        return failures.stream()
            .anyMatch(failure -> failure.getStatusCode() != null && failure.getStatusCode() == 0);
    }

    private boolean matchesSearchText(Claim claim, String searchText) {
        if (searchText.contains("rot") || searchText.contains("gelb")) {
            return true; // Color filtering handled separately
        }

        return claim.getCompanyCode().toLowerCase().contains(searchText) ||
               claim.getJobNumber().toLowerCase().contains(searchText) ||
               claim.getInvoiceDate().toLowerCase().contains(searchText) ||
               claim.getClaimNumberSde().toLowerCase().contains(searchText) ||
               claim.getChassisNumber().toLowerCase().contains(searchText) ||
               claim.getInvoiceNumber().toLowerCase().contains(searchText) ||
               claim.getLicensePlate().toLowerCase().contains(searchText) ||
               claim.getCustomerNumber().toLowerCase().contains(searchText) ||
               claim.getCustomerName().toLowerCase().contains(searchText);
    }

    private ClaimListItemDto toListItemDto(Claim claim) {
        String statusDescription = getStatusDescription(claim);
        String colorIndicator = determineColorIndicator(claim);
        
        return new ClaimListItemDto(
            claim.getCompanyCode(),
            claim.getInvoiceNumber(),
            formatDate(claim.getInvoiceDate()),
            claim.getJobNumber(),
            claim.getWorkshopType(),
            claim.getClaimNumber(),
            claim.getChassisNumber(),
            claim.getLicensePlate(),
            formatDate(String.valueOf(claim.getRepairDate())),
            claim.getMileage(),
            claim.getCustomerNumber(),
            claim.getCustomerName(),
            claim.getClaimNumberSde(),
            claim.getStatusCodeSde(),
            statusDescription,
            claim.getNumberOfFailures(),
            colorIndicator
        );
    }

    private String getStatusDescription(Claim claim) {
        Integer statusCode = claim.getStatusCodeSde();
        if (statusCode == null) {
            return "";
        }

        if ("00000000".equals(claim.getClaimNumberSde())) {
            if (statusCode == 5) {
                return "Minimum Request";
            } else if (statusCode == 20) {
                return "Minimum Posted";
            } else {
                return "Minimum Request";
            }
        }

        ClaimStatus status = ClaimStatus.fromCode(statusCode);
        return status != null ? status.getDescription() : "";
    }

    private String determineColorIndicator(Claim claim) {
        List<ClaimFailure> failures = claimFailureRepository.findByCompanyCodeAndClaimNumber(
            claim.getCompanyCode(), 
            claim.getClaimNumber()
        );

        boolean hasError = false;
        boolean hasWarning = false;
        boolean hasInfo = false;

        for (ClaimFailure failure : failures) {
            Integer statusCode = failure.getStatusCode();
            if (statusCode == null) {
                continue;
            }

            // Red: Error conditions
            if (statusCode == 16 || statusCode == 30 || 
                (statusCode == 0 && claim.getClaimNumberSde() != null && !claim.getClaimNumberSde().isBlank())) {
                hasError = true;
            }

            // Yellow: Warning conditions
            if (statusCode == 11) {
                hasWarning = true;
            }

            // Blue: Info conditions
            if (statusCode == 3 || statusCode == 11) {
                hasInfo = true;
            }
        }

        if (hasError) {
            return "RED";
        } else if (hasWarning) {
            return "YELLOW";
        } else if (hasInfo) {
            return "BLUE";
        }
        return "";
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.length() != 8) {
            return "";
        }
        try {
            return dateString.substring(6, 8) + "." + 
                   dateString.substring(4, 6) + "." + 
                   dateString.substring(0, 4);
        } catch (Exception e) {
            return dateString;
        }
    }
}