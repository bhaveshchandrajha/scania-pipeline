package com.example.warranty.service;

import com.example.warranty.entity.*;
import com.example.warranty.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementing subroutine n404 (SB10N) business logic.
 * This service handles warranty claim subfile processing, including:
 * - Subfile initialization and clearing
 * - Mark field management for selection processing
 * - Claim filtering and display logic
 * - Status color coding (red/yellow/blue indicators)
 * - Integration with invoice, parts, and labor data
 */
@Service
@Transactional
public class ClaimSubfileService {

    @Autowired
    private HSG71LF2Repository hsg71Repository;

    @Autowired
    private HSG73PFRepository hsg73Repository;

    @Autowired
    private HSAHKLF3Repository hsahkRepository;

    @Autowired
    private HSAHWPFRepository hsahwRepository;

    @Autowired
    private HSFLALF1Repository hsflaRepository;

    @Autowired
    private HSG70FRepository hsg70Repository;

    @Autowired
    private ClaimStatusService claimStatusService;

    @Autowired
    private ClaimFilterService claimFilterService;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Subroutine SB10N - Initialize subfile processing
     * Clears counters and mark fields
     */
    public void initializeSubfile(SubfileContext context) {
        context.setZl4(0);
        context.setSub15x("");
        context.setIndicator50(false);
        context.setIndicator51(false);
        context.setIndicator52(false);
        context.setIndicator53(false);
        context.setIndicator54(false);
        context.setIndicator55(false);
        context.setIndicator56(false);
        context.setIndicator57(false);
        context.setIndicator58(false);

        if (context.getMark12() == null || context.getMark12().trim().isEmpty()) {
            context.setMark12(context.getMark11());
            context.setMark11(" ");
        }
    }

    /**
     * Subroutine MARK - Process mark field selection
     * Converts selection format from '1 ' to ' 1'
     */
    public void processMarkSelection(SubfileContext context) {
        context.setSub01x(context.getSub010());

        if (context.getMark12() == null || context.getMark12().trim().isEmpty()) {
            context.setMark12(context.getMark11());
            context.setMark11(" ");
        }

        if (context.getMark22() == null || context.getMark22().trim().isEmpty()) {
            context.setMark22(context.getMark21());
            context.setMark21(" ");
        }
    }

    /**
     * Subroutine SB100 - Main subfile processing
     * Loads claim records into subfile with filtering and color coding
     */
    public SubfileResult buildSubfile(SubfileContext context) {
        SubfileResult result = new SubfileResult();
        
        context.setIndicator90(false);
        context.setIndicator71(false);
        context.setIndicator99(true);
        context.setZl1(0);
        context.setZl2(0);

        List<SubfileRecord> records = new ArrayList<>();

        // Determine sort order (ascending/descending)
        boolean ascending = context.isIndicator63();
        String pkz = context.getPkz();

        List<HSG71LF2Entity> claims;
        if (ascending) {
            claims = hsg71Repository.findByPakzOrderByClaimNrAsc(pkz);
        } else {
            claims = hsg71Repository.findByPakzOrderByClaimNrDesc(pkz);
        }

        for (HSG71LF2Entity claim : claims) {
            if (context.getZl1() >= 9999) {
                break;
            }

            // Apply claim age filter
            if (context.getFiltag() != 0 && claim.getStatusCodeSde() != 99) {
                if (claim.getRepDatum() != null && claim.getRepDatum() > 0) {
                    LocalDate repairDate = parseIsoDate(String.valueOf(claim.getRepDatum()));
                    LocalDate currentDate = LocalDate.now();
                    long daysDiff = ChronoUnit.DAYS.between(repairDate, currentDate);
                    if (daysDiff > context.getFiltag()) {
                        claim.setStatusCodeSde(99);
                    }
                }
            }

            // Apply claim type filter
            if (context.getFilart() != null && !context.getFilart().trim().isEmpty() 
                && claim.getStatusCodeSde() != 99) {
                applyClaimTypeFilter(claim, context);
            }

            // Check for open claims
            boolean isOpen = false;
            if ("J".equals(context.getFiloff())) {
                if (claim.getStatusCodeSde() < 20 && claim.getStatusCodeSde() != 5) {
                    isOpen = true;
                }

                List<HSG73PFEntity> failures = hsg73Repository.findByPakzAndClaimNr(
                    claim.getPakz(), claim.getClaimNr());
                
                if (failures.isEmpty()) {
                    isOpen = true;
                } else {
                    for (HSG73PFEntity failure : failures) {
                        if (failure.getStatusCode() == 0) {
                            isOpen = true;
                            break;
                        }
                    }
                }

                if (!isOpen) {
                    claim.setStatusCodeSde(99);
                }
            }

            // Skip if filtered out
            if (claim.getStatusCodeSde() == 99) {
                continue;
            }

            // Build subfile record
            SubfileRecord record = new SubfileRecord();
            record.setMark1("");
            record.setMark2("");
            record.setSub000(claim.getPakz());
            record.setSub010(claim.getRechNr());
            record.setSub020(formatDate(claim.getRechDatum()));
            record.setSub030(claim.getAuftragsNr());
            record.setSub040(claim.getWete());
            record.setSub050(claim.getClaimNr());
            record.setSub060(claim.getChassisNr());
            record.setSub070(claim.getKennzeichen());
            record.setSub090(formatDate(String.valueOf(claim.getRepDatum())));
            record.setSub100(claim.getKmStand());
            record.setSub140(claim.getKdNr());
            record.setSub150(claim.getKdName());
            record.setSub160(claim.getClaimNrSde());
            record.setSub170(claim.getStatusCodeSde());

            // Get status description
            String statusText = claimStatusService.getStatusDescription(
                claim.getStatusCodeSde(), claim.getClaimNrSde());
            record.setSubsta(statusText);

            // Apply status filter
            if (!applyStatusFilter(claim, context)) {
                continue;
            }

            // Apply color coding
            applyColorCoding(record, claim, context);

            // Apply search filter
            if (applySearchFilter(record, context)) {
                // Apply remaining filters
                if (applyRemainingFilters(claim, context)) {
                    context.incrementZl1();
                    context.incrementZl2();
                    records.add(record);
                }
            }
        }

        // Handle empty result
        if (records.isEmpty()) {
            SubfileRecord emptyRecord = new SubfileRecord();
            emptyRecord.setMark1("");
            emptyRecord.setMark2("");
            context.setIndicator71(true);
            context.incrementZl1();
            context.incrementZl2();
            records.add(emptyRecord);
        }

        // Position to new claim if specified
        if (context.getNeucla() != null && !context.getNeucla().trim().isEmpty()) {
            positionToNewClaim(records, context);
        }

        result.setRecords(records);
        result.setTotalRecords(context.getZl1());
        return result;
    }

