package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "HSG71LF2")
public class Claim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String pakz;
    
    @Column(name = "RECH.-NR.", length = 5)
    private String rechNr;
    
    @Column(name = "RECH.-DATUM", length = 8)
    private String rechDatum;
    
    @Column(name = "AUFTRAGS-NR.", length = 5)
    private String auftragsNr;
    
    @Column(name = "WETE", length = 1)
    private String wete;
    
    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNr;
    
    @Column(name = "CHASSIS-NR.", length = 7)
    private String chassisNr;
    
    @Column(name = "KENNZEICHEN", length = 10)
    private String kennzeichen;
    
    @Column(name = "ZUL.-DATUM", precision = 8, scale = 0)
    private Integer zulDatum;
    
    @Column(name = "REP.-DATUM", precision = 8, scale = 0)
    private Integer repDatum;
    
    @Column(name = "KM-STAND", precision = 3, scale = 0)
    private Integer kmStand;
    
    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0)
    private Integer produktTyp;
    
    @Column(name = "ANHANG", length = 1)
    private String anhang;
    
    @Column(name = "AUSL#NDER", length = 1)
    private String auslaender;
    
    @Column(name = "KD-NR.", length = 6)
    private String kdNr;
    
    @Column(name = "KD-NAME", length = 30)
    private String kdName;
    
    @Column(name = "CLAIM-NR. SDE", length = 8)
    private String claimNrSde;
    
    @Column(name = "STATUS CODE SDE", precision = 2, scale = 0)
    private Integer statusCodeSde;
    
    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0)
    private Integer anzFehler;
    
    @Column(name = "BEREICH", length = 1)
    private String bereich;
    
    @Column(name = "AUF.NR.", length = 10)
    private String aufNr;
    
    public Claim() {
    }
    
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
    
    public String getRechNr() {
        return rechNr;
    }
    
    public void setRechNr(String rechNr) {
        this.rechNr = rechNr;
    }
    
    public String getRechDatum() {
        return rechDatum;
    }
    
    public void setRechDatum(String rechDatum) {
        this.rechDatum = rechDatum;
    }
    
    public String getAuftragsNr() {
        return auftragsNr;
    }
    
    public void setAuftragsNr(String auftragsNr) {
        this.auftragsNr = auftragsNr;
    }
    
    public String getWete() {
        return wete;
    }
    
    public void setWete(String wete) {
        this.wete = wete;
    }
    
    public String getClaimNr() {
        return claimNr;
    }
    
    public void setClaimNr(String claimNr) {
        this.claimNr = claimNr;
    }
    
    public String getChassisNr() {
        return chassisNr;
    }
    
    public void setChassisNr(String chassisNr) {
        this.chassisNr = chassisNr;
    }
    
    public String getKennzeichen() {
        return kennzeichen;
    }
    
    public void setKennzeichen(String kennzeichen) {
        this.kennzeichen = kennzeichen;
    }
    
    public Integer getZulDatum() {
        return zulDatum;
    }
    
    public void setZulDatum(Integer zulDatum) {
        this.zulDatum = zulDatum;
    }
    
    public Integer getRepDatum() {
        return repDatum;
    }
    
    public void setRepDatum(Integer repDatum) {
        this.repDatum = repDatum;
    }
    
    public Integer getKmStand() {
        return kmStand;
    }
    
    public void setKmStand(Integer kmStand) {
        this.kmStand = kmStand;
    }
    
    public Integer getProduktTyp() {
        return produktTyp;
    }
    
    public void setProduktTyp(Integer produktTyp) {
        this.produktTyp = produktTyp;
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
    
    public String getKdNr() {
        return kdNr;
    }
    
    public void setKdNr(String kdNr) {
        this.kdNr = kdNr;
    }
    
    public String getKdName() {
        return kdName;
    }
    
    public void setKdName(String kdName) {
        this.kdName = kdName;
    }
    
    public String getClaimNrSde() {
        return claimNrSde;
    }
    
    public void setClaimNrSde(String claimNrSde) {
        this.claimNrSde = claimNrSde;
    }
    
    public Integer getStatusCodeSde() {
        return statusCodeSde;
    }
    
    public void setStatusCodeSde(Integer statusCodeSde) {
        this.statusCodeSde = statusCodeSde;
    }
    
    public Integer getAnzFehler() {
        return anzFehler;
    }
    
    public void setAnzFehler(Integer anzFehler) {
        this.anzFehler = anzFehler;
    }
    
    public String getBereich() {
        return bereich;
    }
    
    public void setBereich(String bereich) {
        this.bereich = bereich;
    }
    
    public String getAufNr() {
        return aufNr;
    }
    
    public void setAufNr(String aufNr) {
        this.aufNr = aufNr;
    }
}