/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * JPA entity for work positions (HSAHWPF).
 */
@Entity
@Table(name = "HSAHWPF")
public class WorkPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String companyCode;
    
    @Column(name = "RNR", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RG-NR. 10A", length = 10)
    private String invoiceNumber10;
    
    @Column(name = "RDAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "KZ S", length = 1)
    private String indicatorS;
    
    @Column(name = "ANR", length = 5)
    private String orderNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "W/T", length = 1)
    private String workshopTheke;
    
    @Column(name = "SPLITT", length = 2)
    private String split;
    
    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer position;
    
    @Column(name = "EC", length = 2)
    private String ec;
    
    @Column(name = "LNR PAK", precision = 3, scale = 0)
    private Integer lnrPak;
    
    @Column(name = "PAKET-NR.", length = 8)
    private String paketNr;
    
    @Column(name = "SORT RZ", precision = 3, scale = 0)
    private Integer sortRz;
    
    @Column(name = "LNR RZ", precision = 3, scale = 0)
    private Integer lnrRz;
    
    @Column(name = "AG", length = 8)
    private String ag;
    
    @Column(name = "L.NR.", length = 3)
    private String lnr;
    
    @Column(name = "BEZ.", length = 40)
    private String description;
    
    @Column(name = "WERKSZEIT", precision = 5, scale = 2)
    private BigDecimal werkszeit;
    
    @Column(name = "AW-STUNDEN", precision = 5, scale = 2)
    private BigDecimal awStunden;
    
    @Column(name = "ZE", precision = 5, scale = 0)
    private Integer ze;
    
    @Column(name = "PE", precision = 5, scale = 0)
    private Integer pe;
    
    @Column(name = "SATZ-PE", precision = 5, scale = 2)
    private BigDecimal satzPe;
    
    @Column(name = "GEW-ZE", length = 1)
    private String gewZe;
    
    @Column(name = "PREIS", precision = 9, scale = 2)
    private BigDecimal preis;
    
    @Column(name = "MONTEUR", length = 3)
    private String monteur;
    
    @Column(name = "BC", length = 2)
    private String bc;
    
    @Column(name = "V-SATZ", precision = 5, scale = 2)
    private BigDecimal vSatz;
    
    @Column(name = "M-STUNDEN", precision = 5, scale = 2)
    private BigDecimal mStunden;
    
    @Column(name = "V-DM-NETTO", precision = 9, scale = 2)
    private BigDecimal vDmNetto;
    
    @Column(name = "V-DM BRUTTO", precision = 9, scale = 2)
    private BigDecimal vDmBrutto;
    
    @Column(name = "V-STUNDEN", precision = 9, scale = 2)
    private BigDecimal vStunden;
    
    @Column(name = "ZUSCHLAG", precision = 5, scale = 2)
    private BigDecimal zuschlag;
    
    @Column(name = "RABATT", precision = 5, scale = 2)
    private BigDecimal rabatt;
    
    @Column(name = "KZ S/AW", length = 1)
    private String kzSAw;
    
    @Column(name = "KZ-MWST", length = 1)
    private String kzMwst;
    
    @Column(name = "VERDICHTEN", length = 1)
    private String verdichten;
    
    @Column(name = "TXT-KEY", length = 3)
    private String txtKey;
    
    @Column(name = "RG BRUTTO", precision = 9, scale = 2)
    private BigDecimal rgBrutto;
    
    @Column(name = "RG RABATT", precision = 9, scale = 2)
    private BigDecimal rgRabatt;
    
    @Column(name = "RG NETTO", precision = 9, scale = 2)
    private BigDecimal rgNetto;
    
    @Column(name = "KEN.RE2SUM", length = 1)
    private String kenRe2Sum;
    
    @Column(name = "URSPR-FAK/H MON", precision = 5, scale = 2)
    private BigDecimal ursprFakHMon;
    
    @Column(name = "URSPR-NETTO MON", precision = 9, scale = 2)
    private BigDecimal ursprNettoMon;
    
    @Column(name = "EINSTANDSPREIS", precision = 9, scale = 2)
    private BigDecimal einstandspreis;
    
    @Column(name = "EPS NAME", length = 20)
    private String epsName;
    
    @Column(name = "EPS MINDERUNG %", precision = 5, scale = 2)
    private BigDecimal epsMinderungPercent;
    
    @Column(name = "VARIANTE", length = 500)
    private String variante;
    
    @Column(name = "ARBEITSBESCHREIBUNG", length = 2000)
    private String arbeitsbeschreibung;
    
    @Column(name = "RECHNUNGSTEXT", length = 2000)
    private String rechnungstext;

    // Constructors
    public WorkPosition() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getInvoiceNumber10() { return invoiceNumber10; }
    public void setInvoiceNumber10(String invoiceNumber10) { this.invoiceNumber10 = invoiceNumber10; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getIndicatorS() { return indicatorS; }
    public void setIndicatorS(String indicatorS) { this.indicatorS = indicatorS; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getWorkshopTheke() { return workshopTheke; }
    public void setWorkshopTheke(String workshopTheke) { this.workshopTheke = workshopTheke; }

    public String getSplit() { return split; }
    public void setSplit(String split) { this.split = split; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getEc() { return ec; }
    public void setEc(String ec) { this.ec = ec; }

    public Integer getLnrPak() { return lnrPak; }
    public void setLnrPak(Integer lnrPak) { this.lnrPak = lnrPak; }

    public String getPaketNr() { return paketNr; }
    public void setPaketNr(String paketNr) { this.paketNr = paketNr; }

    public Integer getSortRz() { return sortRz; }
    public void setSortRz(Integer sortRz) { this.sortRz = sortRz; }

    public Integer getLnrRz() { return lnrRz; }
    public void setLnrRz(Integer lnrRz) { this.lnrRz = lnrRz; }

    public String getAg() { return ag; }
    public void setAg(String ag) { this.ag = ag; }

    public String getLnr() { return lnr; }
    public void setLnr(String lnr) { this.lnr = lnr; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getWerkszeit() { return werkszeit; }
    public void setWerkszeit(BigDecimal werkszeit) { this.werkszeit = werkszeit; }

    public BigDecimal getAwStunden() { return awStunden; }
    public void setAwStunden(BigDecimal awStunden) { this.awStunden = awStunden; }

    public Integer getZe() { return ze; }
    public void setZe(Integer ze) { this.ze = ze; }

    public Integer getPe() { return pe; }
    public void setPe(Integer pe) { this.pe = pe; }

    public BigDecimal getSatzPe() { return satzPe; }
    public void setSatzPe(BigDecimal satzPe) { this.satzPe = satzPe; }

    public String getGewZe() { return gewZe; }
    public void setGewZe(String gewZe) { this.gewZe = gewZe; }

    public BigDecimal getPreis() { return preis; }
    public void setPreis(BigDecimal preis) { this.preis = preis; }

    public String getMonteur() { return monteur; }
    public void setMonteur(String monteur) { this.monteur = monteur; }

    public String getBc() { return bc; }
    public void setBc(String bc) { this.bc = bc; }

    public BigDecimal getVSatz() { return vSatz; }
    public void setVSatz(BigDecimal vSatz) { this.vSatz = vSatz; }

    public BigDecimal getMStunden() { return mStunden; }
    public void setMStunden(BigDecimal mStunden) { this.mStunden = mStunden; }

    public BigDecimal getVDmNetto() { return vDmNetto; }
    public void setVDmNetto(BigDecimal vDmNetto) { this.vDmNetto = vDmNetto; }

    public BigDecimal getVDmBrutto() { return vDmBrutto; }
    public void setVDmBrutto(BigDecimal vDmBrutto) { this.vDmBrutto = vDmBrutto; }

    public BigDecimal getVStunden() { return vStunden; }
    public void setVStunden(BigDecimal vStunden) { this.vStunden = vStunden; }

    public BigDecimal getZuschlag() { return zuschlag; }
    public void setZuschlag(BigDecimal zuschlag) { this.zuschlag = zuschlag; }

    public BigDecimal getRabatt() { return rabatt; }
    public void setRabatt(BigDecimal rabatt) { this.rabatt = rabatt; }

    public String getKzSAw() { return kzSAw; }
    public void setKzSAw(String kzSAw) { this.kzSAw = kzSAw; }

    public String getKzMwst() { return kzMwst; }
    public void setKzMwst(String kzMwst) { this.kzMwst = kzMwst; }

    public String getVerdichten() { return verdichten; }
    public void setVerdichten(String verdichten) { this.verdichten = verdichten; }

    public String getTxtKey() { return txtKey; }
    public void setTxtKey(String txtKey) { this.txtKey = txtKey; }

    public BigDecimal getRgBrutto() { return rgBrutto; }
    public void setRgBrutto(BigDecimal rgBrutto) { this.rgBrutto = rgBrutto; }

    public BigDecimal getRgRabatt() { return rgRabatt; }
    public void setRgRabatt(BigDecimal rgRabatt) { this.rgRabatt = rgRabatt; }

    public BigDecimal getRgNetto() { return rgNetto; }
    public void setRgNetto(BigDecimal rgNetto) { this.rgNetto = rgNetto; }

    public String getKenRe2Sum() { return kenRe2Sum; }
    public void setKenRe2Sum(String kenRe2Sum) { this.kenRe2Sum = kenRe2Sum; }

    public BigDecimal getUrsprFakHMon() { return ursprFakHMon; }
    public void setUrsprFakHMon(BigDecimal ursprFakHMon) { this.ursprFakHMon = ursprFakHMon; }

    public BigDecimal getUrsprNettoMon() { return ursprNettoMon; }
    public void setUrsprNettoMon(BigDecimal ursprNettoMon) { this.ursprNettoMon = ursprNettoMon; }

    public BigDecimal getEinstandspreis() { return einstandspreis; }
    public void setEinstandspreis(BigDecimal einstandspreis) { this.einstandspreis = einstandspreis; }

    public String getEpsName() { return epsName; }
    public void setEpsName(String epsName) { this.epsName = epsName; }

    public BigDecimal getEpsMinderungPercent() { return epsMinderungPercent; }
    public void setEpsMinderungPercent(BigDecimal epsMinderungPercent) { this.epsMinderungPercent = epsMinderungPercent; }

    public String getVariante() { return variante; }
    public void setVariante(String variante) { this.variante = variante; }

    public String getArbeitsbeschreibung() { return arbeitsbeschreibung; }
    public void setArbeitsbeschreibung(String arbeitsbeschreibung) { this.arbeitsbeschreibung = arbeitsbeschreibung; }

    public String getRechnungstext() { return rechnungstext; }
    public void setRechnungstext(String rechnungstext) { this.rechnungstext = rechnungstext; }
}