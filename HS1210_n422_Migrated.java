package com.scania.warranty.claim;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ============================================================================
// JPA Entities
// ============================================================================

@Entity
@Table(name = "HSG71PF")
class HSG71PF {
    @Id
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
    private BigDecimal zulDatum;

    @Column(name = "REP.-DATUM", precision = 8, scale = 0)
    private BigDecimal repDatum;

    @Column(name = "KM-STAND", precision = 3, scale = 0)
    private BigDecimal kmStand;

    @Column(name = "PRODUKT-TYP", precision = 1, scale = 0)
    private BigDecimal produktTyp;

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
    private BigDecimal statusCodeSde;

    @Column(name = "ANZ. FEHLER", precision = 2, scale = 0)
    private BigDecimal anzFehler;

    @Column(name = "BEREICH", length = 1)
    private String bereich;

    @Column(name = "AUF.NR.", length = 10)
    private String aufNr;

    // Getters and Setters
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRechNr() { return rechNr; }
    public void setRechNr(String rechNr) { this.rechNr = rechNr; }
    public String getRechDatum() { return rechDatum; }
    public void setRechDatum(String rechDatum) { this.rechDatum = rechDatum; }
    public String getAuftragsNr() { return auftragsNr; }
    public void setAuftragsNr(String auftragsNr) { this.auftragsNr = auftragsNr; }
    public String getWete() { return wete; }
    public void setWete(String wete) { this.wete = wete; }
    public String getClaimNr() { return claimNr; }
    public void setClaimNr(String claimNr) { this.claimNr = claimNr; }
    public String getChassisNr() { return chassisNr; }
    public void setChassisNr(String chassisNr) { this.chassisNr = chassisNr; }
    public String getKennzeichen() { return kennzeichen; }
    public void setKennzeichen(String kennzeichen) { this.kennzeichen = kennzeichen; }
    public BigDecimal getZulDatum() { return zulDatum; }
    public void setZulDatum(BigDecimal zulDatum) { this.zulDatum = zulDatum; }
    public BigDecimal getRepDatum() { return repDatum; }
    public void setRepDatum(BigDecimal repDatum) { this.repDatum = repDatum; }
    public BigDecimal getKmStand() { return kmStand; }
    public void setKmStand(BigDecimal kmStand) { this.kmStand = kmStand; }
    public BigDecimal getProduktTyp() { return produktTyp; }
    public void setProduktTyp(BigDecimal produktTyp) { this.produktTyp = produktTyp; }
    public String getAnhang() { return anhang; }
    public void setAnhang(String anhang) { this.anhang = anhang; }
    public String getAuslaender() { return auslaender; }
    public void setAuslaender(String auslaender) { this.auslaender = auslaender; }
    public String getKdNr() { return kdNr; }
    public void setKdNr(String kdNr) { this.kdNr = kdNr; }
    public String getKdName() { return kdName; }
    public void setKdName(String kdName) { this.kdName = kdName; }
    public String getClaimNrSde() { return claimNrSde; }
    public void setClaimNrSde(String claimNrSde) { this.claimNrSde = claimNrSde; }
    public BigDecimal getStatusCodeSde() { return statusCodeSde; }
    public void setStatusCodeSde(BigDecimal statusCodeSde) { this.statusCodeSde = statusCodeSde; }
    public BigDecimal getAnzFehler() { return anzFehler; }
    public void setAnzFehler(BigDecimal anzFehler) { this.anzFehler = anzFehler; }
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    public String getAufNr() { return aufNr; }
    public void setAufNr(String aufNr) { this.aufNr = aufNr; }
}

@Entity
@Table(name = "S3F003")
class S3F003 {
    @Id
    @Column(name = "Dist Wrnty Cust No", length = 48)
    private String distWrntyCustNo;

    @Column(name = "G/A Cust. Number", length = 10)
    private String gaCustNumber;

    @Column(name = "Dist Name", length = 30)
    private String distName;

    @Column(name = "Dist Short Name 1", length = 15)
    private String distShortName1;

    @Column(name = "Dist Short Name 2", length = 6)
    private String distShortName2;

