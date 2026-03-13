/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

public record V4ValidationCriteria(
    String g71000,
    String g71010,
    String g71020,
    String g71030,
    String g71040,
    String g71190,
    String g71200
) {
    public String extractV4Code() {
        if (g71200 != null && g71200.length() >= 10) { // @rpg-trace: n1988
            return g71200.substring(7, 9); // @rpg-trace: n1988
        }
        return null; // @rpg-trace: n1988
    }

    public boolean isV4Code() {
        String v4Code = extractV4Code(); // @rpg-trace: n1987
        return "V4".equals(v4Code); // @rpg-trace: n1987
    }
}