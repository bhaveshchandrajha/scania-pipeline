package com.scania.warranty.claim.management;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity representing HSG71PF - Warranty Claim Header
 */
@Entity
@Table(name = "HSG71PF")
class Hsg71pf {
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

    // Getters and setters omitted for brevity
}

/**
 * Entity representing S3F003 - Distributor Warranty Customer
 */
@Entity
@Table(name = "S3F003")
class S3f003 {
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
    private BigDecimal purchSplitVor;

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

    // Getters and setters omitted for brevity
}

/**
 * DTO for claim processing parameters
 */
class ClaimParameters {
    private String art;
    private String g7100p;
    private String g7101p;
    private String g7102p;
    private String g7103p;
    private String g7104p;
    private String bereich;
    private String wete;
    private String splitt;
    private String pkz;
    private String usr;
    private LocalDate aktdat;
    private BigDecimal key0011;
    private String locale;
    private boolean in40;
    private boolean in60;
    private boolean in62;
    private boolean in73;
    private boolean in64;
    private boolean updateSwat;

    // Getters and setters
    public String getArt() { return art; }
    public void setArt(String art) { this.art = art; }
    public String getG7100p() { return g7100p; }
    public void setG7100p(String g7100p) { this.g7100p = g7100p; }
    public String getG7101p() { return g7101p; }
    public void setG7101p(String g7101p) { this.g7101p = g7101p; }
    public String getG7102p() { return g7102p; }
    public void setG7102p(String g7102p) { this.g7102p = g7102p; }
    public String getG7103p() { return g7103p; }
    public void setG7103p(String g7103p) { this.g7103p = g7103p; }
    public String getG7104p() { return g7104p; }
    public void setG7104p(String g7104p) { this.g7104p = g7104p; }
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    public String getWete() { return wete; }
    public void setWete(String wete) { this.wete = wete; }
    public String getSplitt() { return splitt; }
    public void setSplitt(String splitt) { this.splitt = splitt; }
    public String getPkz() { return pkz; }
    public void setPkz(String pkz) { this.pkz = pkz; }
    public String getUsr() { return usr; }
    public void setUsr(String usr) { this.usr = usr; }
    public LocalDate getAktdat() { return aktdat; }
    public void setAktdat(LocalDate aktdat) { this.aktdat = aktdat; }
    public BigDecimal getKey0011() { return key0011; }
    public void setKey0011(BigDecimal key0011) { this.key0011 = key0011; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public boolean isIn40() { return in40; }
    public void setIn40(boolean in40) { this.in40 = in40; }
    public boolean isIn60() { return in60; }
    public void setIn60(boolean in60) { this.in60 = in60; }
    public boolean isIn62() { return in62; }
    public void setIn62(boolean in62) { this.in62 = in62; }
    public boolean isIn73() { return in73; }
    public void setIn73(boolean in73) { this.in73 = in73; }
    public boolean isIn64() { return in64; }
    public void setIn64(boolean in64) { this.in64 = in64; }
    public boolean isUpdateSwat() { return updateSwat; }
    public void setUpdateSwat(boolean updateSwat) { this.updateSwat = updateSwat; }
}

/**
 * DTO for claim detail data
 */
class ClaimDetail {
    private String g7305a;
    private String g7306a;
    private String g73065;
    private String g7307a;
    private String g7308a;
    private String g7310a;
    private String g7311a;
    private String g7312a;
    private String g7313a;
    private String g7314a;
    private BigDecimal g7318a;
    private BigDecimal g7319a;
    private BigDecimal g7320a;
    private String g7324a;
    private BigDecimal g7325a;
    private BigDecimal g7326a;
    private BigDecimal g7327a;
    private String g7328a;
    private String g7332a;
    private String g7333a;
    private String g7335a;
    private String aC1;
    private String aC2;
    private String aC3;
    private String aC4;
    private String aC5;
    private String aC6;
    private String aEps;
    private String dC1;
    private String dC2;
    private String dC3;
    private String dC4;
    private String dC5;
    private String dC6;
    private String dEps;
    private String teil;
    private String hgrp;
    private String scod;
    private String steuer;
    private BigDecimal substa;
    private String subgut;