    /**
     * Apply claim type filter
     */
    private void applyClaimTypeFilter(HSG71LF2Entity claim, SubfileContext context) {
        List<HSG73PFEntity> failures = hsg73Repository.findByPakzAndClaimNr(
            claim.getPakz(), claim.getClaimNr());

        boolean found = false;
        for (HSG73PFEntity failure : failures) {
            String scope = claimFilterService.getScope(failure.getDemandCode());
            if (context.getFilart().equals(scope.substring(0, 1))) {
                found = true;
                break;
            }
        }

        if (!found) {
            claim.setStatusCodeSde(99);
        }
    }

    /**
     * Apply status filter
     */
    private boolean applyStatusFilter(HSG71LF2Entity claim, SubfileContext context) {
        if (context.getStatus() == null || context.getStatus().trim().isEmpty()) {
            return true;
        }

        int statusFilter = Integer.parseInt(context.getStatus());
        String operator = context.getZeichen() != null ? context.getZeichen() : "=";

        switch (operator) {
            case "=":
            case "*":
                return statusFilter == claim.getStatusCodeSde();
            case ">":
                return statusFilter < claim.getStatusCodeSde();
            case "<":
                return statusFilter > claim.getStatusCodeSde();
            default:
                return true;
        }
    }

    /**
     * Apply color coding based on claim status
     */
    private void applyColorCoding(SubfileRecord record, HSG71LF2Entity claim, SubfileContext context) {
        record.setIndicator41(false);
        record.setIndicator42(false);
        record.setIndicator43(false);
        record.setSubanz(0);

        if (claim.getClaimNrSde() == null || claim.getClaimNrSde().trim().isEmpty()) {
            record.setSub160("");
            return;
        }

        List<HSG73PFEntity> failures = hsg73Repository.findByPakzAndClaimNr(
            claim.getPakz(), claim.getClaimNr());

        if (failures.isEmpty() && claim.getStatusCodeSde() == 20) {
            record.setIndicator41(true);
        }

        for (HSG73PFEntity failure : failures) {
            // Red for errors
            if (failure.getStatusCode() == 16) {
                record.setIndicator41(true);
            }
            if (failure.getStatusCode() == 30 || 
                (failure.getStatusCode() == 0 && claim.getClaimNrSde() != null) ||
                (claim.getClaimNrSde() == null && claim.getStatusCodeSde() == 20)) {
                record.setIndicator41(true);
            }

            // Yellow for rejection
            if (failure.getStatusCode() == 11) {
                record.setIndicator42(true);
            }

            // Blue for waiting
            if (failure.getStatusCode() == 3 || failure.getStatusCode() == 11) {
                record.setIndicator43(true);
            }

            record.incrementSubanz();
        }

        // Set color indicator text
        StringBuilder colorText = new StringBuilder();
        if (record.isIndicator41()) {
            colorText.append("ROT");
        }
        if (record.isIndicator42()) {
            if (colorText.length() > 0) colorText.append(" ");
            colorText.append("GELB");
        }
        if (record.isIndicator43()) {
            if (colorText.length() > 0) colorText.append(" ");
            colorText.append("BLAU");
        }
        record.setSubcol(colorText.toString());
    }

    /**
     * Apply search filter
     */
    private boolean applySearchFilter(SubfileRecord record, SubfileContext context) {
        if (context.getSuchen() == null || context.getSuchen().trim().isEmpty()) {
            return true;
        }

        String searchString = context.getSuchen().toUpperCase();
        String searchableText = (
            record.getSub000() + record.getSub030() + record.getSub020() +
            record.getSub160() + record.getSub050() + record.getSub010() +
            record.getSub020() + record.getSub060() + record.getSub140() +
            record.getSub150() + record.getSub160() + record.getSub070() +
            record.getSub060() + record.getSubsta() + record.getSubcol()
        ).toUpperCase();

        return searchableText.contains(searchString);
    }

