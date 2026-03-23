/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.io.Serializable;

// This entity represents the HSAHKPF physical file (Invoice Header master)
// Reusing field patterns from existing Invoice entity but for the physical file
// Since HSAHKPF is not in existing entities, we create a lightweight entity
// with the key fields referenced in the RPG CHAIN operation
@Entity
@Table(name = "HSAHKPF")
@IdClass(InvoiceHeaderId.class)
public class InvoiceHeader implements Serializable {

    @Id
    @Column(name = "AHK000")
    private String ahk000; // @rpg-trace: schema

    @Id
    @Column(name = "AHK010")
    private String ahk010; // @rpg-trace: schema

    @Id
    @Column(name = "AHK020")
    private String ahk020; // @rpg-trace: schema

    @Column(name = "AHK025", length = 1)
    private String ahk025; // @rpg-trace: schema (blank in CHAIN key)

    @Id
    @Column(name = "AHK030")
    private String ahk030; // @rpg-trace: schema

    @Id
    @Column(name = "AHK040")
    private String ahk040; // @rpg-trace: schema

    @Id
    @Column(name = "AHK050")
    private String ahk050; // @rpg-trace: schema

    @Id
    @Column(name = "AHK060")
    private String ahk060; // @rpg-trace: schema

    @Column(name = "AHK080")
    private String ahk080; // @rpg-trace: schema

    @Column(name = "AHK190", length = 6)
    private String ahk190; // @rpg-trace: schema (G71190 in CHAIN key)

    @Column(name = "AHK200", length = 6)
    private String ahk200; // @rpg-trace: schema (%Subst(G71200:8:2) in CHAIN key)

    public InvoiceHeader() {}

    public String getAhk000() { return ahk000; }
    public void setAhk000(String ahk000) { this.ahk000 = ahk000; }

    public String getAhk010() { return ahk010; }
    public void setAhk010(String ahk010) { this.ahk010 = ahk010; }

    public String getAhk020() { return ahk020; }
    public void setAhk020(String ahk020) { this.ahk020 = ahk020; }

    public String getAhk025() { return ahk025; }
    public void setAhk025(String ahk025) { this.ahk025 = ahk025; }

    public String getAhk030() { return ahk030; }
    public void setAhk030(String ahk030) { this.ahk030 = ahk030; }

    public String getAhk040() { return ahk040; }
    public void setAhk040(String ahk040) { this.ahk040 = ahk040; }

    public String getAhk050() { return ahk050; }
    public void setAhk050(String ahk050) { this.ahk050 = ahk050; }

    public String getAhk060() { return ahk060; }
    public void setAhk060(String ahk060) { this.ahk060 = ahk060; }

    public String getAhk080() { return ahk080; }
    public void setAhk080(String ahk080) { this.ahk080 = ahk080; }

    public String getAhk190() { return ahk190; }
    public void setAhk190(String ahk190) { this.ahk190 = ahk190; }

    public String getAhk200() { return ahk200; }
    public void setAhk200(String ahk200) { this.ahk200 = ahk200; }
}