    @Column(name = "Dist Locn /Town", length = 15)
    private String distLocnTown;

    @Column(name = "Parts Dist. Number", length = 10)
    private String partsDistNumber;

    @Column(name = "Curr Claim Rg Start No", precision = 8, scale = 0)
    private BigDecimal currClaimRgStartNo;

    @Column(name = "Curr Claim Rg End No", precision = 8, scale = 0)
    private BigDecimal currClaimRgEndNo;

    @Column(name = "Prev Claim Rg Start No", precision = 8, scale = 0)
    private BigDecimal prevClaimRgStartNo;

    @Column(name = "Prev Claim Rg End No", precision = 8, scale = 0)
    private BigDecimal prevClaimRgEndNo;

    @Column(name = "Stnd Labour Rt/Hr", precision = 8, scale = 2)
    private BigDecimal stndLabourRtHr;

    @Column(name = "Labour Rt/Hr 2", precision = 8, scale = 2)
    private BigDecimal labourRtHr2;

    @Column(name = "Labour Rt/Hr 3", precision = 8, scale = 2)
    private BigDecimal labourRtHr3;

    @Column(name = "Eff Per Start Date 1", precision = 8, scale = 0)
    private BigDecimal effPerStartDate1;

    @Column(name = "Eff Per End Date 1", precision = 8, scale = 0)
    private BigDecimal effPerEndDate1;

    @Column(name = "Eff Per Start Date 2", precision = 8, scale = 0)
    private BigDecimal effPerStartDate2;

    @Column(name = "Eff Per End Date 2", precision = 8, scale = 0)
    private BigDecimal effPerEndDate2;

    @Column(name = "Eff Per Start Date 3", precision = 8, scale = 0)
    private BigDecimal effPerStartDate3;

    @Column(name = "Eff Per End Date 3", precision = 8, scale = 0)
    private BigDecimal effPerEndDate3;

    @Column(name = "Dist Lab Uplift", precision = 5, scale = 4)
    private BigDecimal distLabUplift;

    @Column(name = "Purch Split S/Order", precision = 3, scale = 0)
    private BigDecimal purchSplitSOrder;

    @Column(name = "Purch Split VOR", precision = 3, scale = 0)
    private BigDecimal purchSplitVOR;

    @Column(name = "L/Val Comp Uplift %", precision = 5, scale = 4)
    private BigDecimal lValCompUpliftPct;

    @Column(name = "BML/ Hand. Uplift %", precision = 5, scale = 4)
    private BigDecimal bmlHandUpliftPct;

    @Column(name = "Dist. Labour Uplift Factor 2", precision = 5, scale = 4)
    private BigDecimal distLabourUpliftFactor2;

    @Column(name = "Dist. Labour Uplift Factor 3", precision = 5, scale = 4)
    private BigDecimal distLabourUpliftFactor3;

    @Column(name = "Dist. compensation forcore uplift.", length = 1)
    private String distCompensationForcoreUplift;

    @Column(name = "Online dlr access all claims", length = 1)
    private String onlineDlrAccessAllClaims;

    @Column(name = "VAT code in Leg System", length = 10)
    private String vatCodeInLegSystem;

    @Column(name = "VAT %", precision = 5, scale = 3)
    private BigDecimal vatPct;

