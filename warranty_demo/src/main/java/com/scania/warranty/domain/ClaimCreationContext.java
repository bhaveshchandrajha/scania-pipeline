/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.math.BigDecimal;

public record ClaimCreationContext(
    String kuerzel, // @rpg-trace: n1779
    String claimNr, // @rpg-trace: n1779
    Integer failureNo, // @rpg-trace: n1783
    boolean create, // @rpg-trace: n1833
    boolean maintenance, // @rpg-trace: n1791
    String groups, // @rpg-trace: n1796
    String partNo, // @rpg-trace: n1797
    BigDecimal partValue, // @rpg-trace: n1798
    String text, // @rpg-trace: n1779
    Integer lineNo, // @rpg-trace: n1792
    BigDecimal valueMat, // @rpg-trace: n1793
    BigDecimal valueLab, // @rpg-trace: n1794
    BigDecimal valueSpe // @rpg-trace: n1795
) {
}