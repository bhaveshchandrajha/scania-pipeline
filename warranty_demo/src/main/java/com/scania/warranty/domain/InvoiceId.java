/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class InvoiceId implements Serializable {
    private String ahk000;
    private String ahk040;
    private String ahk050;
    private String ahk060;
    private String ahk070;
    private String ahk080;

    public InvoiceId() {}

    public InvoiceId(String ahk000, String ahk040, String ahk050, String ahk060, String ahk070, String ahk080) {
        this.ahk000 = ahk000;
        this.ahk040 = ahk040;
        this.ahk050 = ahk050;
        this.ahk060 = ahk060;
        this.ahk070 = ahk070;
        this.ahk080 = ahk080;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceId that = (InvoiceId) o;
        return Objects.equals(ahk000, that.ahk000) && Objects.equals(ahk040, that.ahk040) &&
               Objects.equals(ahk050, that.ahk050) && Objects.equals(ahk060, that.ahk060) &&
               Objects.equals(ahk070, that.ahk070) && Objects.equals(ahk080, that.ahk080);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ahk000, ahk040, ahk050, ahk060, ahk070, ahk080);
    }
}