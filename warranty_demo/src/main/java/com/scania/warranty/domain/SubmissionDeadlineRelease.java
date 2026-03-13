/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

/**
 * JPA entity for submission deadline release (HSG70F).
 */
@Entity
@Table(name = "HSG70F")
public class SubmissionDeadlineRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "KZL", length = 3, nullable = false)
    private String kzl; // @rpg-trace: schema

    @Column(name = "R.NR.", length = 5, nullable = false)
    private String rnr; // @rpg-trace: schema

    @Column(name = "R.DAT", length = 8, nullable = false)
    private String rdat; // @rpg-trace: schema

    @Column(name = "FGNR.", length = 17, nullable = false)
    private String fgnr; // @rpg-trace: schema

    @Column(name = "REP.DAT.", length = 8, nullable = false)
    private String repdat; // @rpg-trace: schema

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status; // @rpg-trace: schema

    @Column(name = "CUS.NO.", precision = 5, scale = 0, nullable = false)
    private Integer cusNo; // @rpg-trace: schema

    @Column(name = "D.C.NO.", precision = 8, scale = 0, nullable = false)
    private Integer dcNo; // @rpg-trace: schema

    @Column(name = "D.C.FN.", length = 5, nullable = false)
    private String dcFn; // @rpg-trace: schema

    public SubmissionDeadlineRelease() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKzl() {
        return kzl;
    }

    public void setKzl(String kzl) {
        this.kzl = kzl;
    }

    public String getRnr() {
        return rnr;
    }

    public void setRnr(String rnr) {
        this.rnr = rnr;
    }

    public String getRdat() {
        return rdat;
    }

    public void setRdat(String rdat) {
        this.rdat = rdat;
    }

    public String getFgnr() {
        return fgnr;
    }

    public void setFgnr(String fgnr) {
        this.fgnr = fgnr;
    }

    public String getRepdat() {
        return repdat;
    }

    public void setRepdat(String repdat) {
        this.repdat = repdat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCusNo() {
        return cusNo;
    }

    public void setCusNo(Integer cusNo) {
        this.cusNo = cusNo;
    }

    public Integer getDcNo() {
        return dcNo;
    }

    public void setDcNo(Integer dcNo) {
        this.dcNo = dcNo;
    }

    public String getDcFn() {
        return dcFn;
    }

    public void setDcFn(String dcFn) {
        this.dcFn = dcFn;
    }
}