/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ExternalServiceId implements Serializable {
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

    public ExternalServiceId() {}

    public ExternalServiceId(String fla000, String fla010, String fla020, BigDecimal fla030,
                             String fla040, BigDecimal fla050, String fla160, String fla170,
                             String fla180, String fla190, String fla200) {
        this.fla000 = fla000;
        this.fla010 = fla010;
        this.fla020 = fla020;
        this.fla030 = fla030;
        this.fla040 = fla040;
        this.fla050 = fla050;
        this.fla160 = fla160;
        this.fla170 = fla170;
        this.fla180 = fla180;
        this.fla190 = fla190;
        this.fla200 = fla200;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalServiceId that = (ExternalServiceId) o;
        return Objects.equals(fla000, that.fla000) && Objects.equals(fla010, that.fla010) &&
               Objects.equals(fla020, that.fla020) && Objects.equals(fla030, that.fla030) &&
               Objects.equals(fla040, that.fla040) && Objects.equals(fla050, that.fla050) &&
               Objects.equals(fla160, that.fla160) && Objects.equals(fla170, that.fla170) &&
               Objects.equals(fla180, that.fla180) && Objects.equals(fla190, that.fla190) &&
               Objects.equals(fla200, that.fla200);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fla000, fla010, fla020, fla030, fla040, fla050, fla160, fla170, fla180, fla190, fla200);
    }
}