    /**
     * Apply remaining filters (PKZ, vehicle, customer, SDE)
     */
    private boolean applyRemainingFilters(HSG71LF2Entity claim, SubfileContext context) {
        // PKZ filter
        if (context.getFilpkz() != null && !context.getFilpkz().equals(context.getBts010()) 
            && !context.getFilpkz().equals(claim.getPakz())) {
            return false;
        }

        // Vehicle filter
        if (context.getFiltfg() != null && !context.getFiltfg().trim().isEmpty() 
            && !context.getFiltfg().equals(claim.getChassisNr())) {
            return false;
        }

        // Customer filter
        if (context.getFiltkd() != null && !context.getFiltkd().trim().isEmpty() 
            && !context.getFiltkd().equals(claim.getKdNr())) {
            return false;
        }

        // SDE claim number filter
        if (context.getFilsde() != null && !context.getFilsde().trim().isEmpty() 
            && !context.getFilsde().equals(claim.getClaimNrSde())) {
            return false;
        }

        return true;
    }

    /**
     * Position subfile to new claim
     */
    private void positionToNewClaim(List<SubfileRecord> records, SubfileContext context) {
        for (int i = 0; i < records.size(); i++) {
            SubfileRecord record = records.get(i);
            if (record.getSub050().compareTo(context.getNeucla()) >= 0) {
                context.setRec1(i + 1);
                context.setPag1(i + 1);
                break;
            }
        }
        context.setNeucla("");
    }

    /**
     * Format date from YYYYMMDD to display format
     */
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty() || "0".equals(isoDate)) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(isoDate, ISO_DATE_FORMATTER);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Parse ISO date string to LocalDate
     */
    private LocalDate parseIsoDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate, ISO_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}

/**
 * Context object for subfile processing
 */
class SubfileContext {
    private int zl1;
    private int zl2;
    private int zl4;
    private String sub15x;
    private String mark11;
    private String mark12;
    private String mark21;
    private String mark22;
    private String sub010;
    private String sub01x;
    private String pkz;
    private String neucla;
    private String status;
    private String zeichen;
    private String suchen;
    private String filpkz;
    private String filtfg;
    private String filtkd;
    private String filsde;
    private String filart;
    private String filoff;
    private String bts010;
    private int filtag;
    private int rec1;
    private int pag1;
    private boolean indicator50;
    private boolean indicator51;
    private boolean indicator52;
    private boolean indicator53;
    private boolean indicator54;
    private boolean indicator55;
    private boolean indicator56;
    private boolean indicator57;
    private boolean indicator58;
    private boolean indicator63;
    private boolean indicator71;
    private boolean indicator90;
    private boolean indicator99;

    public void incrementZl1() { this.zl1++; }
    public void incrementZl2() { this.zl2++; }