    // Getters and setters
    public String getG7305a() { return g7305a; }
    public void setG7305a(String g7305a) { this.g7305a = g7305a; }
    public String getG7306a() { return g7306a; }
    public void setG7306a(String g7306a) { this.g7306a = g7306a; }
    public String getG73065() { return g73065; }
    public void setG73065(String g73065) { this.g73065 = g73065; }
    public String getG7307a() { return g7307a; }
    public void setG7307a(String g7307a) { this.g7307a = g7307a; }
    public String getG7308a() { return g7308a; }
    public void setG7308a(String g7308a) { this.g7308a = g7308a; }
    public String getG7310a() { return g7310a; }
    public void setG7310a(String g7310a) { this.g7310a = g7310a; }
    public String getG7311a() { return g7311a; }
    public void setG7311a(String g7311a) { this.g7311a = g7311a; }
    public String getG7312a() { return g7312a; }
    public void setG7312a(String g7312a) { this.g7312a = g7312a; }
    public String getG7313a() { return g7313a; }
    public void setG7313a(String g7313a) { this.g7313a = g7313a; }
    public String getG7314a() { return g7314a; }
    public void setG7314a(String g7314a) { this.g7314a = g7314a; }
    public BigDecimal getG7318a() { return g7318a; }
    public void setG7318a(BigDecimal g7318a) { this.g7318a = g7318a; }
    public BigDecimal getG7319a() { return g7319a; }
    public void setG7319a(BigDecimal g7319a) { this.g7319a = g7319a; }
    public BigDecimal getG7320a() { return g7320a; }
    public void setG7320a(BigDecimal g7320a) { this.g7320a = g7320a; }
    public String getG7324a() { return g7324a; }
    public void setG7324a(String g7324a) { this.g7324a = g7324a; }
    public BigDecimal getG7325a() { return g7325a; }
    public void setG7325a(BigDecimal g7325a) { this.g7325a = g7325a; }
    public BigDecimal getG7326a() { return g7326a; }
    public void setG7326a(BigDecimal g7326a) { this.g7326a = g7326a; }
    public BigDecimal getG7327a() { return g7327a; }
    public void setG7327a(BigDecimal g7327a) { this.g7327a = g7327a; }
    public String getG7328a() { return g7328a; }
    public void setG7328a(String g7328a) { this.g7328a = g7328a; }
    public String getG7332a() { return g7332a; }
    public void setG7332a(String g7332a) { this.g7332a = g7332a; }
    public String getG7333a() { return g7333a; }
    public void setG7333a(String g7333a) { this.g7333a = g7333a; }
    public String getG7335a() { return g7335a; }
    public void setG7335a(String g7335a) { this.g7335a = g7335a; }
    public String getAC1() { return aC1; }
    public void setAC1(String aC1) { this.aC1 = aC1; }
    public String getAC2() { return aC2; }
    public void setAC2(String aC2) { this.aC2 = aC2; }
    public String getAC3() { return aC3; }
    public void setAC3(String aC3) { this.aC3 = aC3; }
    public String getAC4() { return aC4; }
    public void setAC4(String aC4) { this.aC4 = aC4; }
    public String getAC5() { return aC5; }
    public void setAC5(String aC5) { this.aC5 = aC5; }
    public String getAC6() { return aC6; }
    public void setAC6(String aC6) { this.aC6 = aC6; }
    public String getAEps() { return aEps; }
    public void setAEps(String aEps) { this.aEps = aEps; }
    public String getDC1() { return dC1; }
    public void setDC1(String dC1) { this.dC1 = dC1; }
    public String getDC2() { return dC2; }
    public void setDC2(String dC2) { this.dC2 = dC2; }
    public String getDC3() { return dC3; }
    public void setDC3(String dC3) { this.dC3 = dC3; }
    public String getDC4() { return dC4; }
    public void setDC4(String dC4) { this.dC4 = dC4; }
    public String getDC5() { return dC5; }
    public void setDC5(String dC5) { this.dC5 = dC5; }
    public String getDC6() { return dC6; }
    public void setDC6(String dC6) { this.dC6 = dC6; }
    public String getDEps() { return dEps; }
    public void setDEps(String dEps) { this.dEps = dEps; }
    public String getTeil() { return teil; }
    public void setTeil(String teil) { this.teil = teil; }
    public String getHgrp() { return hgrp; }
    public void setHgrp(String hgrp) { this.hgrp = hgrp; }
    public String getScod() { return scod; }
    public void setScod(String scod) { this.scod = scod; }
    public String getSteuer() { return steuer; }
    public void setSteuer(String steuer) { this.steuer = steuer; }
    public BigDecimal getSubsta() { return substa; }
    public void setSubsta(BigDecimal substa) { this.substa = substa; }
    public String getSubgut() { return subgut; }
    public void setSubgut(String subgut) { this.subgut = subgut; }
}

/**
 * Service for warranty claim management (HS1212)
 * Migrated from RPG program HS1212
 */
@Service
@Transactional
public class WarrantyClaimManagementService {

