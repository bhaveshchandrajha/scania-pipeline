/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.io.Serializable;

// This entity represents the HSEPAF physical file (Extended Part Agreement)
// Used in SETLL/READE operations in CheckV4
@Entity
@Table(name = "HSEPAF")
@IdClass(ExtendedPartAgreementId.class)
public class ExtendedPartAgreement implements Serializable {

    @Id
    @Column(name = "EPA000")
    private String epa000; // @rpg-trace: schema

    @Id
    @Column(name = "EPA040")
    private String epa040; // @rpg-trace: schema

    @Id
    @Column(name = "EPA050")
    private String epa050; // @rpg-trace: schema

    @Id
    @Column(name = "EPA060")
    private String epa060; // @rpg-trace: schema

    @Id
    @Column(name = "EPA_TYPE")
    private String epaType; // @rpg-trace: schema

    @Column(name = "EPA_DATV")
    private String epaDatv; // @rpg-trace: schema

    public ExtendedPartAgreement() {}

    public String getEpa000() { return epa000; }
    public void setEpa000(String epa000) { this.epa000 = epa000; }

    public String getEpa040() { return epa040; }
    public void setEpa040(String epa040) { this.epa040 = epa040; }

    public String getEpa050() { return epa050; }
    public void setEpa050(String epa050) { this.epa050 = epa050; }

    public String getEpa060() { return epa060; }
    public void setEpa060(String epa060) { this.epa060 = epa060; }

    public String getEpaType() { return epaType; }
    public void setEpaType(String epaType) { this.epaType = epaType; }

    public String getEpaDatv() { return epaDatv; }
    public void setEpaDatv(String epaDatv) { this.epaDatv = epaDatv; }
}