    // Getters and setters
    public int getZl1() { return zl1; }
    public void setZl1(int zl1) { this.zl1 = zl1; }
    public int getZl2() { return zl2; }
    public void setZl2(int zl2) { this.zl2 = zl2; }
    public int getZl4() { return zl4; }
    public void setZl4(int zl4) { this.zl4 = zl4; }
    public String getSub15x() { return sub15x; }
    public void setSub15x(String sub15x) { this.sub15x = sub15x; }
    public String getMark11() { return mark11; }
    public void setMark11(String mark11) { this.mark11 = mark11; }
    public String getMark12() { return mark12; }
    public void setMark12(String mark12) { this.mark12 = mark12; }
    public String getMark21() { return mark21; }
    public void setMark21(String mark21) { this.mark21 = mark21; }
    public String getMark22() { return mark22; }
    public void setMark22(String mark22) { this.mark22 = mark22; }
    public String getSub010() { return sub010; }
    public void setSub010(String sub010) { this.sub010 = sub010; }
    public String getSub01x() { return sub01x; }
    public void setSub01x(String sub01x) { this.sub01x = sub01x; }
    public String getPkz() { return pkz; }
    public void setPkz(String pkz) { this.pkz = pkz; }
    public String getNeucla() { return neucla; }
    public void setNeucla(String neucla) { this.neucla = neucla; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getZeichen() { return zeichen; }
    public void setZeichen(String zeichen) { this.zeichen = zeichen; }
    public String getSuchen() { return suchen; }
    public void setSuchen(String suchen) { this.suchen = suchen; }
    public String getFilpkz() { return filpkz; }
    public void setFilpkz(String filpkz) { this.filpkz = filpkz; }
    public String getFiltfg() { return filtfg; }
    public void setFiltfg(String filtfg) { this.filtfg = filtfg; }
    public String getFiltkd() { return filtkd; }
    public void setFiltkd(String filtkd) { this.filtkd = filtkd; }
    public String getFilsde() { return filsde; }
    public void setFilsde(String filsde) { this.filsde = filsde; }
    public String getFilart() { return filart; }
    public void setFilart(String filart) { this.filart = filart; }
    public String getFiloff() { return filoff; }
    public void setFiloff(String filoff) { this.filoff = filoff; }
    public String getBts010() { return bts010; }
    public void setBts010(String bts010) { this.bts010 = bts010; }
    public int getFiltag() { return filtag; }
    public void setFiltag(int filtag) { this.filtag = filtag; }
    public int getRec1() { return rec1; }
    public void setRec1(int rec1) { this.rec1 = rec1; }
    public int getPag1() { return pag1; }
    public void setPag1(int pag1) { this.pag1 = pag1; }
    public boolean isIndicator50() { return indicator50; }
    public void setIndicator50(boolean indicator50) { this.indicator50 = indicator50; }
    public boolean isIndicator51() { return indicator51; }
    public void setIndicator51(boolean indicator51) { this.indicator51 = indicator51; }
    public boolean isIndicator52() { return indicator52; }
    public void setIndicator52(boolean indicator52) { this.indicator52 = indicator52; }
    public boolean isIndicator53() { return indicator53; }
    public void setIndicator53(boolean indicator53) { this.indicator53 = indicator53; }
    public boolean isIndicator54() { return indicator54; }
    public void setIndicator54(boolean indicator54) { this.indicator54 = indicator54; }
    public boolean isIndicator55() { return indicator55; }
    public void setIndicator55(boolean indicator55) { this.indicator55 = indicator55; }
    public boolean isIndicator56() { return indicator56; }
    public void setIndicator56(boolean indicator56) { this.indicator56 = indicator56; }
    public boolean isIndicator57() { return indicator57; }
    public void setIndicator57(boolean indicator57) { this.indicator57 = indicator57; }
    public boolean isIndicator58() { return indicator58; }
    public void setIndicator58(boolean indicator58) { this.indicator58 = indicator58; }
    public boolean isIndicator63() { return indicator63; }
    public void setIndicator63(boolean indicator63) { this.indicator63 = indicator63; }
    public boolean isIndicator71() { return indicator71; }
    public void setIndicator71(boolean indicator71) { this.indicator71 = indicator71; }
    public boolean isIndicator90() { return indicator90; }
    public void setIndicator90(boolean indicator90) { this.indicator90 = indicator90; }
    public boolean isIndicator99() { return indicator99; }
    public void setIndicator99(boolean indicator99) { this.indicator99 = indicator99; }
}

/**
 * Subfile record for display
 */
class SubfileRecord {
    private String mark1;
    private String mark2;
    private String sub000;
    private String sub010;
    private String sub020;
    private String sub030;
    private String sub040;
    private String sub050;
    private String sub060;
    private String sub070;
    private String sub090;
    private int sub100;
    private String sub140;
    private String sub150;
    private String sub160;
    private int sub170;
    private String substa;
    private String subcol;
    private int subanz;
    private boolean indicator41;
    private boolean indicator42;
    private boolean indicator43;

    public void incrementSubanz() { this.subanz++; }

    // Getters and setters
    public String getMark1() { return mark1; }
    public void setMark1(String mark1) { this.mark1 = mark1; }
    public String getMark2() { return mark2; }
    public void setMark2(String mark2) { this.mark2 = mark2; }
    public String getSub000() { return sub000; }
    public void setSub000(String sub000) { this.sub000 = sub000; }
    public String getSub010() { return sub010; }
    public void setSub010(String sub010) { this.sub010 = sub010; }
    public String getSub020() { return sub020; }
    public void setSub020(String sub020) { this.sub020 = sub020; }
    public String getSub030() { return sub030; }
    public void setSub030(String sub030) { this.sub030 = sub030; }
    public String getSub040() { return sub040; }
    public void setSub040(String sub040) { this.sub040 = sub040; }
    public String getSub050() { return sub050; }
    public void setSub050(String sub050) { this.sub050 = sub050; }
    public String getSub060() { return sub060; }
    public void setSub060(String sub060) { this.sub060 = sub060; }
    public String getSub070() { return sub070; }
    public void setSub070(String sub070) { this.sub070 = sub070; }
    public String getSub090() { return sub090; }
    public void setSub090(String sub090) { this.sub090 = sub090; }
    public int getSub100() { return sub100; }
    public void setSub100(int sub100) { this.sub100 = sub100; }
    public String getSub140() { return sub140; }
    public void setSub140(String sub140) { this.sub140 = sub140; }
    public String getSub150() { return sub150; }
    public void setSub150(String sub150) { this.sub150 = sub150; }
    public String getSub160() { return sub160; }
    public void setSub160(String sub160) { this.sub160 = sub160; }
    public int getSub170() { return sub170; }
    public void setSub170(int sub170) { this.sub170 = sub170; }
    public String getSubsta() { return substa; }
    public void setSubsta(String substa) { this.substa = substa; }
    public String getSubcol() { return subcol; }
    public void setSubcol(String subcol) { this.subcol = subcol; }
    public int getSubanz() { return subanz; }
    public void setSubanz(int subanz) { this.subanz = subanz; }
    public boolean isIndicator41() { return indicator41; }
    public void setIndicator41(boolean indicator41) { this.indicator41 = indicator41; }
    public boolean isIndicator42() { return indicator42; }
    public void setIndicator42(boolean indicator42) { this.indicator42 = indicator42; }
    public boolean isIndicator43() { return indicator43; }
    public void setIndicator43(boolean indicator43) { this.indicator43 = indicator43; }
}

/**
 * Result object for subfile build operation
 */
class SubfileResult {
    private List<SubfileRecord> records;
    private int totalRecords;