    // Getters and Setters
    public String getDistWrntyCustNo() { return distWrntyCustNo; }
    public void setDistWrntyCustNo(String distWrntyCustNo) { this.distWrntyCustNo = distWrntyCustNo; }
    public String getGaCustNumber() { return gaCustNumber; }
    public void setGaCustNumber(String gaCustNumber) { this.gaCustNumber = gaCustNumber; }
    public String getDistName() { return distName; }
    public void setDistName(String distName) { this.distName = distName; }
    public String getDistShortName1() { return distShortName1; }
    public void setDistShortName1(String distShortName1) { this.distShortName1 = distShortName1; }
    public String getDistShortName2() { return distShortName2; }
    public void setDistShortName2(String distShortName2) { this.distShortName2 = distShortName2; }
    public String getDistLocnTown() { return distLocnTown; }
    public void setDistLocnTown(String distLocnTown) { this.distLocnTown = distLocnTown; }
    public String getPartsDistNumber() { return partsDistNumber; }
    public void setPartsDistNumber(String partsDistNumber) { this.partsDistNumber = partsDistNumber; }
    public BigDecimal getCurrClaimRgStartNo() { return currClaimRgStartNo; }
    public void setCurrClaimRgStartNo(BigDecimal currClaimRgStartNo) { this.currClaimRgStartNo = currClaimRgStartNo; }
    public BigDecimal getCurrClaimRgEndNo() { return currClaimRgEndNo; }
    public void setCurrClaimRgEndNo(BigDecimal currClaimRgEndNo) { this.currClaimRgEndNo = currClaimRgEndNo; }
    public BigDecimal getPrevClaimRgStartNo() { return prevClaimRgStartNo; }
    public void setPrevClaimRgStartNo(BigDecimal prevClaimRgStartNo) { this.prevClaimRgStartNo = prevClaimRgStartNo; }
    public BigDecimal getPrevClaimRgEndNo() { return prevClaimRgEndNo; }
    public void setPrevClaimRgEndNo(BigDecimal prevClaimRgEndNo) { this.prevClaimRgEndNo = prevClaimRgEndNo; }
    public BigDecimal getStndLabourRtHr() { return stndLabourRtHr; }
    public void setStndLabourRtHr(BigDecimal stndLabourRtHr) { this.stndLabourRtHr = stndLabourRtHr; }
    public BigDecimal getLabourRtHr2() { return labourRtHr2; }
    public void setLabourRtHr2(BigDecimal labourRtHr2) { this.labourRtHr2 = labourRtHr2; }
    public BigDecimal getLabourRtHr3() { return labourRtHr3; }
    public void setLabourRtHr3(BigDecimal labourRtHr3) { this.labourRtHr3 = labourRtHr3; }
    public BigDecimal getEffPerStartDate1() { return effPerStartDate1; }
    public void setEffPerStartDate1(BigDecimal effPerStartDate1) { this.effPerStartDate1 = effPerStartDate1; }
    public BigDecimal getEffPerEndDate1() { return effPerEndDate1; }
    public void setEffPerEndDate1(BigDecimal effPerEndDate1) { this.effPerEndDate1 = effPerEndDate1; }
    public BigDecimal getEffPerStartDate2() { return effPerStartDate2; }
    public void setEffPerStartDate2(BigDecimal effPerStartDate2) { this.effPerStartDate2 = effPerStartDate2; }
    public BigDecimal getEffPerEndDate2() { return effPerEndDate2; }
    public void setEffPerEndDate2(BigDecimal effPerEndDate2) { this.effPerEndDate2 = effPerEndDate2; }
    public BigDecimal getEffPerStartDate3() { return effPerStartDate3; }
    public void setEffPerStartDate3(BigDecimal effPerStartDate3) { this.effPerStartDate3 = effPerStartDate3; }
    public BigDecimal getEffPerEndDate3() { return effPerEndDate3; }
    public void setEffPerEndDate3(BigDecimal effPerEndDate3) { this.effPerEndDate3 = effPerEndDate3; }
    public BigDecimal getDistLabUplift() { return distLabUplift; }
    public void setDistLabUplift(BigDecimal distLabUplift) { this.distLabUplift = distLabUplift; }
    public BigDecimal getPurchSplitSOrder() { return purchSplitSOrder; }
    public void setPurchSplitSOrder(BigDecimal purchSplitSOrder) { this.purchSplitSOrder = purchSplitSOrder; }
    public BigDecimal getPurchSplitVOR() { return purchSplitVOR; }
    public void setPurchSplitVOR(BigDecimal purchSplitVOR) { this.purchSplitVOR = purchSplitVOR; }
    public BigDecimal getlValCompUpliftPct() { return lValCompUpliftPct; }
    public void setlValCompUpliftPct(BigDecimal lValCompUpliftPct) { this.lValCompUpliftPct = lValCompUpliftPct; }
    public BigDecimal getBmlHandUpliftPct() { return bmlHandUpliftPct; }
    public void setBmlHandUpliftPct(BigDecimal bmlHandUpliftPct) { this.bmlHandUpliftPct = bmlHandUpliftPct; }
    public BigDecimal getDistLabourUpliftFactor2() { return distLabourUpliftFactor2; }
    public void setDistLabourUpliftFactor2(BigDecimal distLabourUpliftFactor2) { this.distLabourUpliftFactor2 = distLabourUpliftFactor2; }
    public BigDecimal getDistLabourUpliftFactor3() { return distLabourUpliftFactor3; }
    public void setDistLabourUpliftFactor3(BigDecimal distLabourUpliftFactor3) { this.distLabourUpliftFactor3 = distLabourUpliftFactor3; }
    public String getDistCompensationForcoreUplift() { return distCompensationForcoreUplift; }
    public void setDistCompensationForcoreUplift(String distCompensationForcoreUplift) { this.distCompensationForcoreUplift = distCompensationForcoreUplift; }
    public String getOnlineDlrAccessAllClaims() { return onlineDlrAccessAllClaims; }
    public void setOnlineDlrAccessAllClaims(String onlineDlrAccessAllClaims) { this.onlineDlrAccessAllClaims = onlineDlrAccessAllClaims; }
    public String getVatCodeInLegSystem() { return vatCodeInLegSystem; }
    public void setVatCodeInLegSystem(String vatCodeInLegSystem) { this.vatCodeInLegSystem = vatCodeInLegSystem; }
    public BigDecimal getVatPct() { return vatPct; }
    public void setVatPct(BigDecimal vatPct) { this.vatPct = vatPct; }
}

