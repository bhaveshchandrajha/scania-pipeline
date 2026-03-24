/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSAHWPF")
@IdClass(LaborId.class)
public class Labor {

    @Id
    @Column(name = "AHW000", length = 3, nullable = false)
    private String ahw000; // @rpg-trace: schema

    @Id
    @Column(name = "AHW010", length = 5, nullable = false)
    private String ahw010; // @rpg-trace: schema

    @Column(name = "AHW015", length = 10, nullable = false)
    private String ahw015; // @rpg-trace: schema

    @Id
    @Column(name = "AHW020", length = 8, nullable = false)
    private String ahw020; // @rpg-trace: schema

    @Id
    @Column(name = "AHW030", length = 1, nullable = false)
    private String ahw030; // @rpg-trace: schema

    @Id
    @Column(name = "AHW040", length = 5, nullable = false)
    private String ahw040; // @rpg-trace: schema

    @Id
    @Column(name = "AHW050", length = 1, nullable = false)
    private String ahw050; // @rpg-trace: schema

    @Id
    @Column(name = "AHW060", length = 1, nullable = false)
    private String ahw060; // @rpg-trace: schema

    @Id
    @Column(name = "AHW070", length = 2, nullable = false)
    private String ahw070; // @rpg-trace: schema

    @Column(name = "AHW075", precision = 3, scale = 0, nullable = false)
    private BigDecimal ahw075; // @rpg-trace: schema

    @Id
    @Column(name = "AHW080", length = 2, nullable = false)
    private String ahw080; // @rpg-trace: schema

    @Id
    @Column(name = "AHW085", precision = 3, scale = 0, nullable = false)
    private BigDecimal ahw085; // @rpg-trace: schema

    @Id
    @Column(name = "AHW090", length = 8, nullable = false)
    private String ahw090; // @rpg-trace: schema

    @Id
    @Column(name = "AHW093", precision = 3, scale = 0, nullable = false)
    private BigDecimal ahw093; // @rpg-trace: schema

    @Id
    @Column(name = "AHW095", precision = 3, scale = 0, nullable = false)
    private BigDecimal ahw095; // @rpg-trace: schema

    @Id
    @Column(name = "AHW100", length = 8, nullable = false)
    private String ahw100; // @rpg-trace: schema

    @Id
    @Column(name = "AHW110", length = 3, nullable = false)
    private String ahw110; // @rpg-trace: schema

    @Column(name = "AHW120", length = 40, nullable = false)
    private String ahw120; // @rpg-trace: schema

