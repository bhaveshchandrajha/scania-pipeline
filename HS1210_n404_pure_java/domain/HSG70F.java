package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG70F")
public class HSG70F {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "KZL", length = 3)
    private String kzl;
    
    @Column(name = "R.NR.", length = 5)
    private String rnr;
    
    @Column(name = "R.DAT", length = 8)
    private String rdat;
    
    @Column(name = "FGNR.", length = 17)
    private String fgnr;
    
    @Column(name = "REP.DAT.", length = 8)
    private String repdat;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "CUS.NO.", precision = 5, scale = 0)
    private Integer cusno;
    
    @Column(name = "D.C.NO.", precision = 8, scale = 0)
    private Integer dcno;
    
    @Column(name = "D.C.FN.", length = 5)
    private String dcfn;

    // Constructors
    public HSG70F() {
    }

    // Getters and Setters
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

    public Integer getCusno() {
        return cusno;
    }

    public void setCusno(Integer cusno) {
        this.cusno = cusno;
    }

    public Integer getDcno() {
        return dcno;
    }

    public void setDcno(Integer dcno) {
        this.dcno = dcno;
    }

    public String getDcfn() {
        return dcfn;
    }

    public void setDcfn(String dcfn) {
        this.dcfn = dcfn;
    }
}