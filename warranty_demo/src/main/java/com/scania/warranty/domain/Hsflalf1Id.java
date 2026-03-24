/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Hsflalf1Id implements Serializable {
    private String fla000;
    private String fla010;
    private String fla020;
    private BigDecimal fla030;
    private String fla040;
    private BigDecimal fla050;
    private String fla160;
    private String fla170;
    private String fla180;
    private String fla190;
    private String fla200;

    public Hsflalf1Id() {}

    public String getFla000() { return fla000; }
    public void setFla000(String fla000) { this.fla000 = fla000; }
    public String getFla010() { return fla010; }
    public void setFla010(String fla010) { this.fla010 = fla010; }
    public String getFla020() { return fla020; }
    public void setFla020(String fla020) { this.fla020 = fla020; }
    public BigDecimal getFla030() { return fla030; }
    public void setFla030(BigDecimal fla030) { this.fla030 = fla030; }
    public String getFla040() { return fla040; }
    public void setFla040(String fla040) { this.fla040 = fla040; }
    public BigDecimal getFla050() { return fla050; }
    public void setFla050(BigDecimal fla050) { this.fla050 = fla050; }
    public String getFla160() { return fla160; }
    public void setFla160(String fla160) { this.fla160 = fla160; }
    public String getFla170() { return fla170; }
    public void setFla170(String fla170) { this.fla170 = fla170; }
    public String getFla180() { return fla180; }
    public void setFla180(String fla180) { this.fla180 = fla180; }
    public String getFla190() { return fla190; }
    public void setFla190(String fla190) { this.fla190 = fla190; }
    public String getFla200() { return fla200; }
    public void setFla200(String fla200) { this.fla200 = fla200; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hsflalf1Id that = (Hsflalf1Id) o;
        return Objects.equals(fla000, that.fla000) &&
               Objects.equals(fla010, that.fla010) &&
               Objects.equals(fla020, that.fla020) &&
               Objects.equals(fla030, that.fla030) &&
               Objects.equals(fla040, that.fla040) &&
               Objects.equals(fla050, that.fla050) &&
               Objects.equals(fla160, that.fla160) &&
               Objects.equals(fla170, that.fla170) &&
               Objects.equals(fla180, that.fla180) &&
               Objects.equals(fla190, that.fla190) &&
               Objects.equals(fla200, that.fla200);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fla000, fla010, fla020, fla030, fla040, fla050, fla160, fla170, fla180, fla190, fla200);
    }
}