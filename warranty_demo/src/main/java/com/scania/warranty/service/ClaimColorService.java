/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.repository.ClaimErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for claim color determination (SR_FARBE).
 */
@Service
public class ClaimColorService {

    private final ClaimErrorRepository claimErrorRepository;

    @Autowired
    public ClaimColorService(ClaimErrorRepository claimErrorRepository) {
        this.claimErrorRepository = claimErrorRepository;
    }

    public String determineClaimColor(Claim claim) {
        // RPG: SR_FARBE - Determines display color based on claim/error status
        // Returns: "ROT" (red), "GELB" (yellow), "BLAU" (blue), or blank
        boolean isRed = false;
        boolean isYellow = false;
        boolean isBlue = false;

        // @origin HS1210 L830-833 (IF)
        if (!claim.getClaimNrSde().isBlank()) {
            // @origin HS1210 L941-941 (CHAIN)
            List<ClaimError> errors = claimErrorRepository.findByClaimNumber(claim.getPakz(), claim.getClaimNr());

            // @origin HS1210 L841-844 (IF)
            if (errors.isEmpty() && claim.getStatusCodeSde() == 20) {
                isRed = true;
            }

            // @origin HS1210 L884-1012 (DOW)
            for (ClaimError error : errors) {
                // @origin HS1210 L845-848 (IF)
                if (error.getStatusCode() == 16) {
                    isRed = true;
                }
                if (error.getStatusCode() == 30 || error.getStatusCode() == 0) {
                    isRed = true;
                }
                if (error.getStatusCode() == 11) {
                    isYellow = true;
                }
                if (error.getStatusCode() == 3 || error.getStatusCode() == 11) {
                    isBlue = true;
                }
            }
        } else {
            // No SDE claim number
        }

        if (isRed) {
            return "ROT";
        }
        if (isYellow) {
            return "GELB";
        }
        if (isBlue) {
            return "BLAU";
        }
        return "";
    }
}