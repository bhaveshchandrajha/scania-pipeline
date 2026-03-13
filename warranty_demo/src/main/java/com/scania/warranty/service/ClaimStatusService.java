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
 * Service for claim status operations (SR109, SR_MINIMUM).
 */
@Service
public class ClaimStatusService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;

    @Autowired
    public ClaimStatusService(ClaimRepository claimRepository, ClaimErrorRepository claimErrorRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
    }

    @Transactional
    public void updateClaimStatus(String pakz, String claimNr, int newStatus) {
        // @origin HS1210 L941-941 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L830-833 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            if (claim.getStatusCodeSde() == 2) {
                // @origin HS1210 L887-887 (EVAL)
                claim.setStatusCodeSde(newStatus);
                // @origin HS1210 L860-860 (WRITE)
                claimRepository.save(claim);
            }
        }
    }

    @Transactional
    public void deleteClaimAndErrors(String pakz, String claimNr) {
        // @origin HS1210 L1027-1027 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L845-848 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            // @origin HS1210 L1035-1035 (CHAIN)
            List<ClaimError> errors = claimErrorRepository.findByClaimNumber(pakz, claimNr);
            claimErrorRepository.deleteAll(errors);
            claimRepository.delete(claim);
        }
    }

    @Transactional
    public void postMinimumClaim(String pakz, String claimNr) {
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L864-883 (IF)
        if (claimOpt.isEmpty()) {
            // @origin HS1210 L895-895 (EXSR)
            throw new IllegalArgumentException("Claim not found: " + pakz + "/" + claimNr);
        }
        claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L886-892 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            // @origin HS1210 L890-890 (EVAL)
            claim.setStatusCodeSde(20);
            // @origin HS1210 L861-861 (WRITE)
            claimRepository.save(claim);
        }
    }
}