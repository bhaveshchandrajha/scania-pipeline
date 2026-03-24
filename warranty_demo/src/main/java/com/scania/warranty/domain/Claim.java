/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG71LF2")
@IdClass(ClaimId.class)
public class Claim {

    @Id
    @Column(name = "G71000", length = 3, nullable = false)
    private String g71000; // @rpg-trace: schema

    @Column(name = "G71010", length = 5, nullable = false)
    private String g71010; // @rpg-trace: schema

    @Column(name = "G71020", length = 8, nullable = false)
    private String g71020; // @rpg-trace: schema

    @Column(name = "G71030", length = 5, nullable = false)
    private String g71030; // @rpg-trace: schema

    @Column(name = "G71040", length = 1, nullable = false)
    private String g71040; // @rpg-trace: schema

    @Id
    @Column(name = "G71050", length = 8, nullable = false)
    private String g71050; // @rpg-trace: schema

    @Id
    @Column(name = "G71060", length = 7, nullable = false)
    private String g71060; // @rpg-trace: schema

    @Column(name = "G71070", length = 10, nullable = false)
    private String g71070; // @rpg-trace: schema

    @Column(name = "G71080", precision = 8, scale = 0, nullable = false)
    private BigDecimal g71080; // @rpg-trace: schema

    @Column(name = "G71090", precision = 8, scale = 0, nullable = false)
    private BigDecimal g71090; // @rpg-trace: schema

    @Column(name = "G71100", precision = 3, scale = 0, nullable = false)
    private BigDecimal g71100; // @rpg-trace: schema

    @Column(name = "G71110", precision = 1, scale = 0, nullable = false)
    private BigDecimal g71110; // @rpg-trace: schema

    @Column(name = "G71120", length = 1, nullable = false)
    private String g71120; // @rpg-trace: schema

    @Column(name = "G71130", length = 1, nullable = false)
    private String g71130; // @rpg-trace: schema

    @Column(name = "G71140", length = 6, nullable = false)
    private String g71140; // @rpg-trace: schema

    @Column(name = "G71150", length = 30, nullable = false)
    private String g71150; // @rpg-trace: schema

    @Column(name = "G71160", length = 8, nullable = false)
    private String g71160; // @rpg-trace: schema

    @Column(name = "G71170", precision = 2, scale = 0, nullable = false)
    private int g71170; // @rpg-trace: schema

    @Column(name = "G71180", precision = 2, scale = 0, nullable = false)
    private int g71180; // @rpg-trace: schema

    @Column(name = "G71190", length = 1, nullable = false)
    private String g71190; // @rpg-trace: schema

    @Column(name = "G71200", length = 10, nullable = false)
    private String g71200; // @rpg-trace: schema

    public Claim() {}

    public String getG71000() { return g71000; }
    public void setG71000(String g71000) { this.g71000 = g71000; }
    public String getG71010() { return g71010; }
    public void setG71010(String g71010) { this.g71010 = g71010; }
    public String getG71020() { return g71020; }
    public void setG71020(String g71020) { this.g71020 = g71020; }
    public String getG71030() { return g71030; }
    public void setG71030(String g71030) { this.g71030 = g71030; }
    public String getG71040() { return g71040; }
    public void setG71040(String g71040) { this.g71040 = g71040; }
    public String getG71050() { return g71050; }
    public void setG71050(String g71050) { this.g71050 = g71050; }
    public String getG71060() { return g71060; }
    public void setG71060(String g71060) { this.g71060 = g71060; }
    public String getG71070() { return g71070; }
    public void setG71070(String g71070) { this.g71070 = g71070; }
    public BigDecimal getG71080() { return g71080; }
    public void setG71080(BigDecimal g71080) { this.g71080 = g71080; }
    public BigDecimal getG71090() { return g71090; }
    public void setG71090(BigDecimal g71090) { this.g71090 = g71090; }
    public BigDecimal getG71100() { return g71100; }
    public void setG71100(BigDecimal g71100) { this.g71100 = g71100; }
    public BigDecimal getG71110() { return g71110; }
    public void setG71110(BigDecimal g71110) { this.g71110 = g71110; }
    public String getG71120() { return g71120; }
    public void setG71120(String g71120) { this.g71120 = g71120; }
    public String getG71130() { return g71130; }
    public void setG71130(String g71130) { this.g71130 = g71130; }
    public String getG71140() { return g71140; }
    public void setG71140(String g71140) { this.g71140 = g71140; }
    public String getG71150() { return g71150; }
    public void setG71150(String g71150) { this.g71150 = g71150; }
    public String getG71160() { return g71160; }
    public void setG71160(String g71160) { this.g71160 = g71160; }
    public int getG71170() { return g71170; }
    public void setG71170(int g71170) { this.g71170 = g71170; }
    public int getG71180() { return g71180; }
    public void setG71180(int g71180) { this.g71180 = g71180; }
    public String getG71190() { return g71190; }
    public void setG71190(String g71190) { this.g71190 = g71190; }
    public String getG71200() { return g71200; }
    public void setG71200(String g71200) { this.g71200 = g71200; }
}