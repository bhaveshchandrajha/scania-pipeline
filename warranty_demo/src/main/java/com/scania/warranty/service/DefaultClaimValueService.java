/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DefaultClaimValueService implements ClaimValueService {

    @Override
    public BigDecimal getClaimValues(String mode, String g73000, String g73050, String g73060, String g73065) {
        // This delegates to the existing getClaimValues RPG procedure logic.
        // Implementation should query claim line items and sum requested values.
        // Placeholder: in a full migration, this would aggregate GPS200 or similar fields
        // from HSGPSPF or related tables for the given claim key.
        // For now, this must be implemented by the team migrating the getClaimValues procedure.
        throw new UnsupportedOperationException(
                "getClaimValues must be implemented from the corresponding RPG procedure migration");
    }
}