    private static final String[] CLAIM_TYPES = {
        "1 = Original Mont.  ",
        "2 = Orig. teil mont.",
        "4 = Orig. ab Lager  ",
        "6 = IRM Vertrag     ",
        "8 = SP. Maintenance ",
        "9 = EPC             "
    };

    private static final int DEFAULT_DAYS_LIMIT = 14;

    /**
     * Main entry point for claim processing
     */
    public void processClaimManagement(ClaimParameters params) {
        // Initialize parameters
        params.setAktdat(LocalDate.now());
        params.setBereich("1");
        params.setWete("1");
        params.setSplitt("04");
        params.setUpdateSwat(true);

        // Retrieve home dealer
        String pkz = retrieveHomeDealerPkz(params.getPkz());
        if (pkz == null) {
            return;
        }
        params.setPkz(pkz);

        // Retrieve locale
        String locale = getCwpInfo("", "locale");
        params.setLocale(locale);

        // Determine key0011 from GANR or FIS020
        BigDecimal key0011 = determineKey0011(params);
        params.setKey0011(key0011);

        // Load claim header data
        Hsg71pf claimHeader = loadClaimHeader(params);
        if (claimHeader == null) {
            return;
        }

        // Determine bereich (area)
        String bereich = determineBereich(claimHeader);
        params.setBereich(bereich);

        // Check if claim is editable
        boolean isEditable = checkClaimEditability(claimHeader);
        params.setIn62(isEditable);

        // Retrieve partner information
        S3f003 partner = retrievePartnerInfo(key0011);
        if (partner == null) {
            return;
        }

        // Calculate material cost
        BigDecimal matcos = calculateMaterialCost(claimHeader, partner);

        // Read positions
        List<Integer> positions = readPositions(params.getG7100p(), claimHeader.getClaimNr());

        // Validate header data
        ValidationResult validation = validateClaimHeader(claimHeader);
        if (!validation.isValid()) {
            handleValidationErrors(validation);
            return;
        }

        // Process based on action type
        processClaimAction(params, claimHeader, matcos);
    }

    /**
     * Load claim header from database
     */
    private Hsg71pf loadClaimHeader(ClaimParameters params) {
        // Implementation would query HSG71PF using composite key
        // (G7100P, G7101P, G7102P, G7103P, G7104P)
        return null; // Placeholder
    }

    /**
     * Determine bereich (area) based on claim data
     */
    private String determineBereich(Hsg71pf claim) {
        if (claim.getBereich() != null && !claim.getBereich().trim().isEmpty()) {
            return claim.getBereich();
        }
        if (claim.getProduktTyp() != null) {
            int produktTyp = claim.getProduktTyp().intValue();
            if (produktTyp == 2) {
                return "3"; // Bus
            } else if (produktTyp == 3) {
                return "6"; // Engine
            }
        }
        return "1"; // Default: Truck
    }

    /**
     * Check if claim is editable
     */
    private boolean checkClaimEditability(Hsg71pf claim) {
        if (claim.getClaimNrSde() == null || claim.getClaimNrSde().trim().isEmpty()) {
            return false;
        }
        // Additional logic to check status and positions
        return true;
    }

    /**
     * Retrieve partner information
     */
    private S3f003 retrievePartnerInfo(BigDecimal ganrn) {
        // Implementation would query S3F003 using GANRN
        return null; // Placeholder
    }

