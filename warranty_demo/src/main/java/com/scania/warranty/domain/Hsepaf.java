/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSEPAF")
@IdClass(HsepafKey.class)
public class Hsepaf {

    @Id
    @Column(name = "EPA_KEY1")
    private String epaKey1;

    @Id
    @Column(name = "EPA_KEY2")
    private String epaKey2;

    @Id
    @Column(name = "EPA_KEY3")
    private String epaKey3;

    @Id
    @Column(name = "EPA_KEY4")
    private String epaKey4;

    @Id
    @Column(name = "EPA_KEY5")
    private String epaKey5;

    @Column(name = "EPA_DATV")
    private String epaDatv;

    public Hsepaf() {
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

    public String getEpaDatv() {
        return epaDatv;
    }

    public void setEpaDatv(String epaDatv) {
        this.epaDatv = epaDatv;
    }
}