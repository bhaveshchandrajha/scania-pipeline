/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSG70F")
@IdClass(ClaimReleaseRequestId.class)
public class ClaimReleaseRequest {

    @Id
    @Column(name = "G70_KZL", length = 3, nullable = false)
    private String g70Kzl; // @rpg-trace: schema

    @Id
    @Column(name = "G70_RNR", length = 5, nullable = false)
    private String g70Rnr; // @rpg-trace: schema

    @Id
    @Column(name = "G70_RDAT", length = 8, nullable = false)
    private String g70Rdat; // @rpg-trace: schema

    @Column(name = "G70_FGNR", length = 17, nullable = false)
    private String g70Fgnr; // @rpg-trace: schema

    @Column(name = "G70_DAT", length = 8, nullable = false)
    private String g70Dat; // @rpg-trace: schema

    @Column(name = "G70_STATUS", length = 1, nullable = false)
    private String g70Status; // @rpg-trace: schema

    @Column(name = "G70_CUSNO", precision = 5, scale = 0, nullable = false)
    private BigDecimal g70Cusno; // @rpg-trace: schema

    @Column(name = "G70_CLMNO", precision = 8, scale = 0, nullable = false)
    private BigDecimal g70Clmno; // @rpg-trace: schema

    @Column(name = "G70_CLMFL", length = 5, nullable = false)
    private String g70Clmfl; // @rpg-trace: schema

    public ClaimReleaseRequest() {}

    public String getG70Kzl() { return g70Kzl; }
    public void setG70Kzl(String g70Kzl) { this.g70Kzl = g70Kzl; }
    public String getG70Rnr() { return g70Rnr; }
    public void setG70Rnr(String g70Rnr) { this.g70Rnr = g70Rnr; }
    public String getG70Rdat() { return g70Rdat; }
    public void setG70Rdat(String g70Rdat) { this.g70Rdat = g70Rdat; }
    public String getG70Fgnr() { return g70Fgnr; }
    public void setG70Fgnr(String g70Fgnr) { this.g70Fgnr = g70Fgnr; }
    public String getG70Dat() { return g70Dat; }
    public void setG70Dat(String g70Dat) { this.g70Dat = g70Dat; }
    public String getG70Status() { return g70Status; }
    public void setG70Status(String g70Status) { this.g70Status = g70Status; }
    public BigDecimal getG70Cusno() { return g70Cusno; }
    public void setG70Cusno(BigDecimal g70Cusno) { this.g70Cusno = g70Cusno; }
    public BigDecimal getG70Clmno() { return g70Clmno; }
    public void setG70Clmno(BigDecimal g70Clmno) { this.g70Clmno = g70Clmno; }
    public String getG70Clmfl() { return g70Clmfl; }
    public void setG70Clmfl(String g70Clmfl) { this.g70Clmfl = g70Clmfl; }
}