    /**
     * Calculate material cost based on repair date and labor rates
     */
    private BigDecimal calculateMaterialCost(Hsg71pf claim, S3f003 partner) {
        BigDecimal repairDate = claim.getRepDatum();
        BigDecimal matcos = BigDecimal.ZERO;

        if (repairDate.compareTo(partner.getEffPerStartDate1()) >= 0 &&
            repairDate.compareTo(partner.getEffPerEndDate1()) <= 0) {
            matcos = partner.getStndLabourRtHr().multiply(partner.getDistLabUplift());
        } else if (repairDate.compareTo(partner.getEffPerStartDate2()) >= 0 &&
                   repairDate.compareTo(partner.getEffPerEndDate2()) <= 0) {
            matcos = partner.getLabourRtHr2().multiply(partner.getDistLabourUpliftFactor2());
        } else if (repairDate.compareTo(partner.getEffPerStartDate3()) >= 0 &&
                   repairDate.compareTo(partner.getEffPerEndDate3()) <= 0) {
            matcos = partner.getLabourRtHr3().multiply(partner.getDistLabourUpliftFactor3());
        }

        return matcos;
    }

    /**
     * Read positions for claim
     */
    private List<Integer> readPositions(String dealerId, String claimNo) {
        // SQL query to retrieve distinct position numbers
        // SELECT DISTINCT AHW075 FROM HSAHWPF/HSAHPPF WHERE conditions
        return new ArrayList<>(); // Placeholder
    }

    /**
     * Validate claim header data
     */
    private ValidationResult validateClaimHeader(Hsg71pf claim) {
        ValidationResult result = new ValidationResult();

        // Product type validation
        if (claim.getProduktTyp() == null ||
            claim.getProduktTyp().intValue() < 1 ||
            claim.getProduktTyp().intValue() > 5) {
            result.addError("in50", "Invalid product type");
        }

        // Chassis number validation
        if (claim.getChassisNr() != null && !claim.getChassisNr().trim().isEmpty()) {
            // Validate chassis number exists in master file
            boolean chassisValid = validateChassisNumber(claim.getChassisNr(), claim.getProduktTyp());
            if (!chassisValid) {
                result.addError("in51", "Invalid chassis number");
            }
        }

        // Repair date validation
        if (claim.getRepDatum() != null) {
            boolean dateValid = validateRepairDate(claim.getRepDatum());
            if (!dateValid) {
                result.addError("in52", "Invalid repair date");
            }
        }

        // Mileage validation
        if (claim.getChassisNr() != null && !claim.getChassisNr().trim().isEmpty()) {
            if (claim.getKmStand() == null || claim.getKmStand().compareTo(BigDecimal.ZERO) <= 0) {
                result.addError("in53", "Mileage must be greater than 0");
            }
        }

        // Registration date validation
        if (claim.getKmStand() != null && claim.getKmStand().compareTo(new BigDecimal("2000")) > 0) {
            if (claim.getZulDatum() == null || claim.getZulDatum().compareTo(BigDecimal.ZERO) == 0) {
                result.addError("in49", "Registration date required when mileage > 2000");
            }
        }

        return result;
    }

    /**
     * Validate chassis number
     */
    private boolean validateChassisNumber(String chassisNr, BigDecimal produktTyp) {
        // Implementation would query PIPPMST
        return true; // Placeholder
    }

    /**
     * Validate repair date
     */
    private boolean validateRepairDate(BigDecimal repDatum) {
        try {
            String dateStr = repDatum.toString();
            if (dateStr.length() != 8) {
                return false;
            }
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));

            if (year < 1980 || month < 1 || month > 12 || day < 1 || day > 31) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Process claim action based on type
     */
    private void processClaimAction(ClaimParameters params, Hsg71pf claim, BigDecimal matcos) {
        String art = params.getArt();

        if ("5".equals(art)) {
            params.setIn60(true);
            return;
        } else if ("6".equals(art)) {
            params.setIn40(true);
            // Check header and display
            return;
        } else if ("7".equals(art) || "8".equals(art)) {
            // Check header and display
            return;
        }

        // Check if status < 20 and validate header
        if (claim.getStatusCodeSde() != null && claim.getStatusCodeSde().intValue() < 20) {
            ValidationResult validation = validateClaimHeader(claim);
            if (!validation.isValid()) {
                handleValidationErrors(validation);
                return;
            }
        }

        // Determine status text
        String statusText = determineStatusText(claim.getStatusCodeSde());

        // Process minimum claim
        if ("00000000".equals(claim.getClaimNrSde())) {
            // Display header only
            return;
        }

        // Main processing loop
        processClaimDetails(params, claim);
    }

    /**
     * Determine status text
     */
    private String determineStatusText(BigDecimal statusCode) {
        // Implementation would query HSGSCPF for status description
        return ""; // Placeholder
    }

