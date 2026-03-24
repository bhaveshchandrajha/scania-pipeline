/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSFLALF1")
@IdClass(ExternalServiceId.class)
public class ExternalService {

    @Id
    @Column(name = "FLA000", length = 3, nullable = false)
    private String fla000; // @rpg-trace: schema

    @Id
    @Column(name = "FLA010", length = 8, nullable = false)
    private String fla010; // @rpg-trace: schema

    @Id
    @Column(name = "FLA020", length = 5, nullable = false)
    private String fla020; // @rpg-trace: schema

    @Id
    @Column(name = "FLA030", precision = 3, scale = 0, nullable = false)
    private BigDecimal fla030; // @rpg-trace: schema

    @Id
    @Column(name = "FLA040", length = 8, nullable = false)
    private String fla040; // @rpg-trace: schema

    @Id
    @Column(name = "FLA050", precision = 3, scale = 0, nullable = false)
    private BigDecimal fla050; // @rpg-trace: schema

    @Column(name = "FLA060", length = 40, nullable = false)
    private String fla060; // @rpg-trace: schema

    @Column(name = "FLA065", precision = 3, scale = 0, nullable = false)
    private BigDecimal fla065; // @rpg-trace: schema

    @Column(name = "FLA070", precision = 7, scale = 2, nullable = false)
    private BigDecimal fla070; // @rpg-trace: schema

    @Column(name = "FLA080", precision = 5, scale = 0, nullable = false)
    private BigDecimal fla080; // @rpg-trace: schema

    @Column(name = "FLA090", length = 10, nullable = false)
    private String fla090; // @rpg-trace: schema

    @Column(name = "FLA100", length = 8, nullable = false)
    private String fla100; // @rpg-trace: schema

    @Column(name = "FLA110", length = 60, nullable = false)
    private String fla110; // @rpg-trace: schema

    @Column(name = "FLA120", length = 60, nullable = false)
    private String fla120; // @rpg-trace: schema

    @Column(name = "FLA130", precision = 9, scale = 2, nullable = false)
    private BigDecimal fla130; // @rpg-trace: schema

    @Column(name = "FLA140", length = 3, nullable = false)
    private String fla140; // @rpg-trace: schema

    @Column(name = "FLA150", precision = 9, scale = 2, nullable = false)
    private BigDecimal fla150; // @rpg-trace: schema

    @Id
    @Column(name = "FLA160", length = 5, nullable = false)
    private String fla160; // @rpg-trace: schema

    @Id
    @Column(name = "FLA170", length = 1, nullable = false)
    private String fla170; // @rpg-trace: schema

    @Id
    @Column(name = "FLA180", length = 1, nullable = false)
    private String fla180; // @rpg-trace: schema

    @Id
    @Column(name = "FLA190", length = 2, nullable = false)
    private String fla190; // @rpg-trace: schema

    @Id
    @Column(name = "FLA200", length = 8, nullable = false)
    private String fla200; // @rpg-trace: schema

    @Column(name = "FLA205", precision = 3, scale = 0, nullable = false)
    private BigDecimal fla205; // @rpg-trace: schema

    @Column(name = "FLA207", length = 2, nullable = false)
    private String fla207; // @rpg-trace: schema

    @Column(name = "FLA210", length = 5, nullable = false)
    private String fla210; // @rpg-trace: schema

    @Column(name = "FLA220", length = 8, nullable = false)
    private String fla220; // @rpg-trace: schema

    @Column(name = "FLA230", length = 1, nullable = false)
    private String fla230; // @rpg-trace: schema

    @Column(name = "FLA1220", length = 40, nullable = false)
    private String fla1220; // @rpg-trace: schema

    @Column(name = "FLA1240", length = 40, nullable = false)
    private String fla1240; // @rpg-trace: schema

    public ExternalService() {}

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
    public String getFla060() { return fla060; }
    public void setFla060(String fla060) { this.fla060 = fla060; }
    public BigDecimal getFla065() { return fla065; }
    public void setFla065(BigDecimal fla065) { this.fla065 = fla065; }
    public BigDecimal getFla070() { return fla070; }
    public void setFla070(BigDecimal fla070) { this.fla070 = fla070; }
    public BigDecimal getFla080() { return fla080; }
    public void setFla080(BigDecimal fla080) { this.fla080 = fla080; }
    public String getFla090() { return fla090; }
    public void setFla090(String fla090) { this.fla090 = fla090; }
    public String getFla100() { return fla100; }
    public void setFla100(String fla100) { this.fla100 = fla100; }
    public String getFla110() { return fla110; }
    public void setFla110(String fla110) { this.fla110 = fla110; }
    public String getFla120() { return fla120; }
    public void setFla120(String fla120) { this.fla120 = fla120; }
    public BigDecimal getFla130() { return fla130; }
    public void setFla130(BigDecimal fla130) { this.fla130 = fla130; }
    public String getFla140() { return fla140; }
    public void setFla140(String fla140) { this.fla140 = fla140; }
    public BigDecimal getFla150() { return fla150; }
    public void setFla150(BigDecimal fla150) { this.fla150 = fla150; }
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
    public BigDecimal getFla205() { return fla205; }
    public void setFla205(BigDecimal fla205) { this.fla205 = fla205; }
    public String getFla207() { return fla207; }
    public void setFla207(String fla207) { this.fla207 = fla207; }
    public String getFla210() { return fla210; }
    public void setFla210(String fla210) { this.fla210 = fla210; }
    public String getFla220() { return fla220; }
    public void setFla220(String fla220) { this.fla220 = fla220; }
    public String getFla230() { return fla230; }
    public void setFla230(String fla230) { this.fla230 = fla230; }
    public String getFla1220() { return fla1220; }
    public void setFla1220(String fla1220) { this.fla1220 = fla1220; }
    public String getFla1240() { return fla1240; }
    public void setFla1240(String fla1240) { this.fla1240 = fla1240; }
}