    @Column(name = "AHW129", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw129; // @rpg-trace: schema

    @Column(name = "AHW130", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw130; // @rpg-trace: schema

    @Column(name = "AHW131", precision = 5, scale = 0, nullable = false)
    private BigDecimal ahw131; // @rpg-trace: schema

    @Column(name = "AHW132", precision = 5, scale = 0, nullable = false)
    private BigDecimal ahw132; // @rpg-trace: schema

    @Column(name = "AHW133", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw133; // @rpg-trace: schema

    @Column(name = "AHW134", length = 1, nullable = false)
    private String ahw134; // @rpg-trace: schema

    @Column(name = "AHW140", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw140; // @rpg-trace: schema

    @Column(name = "AHW150", length = 3, nullable = false)
    private String ahw150; // @rpg-trace: schema

    @Column(name = "AHW159", length = 2, nullable = false)
    private String ahw159; // @rpg-trace: schema

    @Column(name = "AHW160", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw160; // @rpg-trace: schema

    @Column(name = "AHW170", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw170; // @rpg-trace: schema

    @Column(name = "AHW180", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw180; // @rpg-trace: schema

    @Column(name = "AHW190", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw190; // @rpg-trace: schema

    @Column(name = "AHW200", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw200; // @rpg-trace: schema

    @Column(name = "AHW210", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw210; // @rpg-trace: schema

    @Column(name = "AHW220", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw220; // @rpg-trace: schema

    @Column(name = "AHW230", length = 1, nullable = false)
    private String ahw230; // @rpg-trace: schema

    @Column(name = "AHW240", length = 1, nullable = false)
    private String ahw240; // @rpg-trace: schema

    @Column(name = "AHW250", length = 1, nullable = false)
    private String ahw250; // @rpg-trace: schema

    @Column(name = "AHW255", length = 3, nullable = false)
    private String ahw255; // @rpg-trace: schema

    @Column(name = "AHW260", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw260; // @rpg-trace: schema

    @Column(name = "AHW270", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw270; // @rpg-trace: schema

    @Column(name = "AHW280", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw280; // @rpg-trace: schema

    @Column(name = "AHW290", length = 1, nullable = false)
    private String ahw290; // @rpg-trace: schema

    @Column(name = "AHW300", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw300; // @rpg-trace: schema

    @Column(name = "AHW310", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw310; // @rpg-trace: schema

    @Column(name = "AHW320", precision = 9, scale = 2, nullable = false)
    private BigDecimal ahw320; // @rpg-trace: schema

    @Column(name = "AHW330", length = 20, nullable = false)
    private String ahw330; // @rpg-trace: schema

    @Column(name = "AHW340", precision = 5, scale = 2, nullable = false)
    private BigDecimal ahw340; // @rpg-trace: schema

    @Column(name = "AHW350", length = 500, nullable = false)
    private String ahw350; // @rpg-trace: schema

    @Column(name = "AHW360", length = 2000, nullable = false)
    private String ahw360; // @rpg-trace: schema

    @Column(name = "AHW370", length = 2000, nullable = false)
    private String ahw370; // @rpg-trace: schema

    public Labor() {}

    public String getAhw000() { return ahw000; }
    public void setAhw000(String ahw000) { this.ahw000 = ahw000; }
    public String getAhw010() { return ahw010; }
    public void setAhw010(String ahw010) { this.ahw010 = ahw010; }
    public String getAhw015() { return ahw015; }
    public void setAhw015(String ahw015) { this.ahw015 = ahw015; }
    public String getAhw020() { return ahw020; }
    public void setAhw020(String ahw020) { this.ahw020 = ahw020; }
    public String getAhw030() { return ahw030; }
    public void setAhw030(String ahw030) { this.ahw030 = ahw030; }
    public String getAhw040() { return ahw040; }
    public void setAhw040(String ahw040) { this.ahw040 = ahw040; }
    public String getAhw050() { return ahw050; }
    public void setAhw050(String ahw050) { this.ahw050 = ahw050; }
    public String getAhw060() { return ahw060; }
    public void setAhw060(String ahw060) { this.ahw060 = ahw060; }
    public String getAhw070() { return ahw070; }
    public void setAhw070(String ahw070) { this.ahw070 = ahw070; }
    public BigDecimal getAhw075() { return ahw075; }
    public void setAhw075(BigDecimal ahw075) { this.ahw075 = ahw075; }
    public String getAhw080() { return ahw080; }
    public void setAhw080(String ahw080) { this.ahw080 = ahw080; }
    public BigDecimal getAhw085() { return ahw085; }
    public void setAhw085(BigDecimal ahw085) { this.ahw085 = ahw085; }
    public String getAhw090() { return ahw090; }
    public void setAhw090(String ahw090) { this.ahw090 = ahw090; }
    public BigDecimal getAhw093() { return ahw093; }
    public void setAhw093(BigDecimal ahw093) { this.ahw093 = ahw093; }
    public BigDecimal getAhw095() { return ahw095; }
    public void setAhw095(BigDecimal ahw095) { this.ahw095 = ahw095; }
    public String getAhw100() { return ahw100; }
    public void setAhw100(String ahw100) { this.ahw100 = ahw100; }
    public String getAhw110() { return ahw110; }
    public void setAhw110(String ahw110) { this.ahw110 = ahw110; }
    public String getAhw120() { return ahw120; }
    public void setAhw120(String ahw120) { this.ahw120 = ahw120; }
    public BigDecimal getAhw129() { return ahw129; }
    public void setAhw129(BigDecimal ahw129) { this.ahw129 = ahw129; }
    public BigDecimal getAhw130() { return ahw130; }
    public void setAhw130(BigDecimal ahw130) { this.ahw130 = ahw130; }
    public BigDecimal getAhw131() { return ahw131; }
    public void setAhw131(BigDecimal ahw131) { this.ahw131 = ahw131; }
    public BigDecimal getAhw132() { return ahw132; }
    public void setAhw132(BigDecimal ahw132) { this.ahw132 = ahw132; }
    public BigDecimal getAhw133() { return ahw133; }
    public void setAhw133(BigDecimal ahw133) { this.ahw133 = ahw133; }
    public String getAhw134() { return ahw134; }
    public void setAhw134(String ahw134) { this.ahw134 = ahw134; }
    public BigDecimal getAhw140() { return ahw140; }
    public void setAhw140(BigDecimal ahw140) { this.ahw140 = ahw140; }
    public String getAhw150() { return ahw150; }
    public void setAhw150(String ahw150) { this.ahw150 = ahw150; }
    public String getAhw159() { return ahw159; }
    public void setAhw159(String ahw159) { this.ahw159 = ahw159; }
    public BigDecimal getAhw160() { return ahw160; }
    public void setAhw160(BigDecimal ahw160) { this.ahw160 = ahw160; }
    public BigDecimal getAhw170() { return ahw170; }
    public void setAhw170(BigDecimal ahw170) { this.ahw170 = ahw170; }
    public BigDecimal getAhw180() { return ahw180; }
    public void setAhw180(BigDecimal ahw180) { this.ahw180 = ahw180; }
    public BigDecimal getAhw190() { return ahw190; }
    public void setAhw190(BigDecimal ahw190) { this.ahw190 = ahw190; }
    public BigDecimal getAhw200() { return ahw200; }
    public void setAhw200(BigDecimal ahw200) { this.ahw200 = ahw200; }
    public BigDecimal getAhw210() { return ahw210; }
    public void setAhw210(BigDecimal ahw210) { this.ahw210 = ahw210; }
    public BigDecimal getAhw220() { return ahw220; }
    public void setAhw220(BigDecimal ahw220) { this.ahw220 = ahw220; }
    public String getAhw230() { return ahw230; }
    public void setAhw230(String ahw230) { this.ahw230 = ahw230; }
    public String getAhw240() { return ahw240; }
    public void setAhw240(String ahw240) { this.ahw240 = ahw240; }
    public String getAhw250() { return ahw250; }
    public void setAhw250(String ahw250) { this.ahw250 = ahw250; }
    public String getAhw255() { return ahw255; }
    public void setAhw255(String ahw255) { this.ahw255 = ahw255; }
    public BigDecimal getAhw260() { return ahw260; }
    public void setAhw260(BigDecimal ahw260) { this.ahw260 = ahw260; }
    public BigDecimal getAhw270() { return ahw270; }
    public void setAhw270(BigDecimal ahw270) { this.ahw270 = ahw270; }
    public BigDecimal getAhw280() { return ahw280; }
    public void setAhw280(BigDecimal ahw280) { this.ahw280 = ahw280; }
    public String getAhw290() { return ahw290; }
    public void setAhw290(String ahw290) { this.ahw290 = ahw290; }
    public BigDecimal getAhw300() { return ahw300; }
    public void setAhw300(BigDecimal ahw300) { this.ahw300 = ahw300; }
    public BigDecimal getAhw310() { return ahw310; }
    public void setAhw310(BigDecimal ahw310) { this.ahw310 = ahw310; }
    public BigDecimal getAhw320() { return ahw320; }
    public void setAhw320(BigDecimal ahw320) { this.ahw320 = ahw320; }
    public String getAhw330() { return ahw330; }
    public void setAhw330(String ahw330) { this.ahw330 = ahw330; }
    public BigDecimal getAhw340() { return ahw340; }
    public void setAhw340(BigDecimal ahw340) { this.ahw340 = ahw340; }
    public String getAhw350() { return ahw350; }
    public void setAhw350(String ahw350) { this.ahw350 = ahw350; }
    public String getAhw360() { return ahw360; }
    public void setAhw360(String ahw360) { this.ahw360 = ahw360; }
    public String getAhw370() { return ahw370; }
    public void setAhw370(String ahw370) { this.ahw370 = ahw370; }
}