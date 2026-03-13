/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSAHKPF")
@IdClass(HsahkpfKey.class)
public class Hsahkpf {

    @Id
    @Column(name = "AHK000")
    private String ahk000;

    @Id
    @Column(name = "AHK010")
    private String ahk010;

    @Id
    @Column(name = "AHK020")
    private String ahk020;

    @Id
    @Column(name = "AHK030")
    private String ahk030;

    @Id
    @Column(name = "AHK040")
    private String ahk040;

    @Id
    @Column(name = "AHK050")
    private String ahk050;

    @Id
    @Column(name = "AHK060")
    private String ahk060;

    @Id
    @Column(name = "AHK070")
    private String ahk070;

    @Column(name = "AHK080")
    private String ahk080;

    public Hsahkpf() {
    }

    public String getAhk000() {
        return ahk000;
    }

    public void setAhk000(String ahk000) {
        this.ahk000 = ahk000;
    }

    public String getAhk010() {
        return ahk010;
    }

    public void setAhk010(String ahk010) {
        this.ahk010 = ahk010;
    }

    public String getAhk020() {
        return ahk020;
    }

    public void setAhk020(String ahk020) {
        this.ahk020 = ahk020;
    }

    public String getAhk030() {
        return ahk030;
    }

    public void setAhk030(String ahk030) {
        this.ahk030 = ahk030;
    }

    public String getAhk040() {
        return ahk040;
    }

    public void setAhk040(String ahk040) {
        this.ahk040 = ahk040;
    }

    public String getAhk050() {
        return ahk050;
    }

    public void setAhk050(String ahk050) {
        this.ahk050 = ahk050;
    }

    public String getAhk060() {
        return ahk060;
    }

    public void setAhk060(String ahk060) {
        this.ahk060 = ahk060;
    }

    public String getAhk070() {
        return ahk070;
    }

    public void setAhk070(String ahk070) {
        this.ahk070 = ahk070;
    }

    public String getAhk080() {
        return ahk080;
    }

    public void setAhk080(String ahk080) {
        this.ahk080 = ahk080;
    }
}