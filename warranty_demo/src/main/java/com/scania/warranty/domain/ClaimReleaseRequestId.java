/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimReleaseRequestId implements Serializable {
    private String g70Kzl;
    private String g70Rnr;
    private String g70Rdat;

    public ClaimReleaseRequestId() {}

    public ClaimReleaseRequestId(String g70Kzl, String g70Rnr, String g70Rdat) {
        this.g70Kzl = g70Kzl;
        this.g70Rnr = g70Rnr;
        this.g70Rdat = g70Rdat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimReleaseRequestId that = (ClaimReleaseRequestId) o;
        return Objects.equals(g70Kzl, that.g70Kzl) && Objects.equals(g70Rnr, that.g70Rnr) &&
               Objects.equals(g70Rdat, that.g70Rdat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g70Kzl, g70Rnr, g70Rdat);
    }
}