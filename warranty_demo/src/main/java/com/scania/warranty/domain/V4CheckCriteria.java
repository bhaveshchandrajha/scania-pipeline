/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

/**
 * Value object encapsulating the criteria needed for the V4 check.
 * Maps to the RPG key fields used in CHAIN and SETLL operations.
 */
public record V4CheckCriteria(
    String g71000,  // Key field 1 (e.g., company code)
    String g71010,  // Key field 2
    String g71020,  // Key field 3
    String g71030,  // Key field 4
    String g71040,  // Key field 5
    String g71190,  // Key field 6
    String g71200   // Full field from which substring(8,2) extracts the variant code
) {
    /**
     * Extracts the 2-character variant code from position 8 of g71200.
     * RPG: %Subst(G71200:8:2)
     */
    public String variantCode() {
        if (g71200 == null || g71200.length() < 10) { // @rpg-trace: n1988
            return "";
        }
        return g71200.substring(7, 9); // @rpg-trace: n1988
    }
}