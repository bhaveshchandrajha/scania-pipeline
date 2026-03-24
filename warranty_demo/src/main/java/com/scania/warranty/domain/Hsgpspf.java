/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "HSGPSPF")
@IdClass(HsgpspfId.class)
public class Hsgpspf {

    @Id
    @Column(name = "GPS000", length = 3, nullable = false) // @rpg-trace: schema
    private String gps000;

    @Id
    @Column(name = "GPS010", length = 8, nullable = false) // @rpg-trace: schema
    private String gps010;

    @Id
    @Column(name = "GPS020", length = 2, nullable = false) // @rpg-trace: schema
    private String gps020;

    @Column(name = "GPS025", length = 2, nullable = false) // @rpg-trace: schema
    private String gps025;

    @Id
    @Column(name = "GPS030", precision = 3, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps030;

    @Id
    @Column(name = "GPS035", precision = 3, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps035;

    @Id
    @Column(name = "GPS040", length = 3, nullable = false) // @rpg-trace: schema
    private String gps040;

    @Column(name = "GPS050", length = 18, nullable = false) // @rpg-trace: schema
    private String gps050;

    @Column(name = "GPS060", precision = 5, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps060;

    @Column(name = "GPS070", precision = 11, scale = 2, nullable = false) // @rpg-trace: schema
    private BigDecimal gps070;

    @Column(name = "GPS080", length = 40, nullable = false) // @rpg-trace: schema
    private String gps080;

    @Column(name = "GPS090", length = 2, nullable = false) // @rpg-trace: schema
    private String gps090;

    @Column(name = "GPS100", precision = 3, scale = 1, nullable = false) // @rpg-trace: schema
    private BigDecimal gps100;

    @Column(name = "GPS110", precision = 4, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps110;

    @Column(name = "GPS120", precision = 3, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps120;

    @Column(name = "GPS130", length = 1, nullable = false) // @rpg-trace: schema
    private String gps130;

    @Column(name = "GPS140", precision = 8, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps140;

    @Column(name = "GPS150", precision = 3, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps150;

    @Column(name = "GPS160", length = 2, nullable = false) // @rpg-trace: schema
    private String gps160;

    @Column(name = "GPS161", length = 2, nullable = false) // @rpg-trace: schema
    private String gps161;

    @Column(name = "GPS162", length = 5, nullable = false) // @rpg-trace: schema
    private String gps162;

    @Column(name = "GPS170", precision = 3, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps170;

    @Column(name = "GPS180", precision = 13, scale = 2, nullable = false) // @rpg-trace: schema
    private BigDecimal gps180;

    @Column(name = "GPS190", precision = 5, scale = 2, nullable = false) // @rpg-trace: schema
    private BigDecimal gps190;

    @Column(name = "GPS200", precision = 15, scale = 2, nullable = false) // @rpg-trace: schema
    private BigDecimal gps200;

    @Column(name = "GPS210", precision = 7, scale = 0, nullable = false) // @rpg-trace: schema
    private BigDecimal gps210;

    @Column(name = "GPS220", length = 5, nullable = false) // @rpg-trace: schema
    private String gps220;

    @Column(name = "GPS230", length = 1, nullable = false) // @rpg-trace: schema
    private String gps230;

    @Column(name = "GPS240", length = 2, nullable = false) // @rpg-trace: schema
    private String gps240;

    @Column(name = "GPS250", length = 2000, nullable = false) // @rpg-trace: schema
    private String gps250;

    public Hsgpspf() {
    }

    public String getGps000() {
        return gps000;
    }

    public void setGps000(String gps000) {
        this.gps000 = gps000;
    }

    public String getGps010() {
        return gps010;
    }

    public void setGps010(String gps010) {
        this.gps010 = gps010;
    }

    public String getGps020() {
        return gps020;
    }

    public void setGps020(String gps020) {
        this.gps020 = gps020;
    }

    public String getGps025() {
        return gps025;
    }

    public void setGps025(String gps025) {
        this.gps025 = gps025;
    }

    public BigDecimal getGps030() {
        return gps030;
    }

    public void setGps030(BigDecimal gps030) {
        this.gps030 = gps030;
    }

    public BigDecimal getGps035() {
        return gps035;
    }

    public void setGps035(BigDecimal gps035) {
        this.gps035 = gps035;
    }

    public String getGps040() {
        return gps040;
    }

    public void setGps040(String gps040) {
        this.gps040 = gps040;
    }

    public String getGps050() {
        return gps050;
    }

    public void setGps050(String gps050) {
        this.gps050 = gps050;
    }

    public BigDecimal getGps060() {
        return gps060;
    }

    public void setGps060(BigDecimal gps060) {
        this.gps060 = gps060;
    }

    public BigDecimal getGps070() {
        return gps070;
    }

    public void setGps070(BigDecimal gps070) {
        this.gps070 = gps070;
    }

    public String getGps080() {
        return gps080;
    }

    public void setGps080(String gps080) {
        this.gps080 = gps080;
    }

    public String getGps090() {
        return gps090;
    }

    public void setGps090(String gps090) {
        this.gps090 = gps090;
    }

    public BigDecimal getGps100() {
        return gps100;
    }

    public void setGps100(BigDecimal gps100) {
        this.gps100 = gps100;
    }

    public BigDecimal getGps110() {
        return gps110;
    }

    public void setGps110(BigDecimal gps110) {
        this.gps110 = gps110;
    }

    public BigDecimal getGps120() {
        return gps120;
    }

    public void setGps120(BigDecimal gps120) {
        this.gps120 = gps120;
    }

    public String getGps130() {
        return gps130;
    }

    public void setGps130(String gps130) {
        this.gps130 = gps130;
    }

    public BigDecimal getGps140() {
        return gps140;
    }

    public void setGps140(BigDecimal gps140) {
        this.gps140 = gps140;
    }

    public BigDecimal getGps150() {
        return gps150;
    }

    public void setGps150(BigDecimal gps150) {
        this.gps150 = gps150;
    }

    public String getGps160() {
        return gps160;
    }

    public void setGps160(String gps160) {
        this.gps160 = gps160;
    }

    public String getGps161() {
        return gps161;
    }

    public void setGps161(String gps161) {
        this.gps161 = gps161;
    }

    public String getGps162() {
        return gps162;
    }

    public void setGps162(String gps162) {
        this.gps162 = gps162;
    }

    public BigDecimal getGps170() {
        return gps170;
    }

    public void setGps170(BigDecimal gps170) {
        this.gps170 = gps170;
    }

    public BigDecimal getGps180() {
        return gps180;
    }

    public void setGps180(BigDecimal gps180) {
        this.gps180 = gps180;
    }

    public BigDecimal getGps190() {
        return gps190;
    }

    public void setGps190(BigDecimal gps190) {
        this.gps190 = gps190;
    }

    public BigDecimal getGps200() {
        return gps200;
    }

    public void setGps200(BigDecimal gps200) {
        this.gps200 = gps200;
    }

    public BigDecimal getGps210() {
        return gps210;
    }

    public void setGps210(BigDecimal gps210) {
        this.gps210 = gps210;
    }

    public String getGps220() {
        return gps220;
    }

    public void setGps220(String gps220) {
        this.gps220 = gps220;
    }

    public String getGps230() {
        return gps230;
    }

    public void setGps230(String gps230) {
        this.gps230 = gps230;
    }

    public String getGps240() {
        return gps240;
    }

    public void setGps240(String gps240) {
        this.gps240 = gps240;
    }

    public String getGps250() {
        return gps250;
    }

    public void setGps250(String gps250) {
        this.gps250 = gps250;
    }
}