// ============================================================================
// JPA Repositories
// ============================================================================

@Repository
interface HSG71PFRepository extends JpaRepository<HSG71PF, String> {
}

@Repository
interface S3F003Repository extends JpaRepository<S3F003, String> {
}

// ============================================================================
// Service Layer
// ============================================================================

@Service
@Transactional
class HS1212Service {

    @Autowired
    private HSG71PFRepository hsg71pfRepository;

    @Autowired
    private S3F003Repository s3f003Repository;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void processClaimModification(String art, String g7100P, String g7101P, String g7102P, String g7103P, String g7104P) {
        LocalDate aktdat = LocalDate.now();
        String aktion = "";
        String auto = "J";
        String bereich = "1";
        String bereich1 = "1";
        BigDecimal clano6 = BigDecimal.ZERO;
        BigDecimal clano8 = BigDecimal.ZERO;
        String cmd = "";
        BigDecimal cmdLen = BigDecimal.ZERO;
        BigDecimal cnr = BigDecimal.ZERO;
        String cn = "";
        String g71050 = "";
        BigDecimal count = BigDecimal.ZERO;
        BigDecimal countZ = BigDecimal.ZERO;
        String c1 = "3";
        String c2 = "13";
        String c3 = "1";
        String c4 = "4";
        String c5 = "6";
        String c6 = "5";
        String epsType = "";
        boolean epsCheck = false;
        String epsInfo = "";
        String fgnr17 = "";
        String fn = "";
        BigDecimal fnn = BigDecimal.ZERO;
        String fabrikat = "1";
        String faino = "";
        String g7105A = "";
        LocalDate g7108F = null;
        LocalDate g7108A = null;
        BigDecimal g71040N = BigDecimal.ZERO;
        String g7117N = "";
        String g7306L = "";
        String g73060 = "";
        BigDecimal g7308N = BigDecimal.ZERO;
        BigDecimal g7310N = BigDecimal.ZERO;
        BigDecimal ganrn = BigDecimal.ZERO;
        String gpsart = "";
        BigDecimal gps120N = BigDecimal.ZERO;
        BigDecimal gps120 = BigDecimal.ZERO;
        String gutart = "";
        BigDecimal hersteller = BigDecimal.ONE;
        String hival = "99999999";
        String keyKzl = "";
        String aprKzl = "";
        String keySdpsr = "";
        String locale = "";
        BigDecimal locA = BigDecimal.ZERO;
        BigDecimal locB = BigDecimal.ZERO;
        String modules = "";
        BigDecimal matcos = BigDecimal.ZERO;
        String msgIdc = "";
        String ok = "";
        String pkz = "";
        BigDecimal pos = BigDecimal.ZERO;
        String reprint = "Y";
        String resultCode = "";
        String returnValue = "";
        String rwParm = "";
        String rueck = "";
        BigDecimal s3fCno = BigDecimal.ZERO;
        BigDecimal gclndd = BigDecimal.ZERO;
        BigDecimal s3fFno = BigDecimal.ZERO;
        BigDecimal pagedd = BigDecimal.ZERO;
        BigDecimal s3fDno = BigDecimal.ZERO;
        BigDecimal wcusdd = BigDecimal.ZERO;
        String scope = "";
        List<String> stat = new ArrayList<>();
        String storno = "";
        String sub060A = "";
        BigDecimal sub060N = BigDecimal.ZERO;
        String svt = "";
        String gps050 = "";
        BigDecimal svta = BigDecimal.ZERO;
        BigDecimal gps070 = BigDecimal.ZERO;
        BigDecimal tage = new BigDecimal(14);
        List<String> typ = new ArrayList<>();
        String tnr = "";
        String tnr0 = "000000000000000000";
        String tnr14 = "";
        String tnra = "";
        List<String> txt = new ArrayList<>();
        boolean updateSwat = true;
        LocalDate vgldat1 = null;
        LocalDate vgldat2 = null;
        String valDmc = "";
        String valScope = "";
        String valDealer = "";
        String wete = "1";
        BigDecimal x = BigDecimal.ZERO;
        BigDecimal zeile = BigDecimal.ZERO;
        BigDecimal gps030 = BigDecimal.ZERO;
        BigDecimal zwrec2 = BigDecimal.ZERO;
        BigDecimal zla = BigDecimal.ZERO;
        BigDecimal zlt = BigDecimal.ZERO;
        BigDecimal zlw2 = BigDecimal.ZERO;
        String umgebung = "";

        BigDecimal err = BigDecimal.ZERO;
        BigDecimal pag1 = BigDecimal.ONE;
        BigDecimal rec1 = BigDecimal.ONE;
        BigDecimal xxdat = new BigDecimal(aktdat.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String punkt1 = ".";
        String punkt2 = ".";
        String flagx4 = "";
        String flag04 = "";
        String sr102F = "";
        String ganr = "";
        String g7102D = "";
        BigDecimal g71090 = BigDecimal.ZERO;
        String g7109D = "";
        String g7111A = "";
        String g71060 = "";
        String g7106A = "";
        BigDecimal g7109A = BigDecimal.ZERO;
        BigDecimal g71100 = BigDecimal.ZERO;
        BigDecimal g7110A = BigDecimal.ZERO;

        typ.add("1 = Original Mont.  ");
        typ.add("2 = Orig. teil mont.");
        typ.add("4 = Orig. ab Lager  ");
        typ.add("6 = IRM Vertrag     ");
        typ.add("8 = SP. Maintenance ");
        typ.add("9 = EPC             ");

        Optional<HSG71PF> hsg71Record = hsg71pfRepository.findById(g7100P);
        if (!hsg71Record.isPresent()) {
            return;
        }

        HSG71PF claim = hsg71Record.get();
        g71050 = claim.getClaimNr();
        g71020 = claim.getRechDatum();
        g71090 = claim.getRepDatum();
        g7102D = formatDate(g71020);
        g7109D = formatDate(g71090.toString());

        boolean in62 = true;
        boolean in73 = false;

        if (claim.getBereich() != null && !claim.getBereich().isEmpty()) {
            bereich = claim.getBereich();
        } else if (claim.getProduktTyp().compareTo(new BigDecimal(2)) == 0) {
            bereich = "3";
        } else if (claim.getProduktTyp().compareTo(new BigDecimal(3)) == 0) {
            bereich = "6";
        } else {
            bereich = "1";
        }

        if (claim.getClaimNrSde() == null || claim.getClaimNrSde().isEmpty()) {
            in62 = false;
            in73 = false;
        }

        BigDecimal ganrKey = ganr.isEmpty() ? new BigDecimal(0) : new BigDecimal(ganr);
        Optional<S3F003> s3f003Record = s3f003Repository.findById(ganrKey.toString());
        if (!s3f003Record.isPresent()) {
            return;
        }

        S3F003 dealer = s3f003Record.get();
        BigDecimal dlrtcc = dealer.getStndLabourRtHr();
        BigDecimal dlupcc = dealer.getDistLabUplift();
        BigDecimal psd1cc = dealer.getEffPerStartDate1();
        BigDecimal ped1cc = dealer.getEffPerEndDate1();
        BigDecimal psd2cc = dealer.getEffPerStartDate2();
        BigDecimal ped2cc = dealer.getEffPerEndDate2();
        BigDecimal psd3cc = dealer.getEffPerStartDate3();
        BigDecimal ped3cc = dealer.getEffPerEndDate3();
        BigDecimal dlr2cc = dealer.getLabourRtHr2();
        BigDecimal dlu2cc = dealer.getDistLabourUpliftFactor2();
        BigDecimal dlr3cc = dealer.getLabourRtHr3();
        BigDecimal dlu3cc = dealer.getDistLabourUpliftFactor3();

        if (g71090.compareTo(psd1cc) >= 0 && g71090.compareTo(ped1cc) <= 0) {
            matcos = dlrtcc.multiply(dlupcc);
        } else if (g71090.compareTo(psd2cc) >= 0 && g71090.compareTo(ped2cc) <= 0) {
            matcos = dlr2cc.multiply(dlu2cc);
        } else if (g71090.compareTo(psd3cc) >= 0 && g71090.compareTo(ped3cc) <= 0) {
            matcos = dlr3cc.multiply(dlu3cc);
        }

        g7111A = claim.getProduktTyp().toString();
        g7106A = claim.getChassisNr();
        g7109A = claim.getRepDatum();
        g7110A = claim.getKmStand();
        g7105A = claim.getClaimNr();

        try {
            g7108A = LocalDate.parse(claim.getZulDatum().toString(), ISO_DATE_FORMATTER);
        } catch (Exception e) {
            g7108A = LocalDate.of(1, 1, 1);
        }

        boolean in50 = false;
        boolean in51 = false;
        boolean in52 = false;
        boolean in53 = false;
        boolean in54 = false;
        boolean in56 = false;

        if ("5".equals(art)) {
            in62 = false;
        } else if ("6".equals(art)) {
            return;
        } else if ("7".equals(art)) {
            art = "";
        } else if ("8".equals(art)) {
            art = "";
        }

        if (claim.getStatusCodeSde().compareTo(new BigDecimal(20)) < 0) {
            checkClaimHeader(claim, in50, in51, in52, in53, in54);
        }

        g7117N = claim.getStatusCodeSde().toString();
        String statxt = "";

        if (claim.getClaimNrSde() != null && !claim.getClaimNrSde().isEmpty() && claim.getClaimNrSde().compareTo("00000000") > 0) {
            String g71160A = claim.getClaimNrSde();
        }

        if (claim.getClaimNrSde() != null && claim.getClaimNrSde().equals("00000000")) {
            return;
        }
    }

    private void checkClaimHeader(HSG71PF claim, boolean in50, boolean in51, boolean in52, boolean in53, boolean in54) {
        if (claim.getProduktTyp().compareTo(BigDecimal.ONE) < 0 || claim.getProduktTyp().compareTo(new BigDecimal(5)) > 0) {
            in50 = true;
        }

        if (claim.getChassisNr() != null && !claim.getChassisNr().isEmpty()) {
            in51 = false;
        } else {
            in51 = true;
        }

        if (claim.getRepDatum() == null || claim.getRepDatum().compareTo(BigDecimal.ZERO) <= 0) {
            in52 = true;
        }

        if (claim.getKmStand().compareTo(BigDecimal.ZERO) <= 0 && claim.getChassisNr() != null && !claim.getChassisNr().isEmpty()) {
            in53 = true;
        }

        if (claim.getKmStand().compareTo(BigDecimal.ZERO) != 0 && (claim.getChassisNr() == null || claim.getChassisNr().isEmpty())) {
            in54 = true;
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() != 8) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(isoDate, ISO_DATE_FORMATTER);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return "";
        }
    }

