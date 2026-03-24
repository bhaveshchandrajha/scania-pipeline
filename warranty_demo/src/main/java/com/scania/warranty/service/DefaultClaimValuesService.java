/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Default implementation of ClaimValuesService.
 * Corresponds to RPG procedure getClaimValues which calculates the total
 * requested/approved claim value for a given claim identified by its key fields.
 */
@Service
public class DefaultClaimValuesService implements ClaimValuesService {

    @Override
    public BigDecimal getClaimValues(String mode, String g73000, String g73050, String g73060, String g73065) {
        // This method delegates to the existing getClaimValues logic in the broader application.
        // The RPG procedure getClaimValues('Requested', G73000, G73050, G73060, G73065)
        // sums up claim line values for the given claim key.
        // Implementation should be provided by the existing claim values calculation module.
        throw new UnsupportedOperationException(
                "getClaimValues must be implemented with actual claim value calculation logic for mode: " + mode
        ); // @rpg-trace: n1972
    }
}