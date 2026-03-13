/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class HsepafKey implements Serializable {
    private String epaKey1;
    private String epaKey2;
    private String epaKey3;
    private String epaKey4;
    private String epaKey5;

    public HsepafKey() {
    }

    public HsepafKey(String epaKey1, String epaKey2, String epaKey3, String epaKey4, String epaKey5) {
        this.epaKey1 = epaKey1;
        this.epaKey2 = epaKey2;
        this.epaKey3 = epaKey3;
        this.epaKey4 = epaKey4;
        this.epaKey5 = epaKey5;
    }

    public String getEpaKey1() {
        return epaKey1;
    }

    public void setEpaKey1(String epaKey1) {
        this.epaKey1 = epaKey1;
    }

    public String getEpaKey2() {
        return epaKey2;
    }

    public void setEpaKey2(String epaKey2) {
        this.epaKey2 = epaKey2;
    }

    public String getEpaKey3() {
        return epaKey3;
    }

    public void setEpaKey3(String epaKey3) {
        this.epaKey3 = epaKey3;
    }

    public String getEpaKey4() {
        return epaKey4;
    }

    public void setEpaKey4(String epaKey4) {
        this.epaKey4 = epaKey4;
    }

    public String getEpaKey5() {
        return epaKey5;
    }

    public void setEpaKey5(String epaKey5) {
        this.epaKey5 = epaKey5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HsepafKey hsepafKey = (HsepafKey) o;
        return Objects.equals(epaKey1, hsepafKey.epaKey1) &&
               Objects.equals(epaKey2, hsepafKey.epaKey2) &&
               Objects.equals(epaKey3, hsepafKey.epaKey3) &&
               Objects.equals(epaKey4, hsepafKey.epaKey4) &&
               Objects.equals(epaKey5, hsepafKey.epaKey5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epaKey1, epaKey2, epaKey3, epaKey4, epaKey5);
    }
}