    /**
     * Process claim details
     */
    private void processClaimDetails(ClaimParameters params, Hsg71pf claim) {
        // Load claim details (failures)
        List<ClaimDetail> details = loadClaimDetails(params);

        // Display subfile and process user selections
        for (ClaimDetail detail : details) {
            processDetailAction(params, claim, detail);
        }
    }

    /**
     * Load claim details
     */
    private List<ClaimDetail> loadClaimDetails(ClaimParameters params) {
        // Implementation would query HSG73PF
        return new ArrayList<>(); // Placeholder
    }

    /**
     * Process detail action
     */
    private void processDetailAction(ClaimParameters params, Hsg71pf claim, ClaimDetail detail) {
        // Process based on user selection (2=Change, 4=Delete, 5=Display, etc.)
    }

    /**
     * Check claim validity
     */
    private boolean checkClaim(ClaimDetail detail, Hsg71pf claim) {
        // EPC must have blank control code
        if ("9".equals(detail.getG7324a()) && detail.getG7314a() != null && !detail.getG7314a().trim().isEmpty()) {
            return false;
        }

        // Claim type validation
        if (!isValidClaimType(detail.getG7324a())) {
            return false;
        }

        // Previous repair date validation
        if (detail.getG7325a() != null && detail.getG7325a().compareTo(BigDecimal.ZERO) > 0) {
            if (!isValidDate(detail.getG7325a())) {
                return false;
            }
        }

        // Claim type 2 requires repair date and mileage
        if ("2".equals(detail.getG7324a())) {
            if (detail.getG7325a() == null || detail.getG7325a().compareTo(BigDecimal.ZERO) == 0 ||
                detail.getG7326a() == null || detail.getG7326a().compareTo(BigDecimal.ZERO) == 0) {
                return false;
            }
        }

        // Previous repair date/mileage must be less than current
        if (detail.getG7325a() != null && detail.getG7325a().compareTo(BigDecimal.ZERO) > 0 &&
            detail.getG7326a() != null && detail.getG7326a().compareTo(BigDecimal.ZERO) > 0) {
            if (detail.getG7325a().compareTo(claim.getRepDatum()) > 0 ||
                detail.getG7326a().compareTo(claim.getKmStand()) > 0) {
                return false;
            }
        }

        // Description required
        if (isBlank(detail.getG7312a()) && isBlank(detail.getG7313a()) &&
            isBlank(detail.getG7332a()) && isBlank(detail.getG7333a())) {
            return false;
        }

        // Control code validation
        if (detail.getG7314a() != null && !detail.getG7314a().trim().isEmpty()) {
            if (!isValidControlCode(detail.getG7314a())) {
                return false;
            }
        }

        // Validate control code matches order
        if (!validateControlCodeForOrder(detail, claim)) {
            return false;
        }

        // Set compensation percentages
        setCompensationPercentages(detail);

        return true;
    }

    /**
     * Check if claim type is valid
     */
    private boolean isValidClaimType(String claimType) {
        return "1".equals(claimType) || "2".equals(claimType) || "4".equals(claimType) ||
               "6".equals(claimType) || "8".equals(claimType) || "9".equals(claimType);
    }

