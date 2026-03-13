/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.dto;

import java.math.BigDecimal;

public record ClaimDto(
    String pakz, // @rpg-trace: schema
    String rechNr, // @rpg-trace: schema
    String rechDatum, // @rpg-trace: schema
    String auftragsNr, // @rpg-trace: schema
    String bereich, // @rpg-trace: schema
    String claimNr, // @rpg-trace: schema
    String chassisNr, // @rpg-trace: schema
    Integer statusCodeSde, // @rpg-trace: schema
    Integer anzFehler // @rpg-trace: schema
) {
}