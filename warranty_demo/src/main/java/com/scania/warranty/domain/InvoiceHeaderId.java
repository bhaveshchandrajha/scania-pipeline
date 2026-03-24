/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class InvoiceHeaderId implements Serializable {

    private String ahk000;
    private String ahk010;
    private String ahk020;
    private String ahk030;
    private String ahk040;
    private String ahk050;
    private String ahk060;

    public InvoiceHeaderId() {}

    public InvoiceHeaderId(String ahk000, String ahk010, String ahk020,
                           String ahk030, String ahk040, String ahk050, String ahk060) {
        this.ahk000 = ahk000;
        this.ahk010 = ahk010;
        this.ahk020 = ahk020;
        this.ahk030 = ahk030;
        this.ahk040 = ahk040;
        this.ahk050 = ahk050;
        this.ahk060 = ahk060;
    }

    public String getAhk000() { return ahk000; }
    public void setAhk000(String ahk000) { this.ahk000 = ahk000; }

    public String getAhk010() { return ahk010; }
    public void setAhk010(String ahk010) { this.ahk010 = ahk010; }

    public String getAhk020() { return ahk020; }
    public void setAhk020(String ahk020) { this.ahk020 = ahk020; }

    public String getAhk030() { return ahk030; }
    public void setAhk030(String ahk030) { this.ahk030 = ahk030; }

    public String getAhk040() { return ahk040; }
    public void setAhk040(String ahk040) { this.ahk040 = ahk040; }

    public String getAhk050() { return ahk050; }
    public void setAhk050(String ahk050) { this.ahk050 = ahk050; }

    public String getAhk060() { return ahk060; }
    public void setAhk060(String ahk060) { this.ahk060 = ahk060; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceHeaderId that = (InvoiceHeaderId) o;
        return Objects.equals(ahk000, that.ahk000) &&
               Objects.equals(ahk010, that.ahk010) &&
               Objects.equals(ahk020, that.ahk020) &&
               Objects.equals(ahk030, that.ahk030) &&
               Objects.equals(ahk040, that.ahk040) &&
               Objects.equals(ahk050, that.ahk050) &&
               Objects.equals(ahk060, that.ahk060);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ahk000, ahk010, ahk020, ahk030, ahk040, ahk050, ahk060);
    }
}