    /**
     * Check if date is valid
     */
    private boolean isValidDate(BigDecimal date) {
        try {
            String dateStr = date.toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if control code is valid
     */
    private boolean isValidControlCode(String controlCode) {
        // Implementation would query S3F085
        return true; // Placeholder
    }

    /**
     * Validate control code for order
     */
    private boolean validateControlCodeForOrder(ClaimDetail detail, Hsg71pf claim) {
        // Implementation would check if control code is valid for the order
        return true; // Placeholder
    }

    /**
     * Set compensation percentages
     */
    private void setCompensationPercentages(ClaimDetail detail) {
        detail.setG7318a(new BigDecimal("100"));
        detail.setG7319a(new BigDecimal("100"));
        detail.setG7320a(new BigDecimal("100"));

        // Adjust based on control code and EPS
        if (detail.getAEps() != null && !detail.getAEps().trim().isEmpty()) {
            String epsType = getEpsType(detail.getAEps(), detail.getG7307a());
            BigDecimal epsComp = getEpsComp(detail.getAEps(), detail.getG7307a());

            if ("1".equals(epsType)) {
                detail.setG7318a(epsComp);
                detail.setG7319a(epsComp);
                detail.setG7320a(epsComp);
            } else if ("2".equals(epsType)) {
                detail.setG7318a(epsComp);
                detail.setG7319a(BigDecimal.ZERO);
                detail.setG7320a(BigDecimal.ZERO);
            } else if ("3".equals(epsType)) {
                detail.setG7318a(BigDecimal.ZERO);
                detail.setG7319a(epsComp);
                detail.setG7320a(epsComp);
            }
        }
    }

    /**
     * Get EPS type
     */
    private String getEpsType(String epsName, String chassisNo) {
        // Implementation would call external procedure
        return "1"; // Placeholder
    }

    /**
     * Get EPS compensation
     */
    private BigDecimal getEpsComp(String epsName, String chassisNo) {
        // Implementation would call external procedure
        return new BigDecimal("100"); // Placeholder
    }

    /**
     * Get claim age in months
     */
    private int getClaimAge(Hsg71pf claim, ClaimDetail detail) {
        try {
            LocalDate repairDate = parseDate(claim.getRepDatum());
            LocalDate compareDate;

            if ("1".equals(detail.getG7324a())) {
                compareDate = parseDate(claim.getZulDatum());
            } else {
                compareDate = parseDate(detail.getG7325a());
            }

            long months = ChronoUnit.MONTHS.between(compareDate, repairDate);
            return months > 99 ? 99 : (int) months;
        } catch (Exception e) {
            return 100;
        }
    }

    /**
     * Parse date from BigDecimal
     */
    private LocalDate parseDate(BigDecimal date) {
        if (date == null) {
            return LocalDate.now();
        }
        String dateStr = date.toString();
        int year = Integer.parseInt(dateStr.substring(0, 4));
        int month = Integer.parseInt(dateStr.substring(4, 6));
        int day = Integer.parseInt(dateStr.substring(6, 8));
        return LocalDate.of(year, month, day);
    }

    /**
     * Load claim from database
     */
    private void loadClaim(ClaimDetail detail) {
        // Implementation would query HSG73PF and populate detail
    }

    /**
     * Get description for part
     */
    private void getDescription(ClaimDetail detail) {
        // Implementation would query various files for descriptions
        detail.setTeil(getPartDesc("", detail.getG7307a()));
        detail.setHgrp(getMainGroupDesc(detail.getG7308a()));
        detail.setScod(getDamageCodeDesc(detail.getG7310a()));
        detail.setSteuer(getControlCodeDesc(detail.getG7314a()));
    }

    /**
     * Get part description
     */
    private String getPartDesc(String franCode, String partNo) {
        // Implementation would query ITLSMF3
        return ""; // Placeholder
    }

    /**
     * Get main group description
     */
    private String getMainGroupDesc(String mainGroup) {
        // Implementation would query S3F018
        return ""; // Placeholder
    }

    /**
     * Get damage code description
     */
    private String getDamageCodeDesc(String damageCode) {
        // Implementation would query S3F019
        return ""; // Placeholder
    }

    /**
     * Get control code description
     */
    private String getControlCodeDesc(String controlCode) {
        // Implementation would query S3F085
        return ""; // Placeholder
    }

    /**
     * Get dates (open date and complain date)
     */
    private Map<String, String> getDates(ClaimParameters params, Hsg71pf claim) {
        // Implementation would query HSAHKPF
        Map<String, String> dates = new HashMap<>();
        dates.put("bookDate", "");
        dates.put("openDate", "");
        return dates;
    }

    /**
     * Get modules
     */
    private String getModules(ClaimParameters params, Hsg71pf claim) {
        // Implementation would query HSFTMF
        return ""; // Placeholder
    }

    /**
     * Retrieve home dealer PKZ
     */
    private String retrieveHomeDealerPkz(String pkzx) {
        // Implementation would query HSBTSLF1
        return ""; // Placeholder
    }

    /**
     * Get CWP info
     */
    private String getCwpInfo(String dealerId, String index) {
        // Implementation would call external procedure
        return ""; // Placeholder
    }

    /**
     * Determine KEY0011
     */
    private BigDecimal determineKey0011(ClaimParameters params) {
        // Implementation would determine from GANR or FIS020
        return BigDecimal.ZERO; // Placeholder
    }

    /**
     * Handle validation errors
     */
    private void handleValidationErrors(ValidationResult result) {
        // Implementation would display errors to user
    }

    /**
     * Check if string is blank
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validation result holder
     */
    private static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();

        public void addError(String code, String message) {
            errors.put(code, message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}