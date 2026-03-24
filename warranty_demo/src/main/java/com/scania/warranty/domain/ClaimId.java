/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimId implements Serializable {
    private String g71000;
    private String g71050;
    private String g71060;

    public ClaimId() {}

    public ClaimId(String g71000, String g71050, String g71060) {
        this.g71000 = g71000;
        this.g71050 = g71050;
        this.g71060 = g71060;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimId that = (ClaimId) o;
        return Objects.equals(g71000, that.g71000) && Objects.equals(g71050, that.g71050) &&
               Objects.equals(g71060, that.g71060);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g71000, g71050, g71060);
    }
}