    public List<SubfileRecord> getRecords() { return records; }
    public void setRecords(List<SubfileRecord> records) { this.records = records; }
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
}

@Entity
@Table(name = "HSFLALF1")
class HSFLALF1Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PKZ", length = 3)
    private String pkz;

    @Column(name = "BES-DAT", length = 8)
    private String besDat;

    @Column(name = "BES-NR", length = 5)
    private String besNr;

    @Column(name = "LNR-FL", precision = 3, scale = 0)
    private Integer lnrFl;

    @Column(name = "KEN-FL", length = 8)
    private String kenFl;

    @Column(name = "LNR", precision = 3, scale = 0)
    private Integer lnr;

    @Column(name = "BESCHREIBUNG", length = 40)
    private String beschreibung;

    @Column(name = "TEXTZEILEN", precision = 3, scale = 0)
    private Integer textzeilen;

    @Column(name = "EK-PREIS", precision = 7, scale = 2)
    private BigDecimal ekPreis;

    @Column(name = "MENGE", precision = 5, scale = 0)
    private Integer menge;

    @Column(name = "EK-RENR", length = 10)
    private String ekRenr;

    @Column(name = "EK-REDAT", length = 8)
    private String ekRedat;

    @Column(name = "EK BEMERKUNGEN 1", length = 60)
    private String ekBemerkungen1;

    @Column(name = "EK BEMERKUNGEN 2", length = 60)
    private String ekBemerkungen2;

    @Column(name = "EK-WERT", precision = 9, scale = 2)
    private BigDecimal ekWert;

    @Column(name = "ZUS %", length = 3)
    private String zusPercent;

    @Column(name = "VK-WERT", precision = 9, scale = 2)
    private BigDecimal vkWert;

    @Column(name = "AUFNR", length = 5)
    private String aufnr;

    @Column(name = "BEREI", length = 1)
    private String berei;

    @Column(name = "WT", length = 1)
    private String wt;

    @Column(name = "SPL", length = 2)
    private String spl;

    @Column(name = "AUFDAT", length = 8)
    private String aufdat;

    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer pos;

    @Column(name = "ERL-GRP", length = 2)
    private String erlGrp;

    @Column(name = "RECNR", length = 5)
    private String recnr;

    @Column(name = "RECDAT", length = 8)
    private String recdat;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "SDPS JOB UUID", length = 40)
    private String sdpsJobUuid;

    @Column(name = "SDPS FLA UUID", length = 40)
    private String sdpsFlaUuid;
}

