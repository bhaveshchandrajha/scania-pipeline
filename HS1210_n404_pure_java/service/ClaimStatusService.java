package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClaimStatusService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;

    public ClaimStatusService(ClaimRepository claimRepository, 
                             ClaimErrorRepository claimErrorRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
    }

    @Transactional
    public void updateClaimStatus(String companyCode, String claimNumber, int newStatusCode) {
        Optional<Claim> claimOpt = claimRepository.findByCompanyCodeAndClaimNumber(companyCode, claimNumber);
        
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        
        Claim claim = claimOpt.get();
        
        if (claim.getStatusCodeSde() != null && claim.getStatusCodeSde() == 2) {
            claim.setStatusCodeSde(newStatusCode);
            claimRepository.save(claim);
        }
    }

    @Transactional
    public void markClaimAsDeleted(String companyCode, String claimNumber) {
        Optional<Claim> claimOpt = claimRepository.findByCompanyCodeAndClaimNumber(companyCode, claimNumber);
        
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        
        Claim claim = claimOpt.get();
        claim.setStatusCodeSde(ClaimStatus.EXCLUDED.getCode());
        claimRepository.save(claim);
        
        List<ClaimError> errors = claimErrorRepository.findByCompanyCodeAndClaimNumber(companyCode, claimNumber);
        for (ClaimError error : errors) {
            claimErrorRepository.delete(error);
        }
    }

    public String getClaimStatusColor(Claim claim) {
        if (claim.getClaimNumberSde() == null || claim.getClaimNumberSde().isBlank() || 
            "00000000".equals(claim.getClaimNumberSde())) {
            return "";
        }
        
        List<ClaimError> errors = claimErrorRepository.findByCompanyCodeAndClaimNumber(
            claim.getCompanyCode(), claim.getClaimNumber());
        
        if (errors.isEmpty() && claim.getStatusCodeSde() != null && claim.getStatusCodeSde() == 20) {
            return "RED";
        }
        
        for (ClaimError error : errors) {
            if (error.getStatusCode() != null) {
                if (error.getStatusCode() == 16 || error.getStatusCode() == 30 || error.getStatusCode() == 0) {
                    return "RED";
                }
                
                if (error.getStatusCode() == 11) {
                    return "YELLOW";
                }
                
                if (error.getStatusCode() == 3 || error.getStatusCode() == 11) {
                    return "BLUE";
                }
            }
        }
        
        return "";
    }

    public boolean isClaimEditable(Claim claim) {
        if (claim.getStatusCodeSde() == null) {
            return true;
        }
        
        int statusCode = claim.getStatusCodeSde();
        
        if (statusCode < 3) {
            return true;
        }
        
        return false;
    }

    public boolean isClaimDeletable(Claim claim) {
        if (claim.getStatusCodeSde() == null) {
            return true;
        }
        
        int statusCode = claim.getStatusCodeSde();
        
        if (statusCode > 3 && statusCode != 99) {
            List<ClaimError> errors = claimErrorRepository.findByCompanyCodeAndClaimNumber(
                claim.getCompanyCode(), claim.getClaimNumber());
            
            if (!errors.isEmpty() || statusCode == 20) {
                return false;
            }
        }
        
        return true;
    }
}