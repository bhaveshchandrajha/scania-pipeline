/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

// Value object encapsulating the parameters needed for the CheckV4 procedure.
// G71000..G71200 are fields from the G71 (Claim) record; G71200 contains a
// 2-character code at position 8 that determines the agreement type.
public record CheckV4Request(
    String g71000,  // key field 1 (maps to AHK000 in HSAHKPF)
    String g71010,  // key field 2
    String g71020,  // key field 3
    String g71030,  // key field 4 (maps to AHK030)
    String g71040,  // key field 5 (maps to AHK040)
    String g71190,  // key field 6 (maps to AHK050)
    String g71200   // contains agreement type code at position 8-9
) {
    /**
     * Extracts the 2-character agreement type code from g71200 at position 8 (0-based: 7).
     * RPG: %Subst(G71200:8:2) — RPG uses 1-based positions.
     */
    public String agreementTypeCode() {
        if (g71200 == null || g71200.length() < 9) {
            return "";
        }
        return g71200.substring(7, 9); // @rpg-trace: n1988
    }
}