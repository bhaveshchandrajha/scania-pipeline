/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import java.math.BigDecimal;

public interface ClaimValueService {

    BigDecimal getClaimValues(String mode, String g73000, String g73050, String g73060, String g73065);
}