package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG71LF2")
public class HSG71LF2 {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String pakz;
    
    @Column(name = "RECH.-NR.", length = 5)
    private String rechnr;
    
    @Column(name = "RECH.-DATUM", length = 8)
    private String rechdatum;
    
    @Column(name = "AUFTRAGS-NR.", length = 5)
    private String auftragsnr;
    
    @Column(name = "WETE", length = 1)
    private String wete;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimnr;
    
    @Column(name = "CHASSIS-NR.", length = 7)
    private String chassisnr;
    
    @Column(name = "KENNZEICHEN", length = 10)
    private String kennzeichen;
    
    @Column(name = "ZUL.-DATUM", precision = 8, scale = 0)
    private Integer zuldatum;
    
    @Column(name = "REP.-DATUM", precision = 8, scale = 0)
    private Integer repdatum;
    
    @Column(name = "KM-STAND", precision = 3, scale = 0)
    private Integer kmstand;
    
    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0)
    private Integer produkttyp;
    
    @Column(name = "ANHANG", length = 1)
    private String anhang;
    
    @Column(name = "AUSL#NDER", length = 1)
    private String auslaender;
    
    @Column(name = "KD-NR.", length = 6)
    private String kdnr;
    
    @Column(name = "KD-NAME", length = 30)
    private String kdname;
    
    @Column(name = "CLAIM-NR. SDE", length = 8)
    private String claimnrsde;
    
    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0)
    private Integer statuscodesde;
    
    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0)
    private Integer anzfehler;
    
    @Column(name = "BEREICH", length = 1)
    private String bereich;
    
    @Column(name = "AUF.NR.", length = 10)
    private String aufnr;

    // Constructors
    public HSG71LF2() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPakz() {
        return pakz;
    }

    public void setPakz(String pakz) {
        this.pakz = pakz;
    }

    public String getRechnr() {
        return rechnr;
    }

    public void setRechnr(String rechnr) {
        this.rechnr = rechnr;
    }

    public String getRechdatum() {
        return rechdatum;
    }

    public void setRechdatum(String rechdatum) {
        this.rechdatum = rechdatum;
    }

    public String getAuftragsnr() {
        return auftragsnr;
    }

    public void setAuftragsnr(String auftragsnr) {
        this.auftragsnr = auftragsnr;
    }

    public String getWete() {
        return wete;
    }

    public void setWete(String wete) {
        this.wete = wete;
    }

    public String getClaimnr() {
        return claimnr;
    }

    public void setClaimnr(String claimnr) {
        this.claimnr = claimnr;
    }

    public String getChassisnr() {
        return chassisnr;
    }

    public void setChassisnr(String chassisnr) {
        this.chassisnr = chassisnr;
    }

    public String getKennzeichen() {
        return kennzeichen;
    }

    public void setKennzeichen(String kennzeichen) {
        this.kennzeichen = kennzeichen;
    }

    public Integer getZuldatum() {
        return zuldatum;
    }

    public void setZuldatum(Integer zuldatum) {
        this.zuldatum = zuldatum;
    }

    public Integer getRepdatum() {
        return repdatum;
    }

    public void setRepdatum(Integer repdatum) {
        this.repdatum = repdatum;
    }

    public Integer getKmstand() {
        return kmstand;
    }

    public void setKmstand(Integer kmstand) {
        this.kmstand = kmstand;
    }

    public Integer getProdukttyp() {
        return produkttyp;
    }

    public void setProdukttyp(Integer produkttyp) {
        this.produkttyp = produkttyp;
    }

    public String getAnhang() {
        return anhang;
    }

    public void setAnhang(String anhang) {
        this.anhang = anhang;
    }

    public String getAuslaender() {
        return auslaender;
    }

    public void setAuslaender(String auslaender) {
        this.auslaender = auslaender;
    }

    public String getKdnr() {
        return kdnr;
    }

    public void setKdnr(String kdnr) {
        this.kdnr = kdnr;
    }

    public String getKdname() {
        return kdname;
    }

    public void setKdname(String kdname) {
        this.kdname = kdname;
    }

    public String getClaimnrsde() {
        return claimnrsde;
    }

    public void setClaimnrsde(String claimnrsde) {
        this.claimnrsde = claimnrsde;
    }

    public Integer getStatuscodesde() {
        return statuscodesde;
    }

    public void setStatuscodesde(Integer statuscodesde) {
        this.statuscodesde = statuscodesde;
    }

    public Integer getAnzfehler() {
        return anzfehler;
    }

    public void setAnzfehler(Integer anzfehler) {
        this.anzfehler = anzfehler;
    }

    public String getBereich() {
        return bereich;
    }

    public void setBereich(String bereich) {
        this.bereich = bereich;
    }

    public String getAufnr() {
        return aufnr;
    }

    public void setAufnr(String aufnr) {
        this.aufnr = aufnr;
    }
}