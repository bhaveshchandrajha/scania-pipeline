package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG70F")
public class WarrantyRelease {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "KZL", length = 3)
    private String kzl;
    
    @Column(name = "R.NR.", length = 5)
    private String rNr;
    
    @Column(name = "R.DAT", length = 8)
    private String rDat;
    
    @Column(name = "FGNR.", length = 17)
    private String fgnr;
    
    @Column(name = "REP.DAT.", length = 8)
    private String repDat;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "CUS.NO.", precision = 5, scale = 0)
    private Integer cusNo;
    
    @Column(name = "D.C.NO.", precision = 8, scale = 0)
    private Integer dcNo;
    
    @Column(name = "D.C.FN.", length = 5)
    private String dcFn;
    
    public WarrantyRelease() {
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
    
    public String getrNr() {
        return rNr;
    }
    
    public void setrNr(String rNr) {
        this.rNr = rNr;
    }
    
    public String getrDat() {
        return rDat;
    }
    
    public void setrDat(String rDat) {
        this.rDat = rDat;
    }
    
    public String getFgnr() {
        return fgnr;
    }
    
    public void setFgnr(String fgnr) {
        this.fgnr = fgnr;
    }
    
    public String getRepDat() {
        return repDat;
    }
    
    public void setRepDat(String repDat) {
        this.repDat = repDat;
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