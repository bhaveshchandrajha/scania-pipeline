/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import org.springframework.stereotype.Service;

@Service
public class ClaimValuesService {

    /**
     * Retrieves the total requested claim value for a given claim.
     * Corresponds to RPG: getClaimValues('Requested':G73000:G73050:G73060:G73065)
     *
     * @param mode          the mode (e.g., "Requested")
     * @param companyCode   G73000
     * @param claimNumber   G73050
     * @param claimSequence G73060
     * @param subSequence   G73065
     * @return the computed claim value
     */
    public java.math.BigDecimal getClaimValues(String mode, String companyCode, String claimNumber,
                                                String claimSequence, String subSequence) {
        // This method delegates to the existing claim values calculation logic.
        // In the RPG program, getClaimValues sums up parts/labor/special costs for the claim.
        // This is a placeholder that must be wired to the actual implementation.
        // For now, returns BigDecimal.ZERO to indicate no value found.
        // TODO: Wire to actual claim value calculation from migrated getClaimValues procedure
        return java.math.BigDecimal.ZERO; // @rpg-trace: n1972
    }
}