@Entity
@Table(name = "HSAHKLF3")
class HSAHKLF3Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PAKZ", length = 3)
    private String pakz;

    @Column(name = "RNR", length = 5)
    private String rnr;

    @Column(name = "RG-NR. 10A", length = 10)
    private String rgNr10a;

    @Column(name = "RDAT", length = 8)
    private String rdat;

    @Column(name = "KZ S", length = 1)
    private String kzS;

    @Column(name = "ANR", length = 5)
    private String anr;

    @Column(name = "BEREI", length = 1)
    private String berei;

    @Column(name = "W/T", length = 1)
    private String wt;

    @Column(name = "SPLITT", length = 2)
    private String splitt;

    @Column(name = "ADAT", length = 8)
    private String adat;

    @Column(name = "ATEXT", length = 40)
    private String atext;

    @Column(name = "L.RNR", length = 5)
    private String lRnr;

    @Column(name = "STO-BEZ-RE", length = 5)
    private String stoBezRe;

    @Column(name = "STO-BEZ-REDAT", length = 8)
    private String stoBezRedat;

    @Column(name = "KOR-BEZ-RE", length = 5)
    private String korBezRe;

    @Column(name = "KOR-BEZ-REDAT", length = 8)
    private String korBezRedat;

    @Column(name = "BFORT", length = 1)
    private String bfort;

    @Column(name = "MWST Y/N", length = 1)
    private String mwstYn;

    @Column(name = "MWST %", precision = 5, scale = 2)
    private BigDecimal mwstPercent;

    @Column(name = "MWST % R.", precision = 5, scale = 2)
    private BigDecimal mwstPercentR;

    @Column(name = "BA-SCHL�SSEL", length = 2)
    private String baSchluessel;

    @Column(name = "KST LOHN", length = 5)
    private String kstLohn;

    @Column(name = "KST TEILE", length = 5)
    private String kstTeile;

    @Column(name = "FIBU MWST", length = 6)
    private String fibuMwst;

    @Column(name = "FIBU MWST AT", length = 6)
    private String fibuMwstAt;

    @Column(name = "FIBU INTERIM", length = 6)
    private String fibuInterim;

    @Column(name = "KTO INTAUF.", length = 6)
    private String ktoIntauf;

    @Column(name = "KTR INT AUF.", length = 7)
    private String ktrIntAuf;

    @Column(name = "KST INT AUF.", length = 5)
    private String kstIntAuf;

    @Column(name = "SPEZ-CODE", length = 10)
    private String spezCode;

    @Column(name = "BRANCH", length = 3)
    private String branch;

    @Column(name = "PROD-CODE", length = 10)
    private String prodCode;

    @Column(name = "PROJEKT", length = 10)
    private String projekt;

    @Column(name = "DOKUMENTENNUMMER", length = 20)
    private String dokumentennummer;

    @Column(name = "KOSTENCODE KONZINT.", length = 3)
    private String kostencodeKonzint;

    @Column(name = "KUNDEN-NR.", length = 6)
    private String kundenNr;

    @Column(name = "ANREDE", length = 1)
    private String anrede;

    @Column(name = "NAME", length = 30)
    private String name;

    @Column(name = "BRANCHE", length = 25)
    private String branche;

    @Column(name = "MATCH", length = 5)
    private String match;

    @Column(name = "STRASSE", length = 25)
    private String strasse;

    @Column(name = "LAND", length = 3)
    private String land;

    @Column(name = "PLZ", length = 5)
    private String plz;

    @Column(name = "ORT", length = 20)
    private String ort;

    @Column(name = "TELEFON", length = 17)
    private String telefon;

    @Column(name = "BESTELLER KUNDE", length = 20)
    private String bestellerKunde;

    @Column(name = "VALUTA", length = 1)
    private String valuta;

    @Column(name = "BONIT#T", length = 1)
    private String bonitaet;

    @Column(name = "ZAHLUNGSART", length = 1)
    private String zahlungsart;

    @Column(name = "RC", length = 3)
    private String rc;

    @Column(name = "RE KUNDEN-NR.", length = 6)
    private String reKundenNr;

    @Column(name = "RE ANREDE", length = 1)
    private String reAnrede;

    @Column(name = "RE NAME", length = 30)
    private String reName;

    @Column(name = "RE BRANCHE", length = 25)
    private String reBranche;

    @Column(name = "RE MATCH", length = 5)
    private String reMatch;

    @Column(name = "RE STRASSE", length = 25)
    private String reStrasse;

    @Column(name = "RE LAND", length = 3)
    private String reLand;

    @Column(name = "RE PLZ", length = 5)
    private String rePlz;

    @Column(name = "RE ORT", length = 20)
    private String reOrt;

    @Column(name = "RE TELE.", length = 17)
    private String reTele;

    @Column(name = "RE VALUTA", length = 1)
    private String reValuta;

    @Column(name = "RE BONIT#T", length = 1)
    private String reBonitaet;

    @Column(name = "RE ZART", length = 1)
    private String reZart;

    @Column(name = "RE RC", length = 3)
    private String reRc;

    @Column(name = "UST-ID-NR/OK", length = 20)
    private String ustIdNrOk;

    @Column(name = "FAHRG.-NR.", length = 17)
    private String fahrgNr;

    @Column(name = "KZ", length = 12)
    private String kz;

    @Column(name = "TYP", length = 15)
    private String typ;

    @Column(name = "BJ", length = 4)
    private String bj;

    @Column(name = "ZDAT", length = 8)
    private String zdat;

    @Column(name = "WRG.", length = 3)
    private String wrg;

    @Column(name = "AU", length = 6)
    private String au;

    @Column(name = "GA", length = 8)
    private String ga;

    @Column(name = "SP", length = 6)
    private String sp;

    @Column(name = "TACHO", length = 8)
    private String tacho;

    @Column(name = "KM", length = 8)
    private String km;

    @Column(name = "HU", length = 6)
    private String hu;

    @Column(name = "AN-TAG", length = 8)
    private String anTag;

    @Column(name = "AN-ZEIT", length = 4)
    private String anZeit;

    @Column(name = "FERT-TAG", length = 8)
    private String fertTag;

    @Column(name = "FERT-ZEIT", length = 4)
    private String fertZeit;

    @Column(name = "BERATER", length = 20)
    private String berater;

    @Column(name = "LEITZAHL", length = 3)
    private String leitzahl;

    @Column(name = "TX.ANF", length = 3)
    private String txAnf;

    @Column(name = "TX.ENDE", length = 3)
    private String txEnde;

    @Column(name = "MOTOR-NR", length = 10)
    private String motorNr;

    @Column(name = "MOTOR-TYP", length = 20)
    private String motorTyp;

    @Column(name = "USER AUFTRAG", length = 10)
    private String userAuftrag;

    @Column(name = "USER RECHNUNG", length = 10)
    private String userRechnung;

    @Column(name = "RGS NETTO", precision = 9, scale = 2)
    private BigDecimal rgsNetto;

    @Column(name = "RGS BASIS AT", precision = 9, scale = 2)
    private BigDecimal rgsBasisAt;

    @Column(name = "RGS BASIS MWST", precision = 9, scale = 2)
    private BigDecimal rgsBasisMwst;

    @Column(name = "RGS MWST", precision = 9, scale = 2)
    private BigDecimal rgsMwst;

    @Column(name = "RGS MWST AT", precision = 9, scale = 2)
    private BigDecimal rgsMwstAt;

    @Column(name = "RGS GES BRUTTO", precision = 9, scale = 2)
    private BigDecimal rgsGesBrutto;

    @Column(name = "EG-UMSATZ", length = 1)
    private String egUmsatz;

    @Column(name = "STEUERFREI DRITTLAND", length = 1)
    private String steuerfreiDrittland;

    @Column(name = "VERBUCHT?", length = 1)
    private String verbucht;

    @Column(name = "RESERVE", precision = 5, scale = 2)
    private BigDecimal reserve1;

    @Column(name = "RESERVE", precision = 9, scale = 2)
    private BigDecimal reserve2;

    @Column(name = "GA-�BERN.", length = 8)
    private String gaUebern;

    @Column(name = "WKT-ID", precision = 9, scale = 0)
    private Integer wktId;

    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve3;

    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve4;

    @Column(name = "F:V>0", precision = 3, scale = 0)
    private Integer fvGt0;

    @Column(name = "F:B>0", precision = 3, scale = 0)
    private Integer fbGt0;

    @Column(name = "KAMPAGNE-NR", precision = 6, scale = 0)
    private Integer kampagneNr;

    @Column(name = "SPO ORDER", length = 10)
    private String spoOrder;

    @Column(name = "KEN-AV", length = 2)
    private String kenAv;

    @Column(name = "KEN-PE", length = 2)
    private String kenPe;

    @Column(name = "KLR-BERECH", length = 1)
    private String klrBerech;

    @Column(name = "KLR-BETRAG", precision = 5, scale = 2)
    private BigDecimal klrBetrag;

    @Column(name = "ASSI-VORGANG-NR", length = 15)
    private String assiVorgangNr;

    @Column(name = "ZAGA-GUELTIG", length = 8)
    private String zagaGueltig;

    @Column(name = "R&W FREIGABE-NR", length = 15)
    private String rwFreigabeNr;

    @Column(name = "KL-ERWEITERUNG", precision = 5, scale = 0)
    private Integer klErweiterung;

    @Column(name = "KL-AUSNAHME IDNR", length = 3)
    private String klAusnahmeIdnr;

    @Column(name = "KL-AUSNAHME KLARTEXT", length = 40)
    private String klAusnahmeKlartext;

    @Column(name = "FAHRZEUG-ART", length = 20)
    private String fahrzeugArt;

    @Column(name = "HERSTELLER", length = 20)
    private String hersteller;

    @Column(name = "AUFBAUART", length = 20)
    private String aufbauart;

    @Column(name = "HERSTELLER AUFBAU", length = 20)
    private String herstellerAufbau;

    @Column(name = "ZUSATZAUSR�STUNG 1", length = 20)
    private String zusatzausruestung1;

    @Column(name = "HERSTELLER ZUSATZ 1", length = 20)
    private String herstellerZusatz1;

    @Column(name = "ZUSATZAUSR�STUNG 2", length = 20)
    private String zusatzausruestung2;

    @Column(name = "HERSTELLER ZUSATZ 2", length = 20)
    private String herstellerZusatz2;

    @Column(name = "ZUSATZAUSR�STUNG 3", length = 20)
    private String zusatzausruestung3;

    @Column(name = "HERSTELLER ZUSATZ 3", length = 20)
    private String herstellerZusatz3;

    @Column(name = "EINSATZART", length = 20)
    private String einsatzart;

    @Column(name = "EURO-NORM", length = 10)
    private String euroNorm;

    @Column(name = "PARTIKELFILTER", length = 1)
    private String partikelfilter;

    @Column(name = "IS-ART", length = 5)
    private String isArt;

    @Column(name = "MAIL TO", length = 200)
    private String mailTo;

    @Column(name = "MAIL CC", length = 200)
    private String mailCc;
}

