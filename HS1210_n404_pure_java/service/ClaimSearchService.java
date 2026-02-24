package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimSearchService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ClaimSearchService(ClaimRepository claimRepository, 
                             ClaimErrorRepository claimErrorRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
    }

    public List<Claim> searchClaims(ClaimSearchCriteria criteria) {
        Specification<Claim> spec = buildSpecification(criteria);
        
        List<Claim> claims = claimRepository.findAll(spec);
        
        if (criteria.claimAgeDays() != null && criteria.claimAgeDays() > 0) {
            claims = filterByClaimAge(claims, criteria.claimAgeDays());
        }
        
        if (criteria.openClaimsOnly()) {
            claims = filterOpenClaims(claims);
        }
        
        if (criteria.searchText() != null && !criteria.searchText().isBlank()) {
            claims = filterBySearchText(claims, criteria.searchText());
        }
        
        return sortClaims(claims, criteria);
    }

    private Specification<Claim> buildSpecification(ClaimSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("companyCode"), criteria.companyCode()));
            
            if (criteria.statusFilter() != null && !criteria.statusFilter().isBlank()) {
                Integer statusCode = parseStatusCode(criteria.statusFilter());
                if (statusCode != null) {
                    String operator = criteria.statusOperator();
                    if ("=".equals(operator) || "*".equals(operator)) {
                        predicates.add(criteriaBuilder.equal(root.get("statusCodeSde"), statusCode));
                    } else if (">".equals(operator)) {
                        predicates.add(criteriaBuilder.greaterThan(root.get("statusCodeSde"), statusCode));
                    } else if ("<".equals(operator)) {
                        predicates.add(criteriaBuilder.lessThan(root.get("statusCodeSde"), statusCode));
                    }
                }
            }
            
            if (criteria.vehicleNumberFilter() != null && !criteria.vehicleNumberFilter().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("chassisNumber"), criteria.vehicleNumberFilter()));
            }
            
            if (criteria.customerNumberFilter() != null && !criteria.customerNumberFilter().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("customerNumber"), criteria.customerNumberFilter()));
            }
            
            if (criteria.sdeClaimNumberFilter() != null && !criteria.sdeClaimNumberFilter().isBlank()) {
                if ("00000000".equals(criteria.sdeClaimNumberFilter())) {
                    predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("claimNumberSde"), ""),
                        criteriaBuilder.equal(root.get("claimNumberSde"), "00000000")
                    ));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("claimNumberSde"), criteria.sdeClaimNumberFilter()));
                }
            }
            
            if (criteria.minimumRequestsOnly()) {
                predicates.add(criteriaBuilder.equal(root.get("claimNumberSde"), "00000000"));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Integer parseStatusCode(String statusFilter) {
        try {
            return Integer.parseInt(statusFilter);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Claim> filterByClaimAge(List<Claim> claims, int maxAgeDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(maxAgeDays);
        
        return claims.stream()
            .filter(claim -> {
                if (claim.getRepairDate() == null || claim.getRepairDate() == 0) {
                    return true;
                }
                
                try {
                    String dateStr = String.valueOf(claim.getRepairDate());
                    LocalDate repairDate = LocalDate.parse(dateStr, ISO_DATE_FORMATTER);
                    return repairDate.isAfter(cutoffDate);
                } catch (Exception e) {
                    return true;
                }
            })
            .collect(Collectors.toList());
    }

    private List<Claim> filterOpenClaims(List<Claim> claims) {
        return claims.stream()
            .filter(claim -> {
                Integer statusCode = claim.getStatusCodeSde();
                if (statusCode == null) {
                    return true;
                }
                
                if (statusCode < 20 && statusCode != 5) {
                    return true;
                }
                
                List<ClaimError> errors = claimErrorRepository.findByCompanyCodeAndClaimNumber(
                    claim.getCompanyCode(), claim.getClaimNumber());
                
                if (errors.isEmpty()) {
                    return true;
                }
                
                return errors.stream().anyMatch(error -> error.getStatusCode() == null || error.getStatusCode() == 0);
            })
            .collect(Collectors.toList());
    }

    private List<Claim> filterBySearchText(List<Claim> claims, String searchText) {
        String searchLower = searchText.toLowerCase();
        
        return claims.stream()
            .filter(claim -> matchesSearchText(claim, searchLower))
            .collect(Collectors.toList());
    }

    private boolean matchesSearchText(Claim claim, String searchLower) {
        if (claim.getCompanyCode() != null && claim.getCompanyCode().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getOrderNumber() != null && claim.getOrderNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getInvoiceDate() != null && claim.getInvoiceDate().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getClaimNumberSde() != null && claim.getClaimNumberSde().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getClaimNumber() != null && claim.getClaimNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getInvoiceNumber() != null && claim.getInvoiceNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getChassisNumber() != null && claim.getChassisNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getCustomerNumber() != null && claim.getCustomerNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        if (claim.getCustomerName() != null && claim.getCustomerName().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        return false;
    }

    private List<Claim> sortClaims(List<Claim> claims, ClaimSearchCriteria criteria) {
        if ("claimNumber".equals(criteria.sortField())) {
            if (criteria.sortAscending()) {
                return claims.stream()
                    .sorted((c1, c2) -> compareStrings(c1.getClaimNumber(), c2.getClaimNumber()))
                    .collect(Collectors.toList());
            } else {
                return claims.stream()
                    .sorted((c1, c2) -> compareStrings(c2.getClaimNumber(), c1.getClaimNumber()))
                    .collect(Collectors.toList());
            }
        } else {
            if (criteria.sortAscending()) {
                return claims.stream()
                    .sorted((c1, c2) -> compareStrings(c1.getInvoiceNumber(), c2.getInvoiceNumber()))
                    .collect(Collectors.toList());
            } else {
                return claims.stream()
                    .sorted((c1, c2) -> compareStrings(c2.getInvoiceNumber(), c1.getInvoiceNumber()))
                    .collect(Collectors.toList());
            }
        }
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareTo(s2);
    }
}