    private boolean checkClaim(String g7324A, String g7314A, String g7307A, String g7308A, String g7310A, 
                                String g7312A, String g7313A, String g7332A, String g7333A, 
                                LocalDate g7325A, BigDecimal g7326A, BigDecimal g71090, BigDecimal g71100) {
        if ("9".equals(g7324A) && g7314A != null && !g7314A.isEmpty()) {
            return false;
        }

        if (!"1".equals(g7324A) && !"2".equals(g7324A) && !"4".equals(g7324A) && 
            !"6".equals(g7324A) && !"8".equals(g7324A) && !"9".equals(g7324A)) {
            return false;
        }

        if (g7325A != null && g7325A.getYear() > 1) {
            if (g7325A.isAfter(LocalDate.now())) {
                return false;
            }
        }

        if ("2".equals(g7324A)) {
            if (g7325A == null || g7326A.compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
        }

        if (g7325A != null && g7326A.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal g73250 = new BigDecimal(g7325A.format(ISO_DATE_FORMATTER));
            if (g73250.compareTo(g71090) > 0 || g7326A.compareTo(g71100) > 0) {
                return false;
            }
        }

        if ((g7312A == null || g7312A.isEmpty()) && (g7313A == null || g7313A.isEmpty()) && 
            (g7332A == null || g7332A.isEmpty()) && (g7333A == null || g7333A.isEmpty())) {
            return false;
        }

        if (g7310A == null || g7310A.isEmpty()) {
            return false;
        }

        if (!"W2".equals(g7314A) && !"W3".equals(g7314A) && !"6".equals(g7324A)) {
            if (g7307A == null || g7307A.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private BigDecimal getClaimAge(String g7324A, BigDecimal g71090, BigDecimal g71080, LocalDate g7325A) {
        try {
            LocalDate repairDate = LocalDate.parse(g71090.toString(), ISO_DATE_FORMATTER);
            LocalDate compareDate;
            
            if ("1".equals(g7324A)) {
                compareDate = LocalDate.parse(g71080.toString(), ISO_DATE_FORMATTER);
            } else {
                compareDate = g7325A;
            }
            
            long monthsBetween = ChronoUnit.MONTHS.between(compareDate, repairDate);
            return monthsBetween > 99 ? new BigDecimal(99) : new BigDecimal(monthsBetween);
        } catch (Exception e) {
            return new BigDecimal(100);
        }
    }

    private void loadClaim(String g71000, String g71050, String sub060) {
    }

    private void getDescription(String g7307A, String g7308A, String g7310A, String g7314A) {
    }

    private void getDates(String g71000, String g71010, String g71020, String g71030, String g71190) {
    }

    private String getModules(String g71000, String g71030, String g71200, String bookDate) {
        return "";
    }

    private void readPositions(String dealerId, String claimNo) {
    }
}

class ClaimDTO {
    private String art;
    private String g7100P;
    private String g7101P;
    private String g7102P;
    private String g7103P;
    private String g7104P;

    public String getArt() { return art; }
    public void setArt(String art) { this.art = art; }
    public String getG7100P() { return g7100P; }
    public void setG7100P(String g7100P) { this.g7100P = g7100P; }
    public String getG7101P() { return g7101P; }
    public void setG7101P(String g7101P) { this.g7101P = g7101P; }
    public String getG7102P() { return g7102P; }
    public void setG7102P(String g7102P) { this.g7102P = g7102P; }
    public String getG7103P() { return g7103P; }
    public void setG7103P(String g7103P) { this.g7103P = g7103P; }
    public String getG7104P() { return g7104P; }
    public void setG7104P(String g7104P) { this.g7104P = g7104P; }
}