@Entity
@Table(name = "HSAHWPF")
class HSAHWPFEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PAKZ", length = 3)
    private String pakz;

    @Column(name = "RNR", length = 5)
    private String rnr;

    @Column(name = "RG-NR. 10A", length = 10)
    private String rgNr10a;

    @Column(name = "RDAT", length = 8)
    private String rdat;

    @Column(name = "KZ S", length = 1)
    private String kzS;

    @Column(name = "ANR", length = 5)
    private String anr;

    @Column(name = "BEREI", length = 1)
    private String berei;

    @Column(name = "W/T", length = 1)
    private String wt;

    @Column(name = "SPLITT", length = 2)
    private String splitt;

    @Column(name = "POS.", precision = 3, scale = 0)
    private Integer pos;

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
    private String lNr;

    @Column(name = "BEZ.", length = 40)
    private String bez;

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
    private String kenRe2sum;

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
}

@Entity
@Table(name = "HSG70F")
class HSG70FEntity {
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
}

@Entity
@Table(name = "HSG71LF2")
class HSG71LF2Entity {
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
}

@Entity
@Table(name = "HSG73PF")
class HSG73PFEntity {
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

    @Column(name = "BEREICH", length = 1)
    private String bereich;

    @Column(name = "CLAIM-NR.", length = 8)
    private String claimNr;

    @Column(name = "FEHLER-NR.", length = 2)
    private String fehlerNr;

    @Column(name = "FOLGE-NR.", length = 2)
    private String folgeNr;

    @Column(name = "FEHLER-TEIL", length = 18)
    private String fehlerTeil;

