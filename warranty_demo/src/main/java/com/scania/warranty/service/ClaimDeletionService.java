/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.repository.ClaimErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for deleting claims (marking as excluded).
 */
@Service
@Transactional
public class ClaimDeletionService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;

    @Autowired
    public ClaimDeletionService(ClaimRepository claimRepository, ClaimErrorRepository claimErrorRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
    }

    public void deleteClaim(String companyCode, String claimNumber) {
        // @origin HS1210 L941-941 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(companyCode, claimNumber);
        // @origin HS1210 L830-833 (IF)
        if (!claimOpt.isPresent()) {
            // @origin HS1210 L895-895 (EXSR)
            throw new IllegalArgumentException("Claim not found");
        }

        Claim claim = claimOpt.get();
        // @origin HS1210 L887-887 (EVAL)
        claim.setStatusCodeSde(99);
        // @origin HS1210 L860-860 (WRITE)
        claimRepository.save(claim);

        // @origin HS1210 L1027-1027 (CHAIN)
        List<ClaimError> errors = claimErrorRepository.findByClaimNumber(companyCode, claimNumber);
        // @origin HS1210 L884-1012 (DOW)
        for (ClaimError error : errors) {
            claimErrorRepository.delete(error);
        }
    }
}