    @Column(name = "HAUPTGRUPPE", length = 2)
    private String hauptgruppe;

    @Column(name = "NEBENGRUPPE", length = 2)
    private String nebengruppe;

    @Column(name = "SCHAD.-C1", length = 2)
    private String schadC1;

    @Column(name = "SCHAD.-C2", length = 2)
    private String schadC2;

    @Column(name = "TEXT1", length = 65)
    private String text1;

    @Column(name = "TEXT2", length = 65)
    private String text2;

    @Column(name = "STEUER CODE", length = 2)
    private String steuerCode;

    @Column(name = "BEW. CODE1", length = 2)
    private String bewCode1;

    @Column(name = "BEW. CODE2", precision = 4, scale = 0)
    private Integer bewCode2;

    @Column(name = "BEW. DATUM", precision = 8, scale = 0)
    private Integer bewDatum;

    @Column(name = "VERG. MAT.", precision = 3, scale = 0)
    private Integer vergMat;

    @Column(name = "VERG. ARB.", precision = 3, scale = 0)
    private Integer vergArb;

    @Column(name = "VERG. SPEZ.", precision = 3, scale = 0)
    private Integer vergSpez;

    @Column(name = "BEANTR. MAT.", precision = 11, scale = 2)
    private BigDecimal beantrMat;

    @Column(name = "BEANTRG. ARB.", precision = 11, scale = 2)
    private BigDecimal beantrgArb;

    @Column(name = "BEANTRG. SPEZ.", precision = 11, scale = 2)
    private BigDecimal beantrgSpez;

    @Column(name = "CLAIM-ART", precision = 1, scale = 0)
    private Integer claimArt;

    @Column(name = "V.-REP.-DATUM", precision = 8, scale = 0)
    private Integer vRepDatum;

    @Column(name = "V.-KM-STAND", precision = 3, scale = 0)
    private Integer vKmStand;

    @Column(name = "FELDTEST-NR.", precision = 6, scale = 0)
    private Integer feldtestNr;

    @Column(name = "KAMPAGNEN-NR.", length = 8)
    private String kampagnenNr;

    @Column(name = "EPS", length = 20)
    private String eps;

    @Column(name = "STATUS CODE", precision = 2, scale = 0)
    private Integer statusCode;

    @Column(name = "VARIANT CODE", precision = 2, scale = 0)
    private Integer variantCode;

    @Column(name = "ACTION CODE", precision = 2, scale = 0)
    private Integer actionCode;

    @Column(name = "TEXT3", length = 65)
    private String text3;

    @Column(name = "TEXT4", length = 65)
    private String text4;

    @Column(name = "FEHLER-NR. SDE", length = 2)
    private String fehlerNrSde;

    @Column(name = "ANHANG", length = 1)
    private String anhang;

    @Column(name = "SOURCE", length = 5)
    private String source;

    @Column(name = "COMPLAIN", length = 5)
    private String complain;

    @Column(name = "SYMPTOM", length = 5)
    private String symptom;

    @Column(name = "FAILURE", length = 5)
    private String failure;

    @Column(name = "LOCATION", length = 5)
    private String location;

    @Column(name = "REPAIR", length = 5)
    private String repair;

    @Column(name = "ERG.CODE", length = 2)
    private String ergCode;

    @Column(name = "RESULT1", length = 2)
    private String result1;

    @Column(name = "RESULT2", length = 5)
    private String result2;

    @Column(name = "FAULT1", length = 2)
    private String fault1;

    @Column(name = "FAULT2", length = 5)
    private String fault2;

    @Column(name = "REPLY1", length = 2)
    private String reply1;

    @Column(name = "REPLY2", length = 5)
    private String reply2;

    @Column(name = "EXPLANATION1", length = 2)
    private String explanation1;

    @Column(name = "EXPLANATION2", length = 5)
    private String explanation2;

    public String getDemandCode() {
        return hauptgruppe;
    }
}

interface HSG71LF2Repository extends JpaRepository<HSG71LF2Entity, Long> {
    List<HSG71LF2Entity> findByPakzOrderByClaimNrAsc(String pakz);
    List<HSG71LF2Entity> findByPakzOrderByClaimNrDesc(String pakz);
}

interface HSG73PFRepository extends JpaRepository<HSG73PFEntity, Long> {
    List<HSG73PFEntity> findByPakzAndClaimNr(String pakz, String claimNr);
}

interface HSAHKLF3Repository extends JpaRepository<HSAHKLF3Entity, Long> {
}

interface HSAHWPFRepository extends JpaRepository<HSAHWPFEntity, Long> {
}

interface HSFLALF1Repository extends JpaRepository<HSFLALF1Entity, Long> {
}

interface HSG70FRepository extends JpaRepository<HSG70FEntity, Long> {
}

@Service
class ClaimStatusService {
    public String getStatusDescription(int statusCode, String claimNrSde) {
        if ("00000000".equals(claimNrSde)) {
            if (statusCode == 5) {
                return "Minimumantrag";
            } else if (statusCode == 20) {
                return "Minimum ausgebucht";
            } else {
                return "Minimumantrag";
            }
        }
        return "";
    }
}

@Service
class ClaimFilterService {
    public String getScope(String demandCode) {
        return "G";
    }
}
