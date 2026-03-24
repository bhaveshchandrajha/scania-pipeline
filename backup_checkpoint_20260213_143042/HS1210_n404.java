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
 * Service class migrated from RPG subroutine n404 (SB10N and related subroutines).
 * Handles warranty claim processing, subfile management, and claim creation logic.
 * 
 * Display file: HS1210D
 * - Manages claim list display with filtering and sorting
 * - Supports claim creation, modification, deletion, and status changes
 * - Provides various selection options (2=Change, 4=Delete, 5=Display, 6=Service Card, 8=Warranty Info, 9=Status Change, 10=Send)
 */
@Service
@Transactional
public class ClaimProcessingService {

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
    private HSGSCPFRepository hsgscRepository;
    
    @Autowired
    private HSBTSLFRepository hsbtsRepository;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter EUR_DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    /**
     * Subroutine SB10N - Initialize subfile processing variables
     */
    public void initializeSubfileProcessing(SubfileContext context) {
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
     * Subroutine MARK - Handle selection mark conversion '1 ' -> ' 1'
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
     * Subroutine SB100 - Build and display subfile with claims
     */
    public SubfileResult buildClaimSubfile(SubfileFilter filter, String pkz, boolean ascending) {
        SubfileResult result = new SubfileResult();
        result.setZl1(0);
        result.setZl2(0);
        
        List<HSG71LF2> claims;
        
        if (ascending) {
            if (filter.isUseLogicalFile()) {
                claims = hsg71Repository.findByPakzOrderByClaimNrAsc(pkz);
            } else {
                claims = hsg71Repository.findByPakzOrderByRechNrAsc(pkz);
            }
        } else {
            if (filter.isUseLogicalFile()) {
                claims = hsg71Repository.findByPakzOrderByClaimNrDesc(pkz);
            } else {
                claims = hsg71Repository.findByPakzOrderByRechNrDesc(pkz);
            }
        }
        
        List<SubfileRecord> subfileRecords = new ArrayList<>();
        
        for (HSG71LF2 claim : claims) {
            if (result.getZl1() >= 9999) {
                break;
            }
            
            // Apply filters
            if (!applyClaimFilters(claim, filter)) {
                continue;
            }
            
            // Check for open claims if filter is set
            if ("J".equals(filter.getFiloff())) {
                boolean isOpen = checkOpenClaim(claim);
                if (!isOpen) {
                    continue;
                }
            }
            
            if (claim.getStatusCodeSde() != 99) {
                SubfileRecord record = buildSubfileRecord(claim);
                
                // Apply status filter
                if (!applyStatusFilter(record, filter)) {
                    continue;
                }
                
                // Apply search filter
                if (!applySearchFilter(record, filter)) {
                    continue;
                }
                
                // Apply additional filters
                if (applyAdditionalFilters(claim, filter, pkz)) {
                    result.setZl1(result.getZl1() + 1);
                    result.setZl2(result.getZl2() + 1);
                    subfileRecords.add(record);
                }
            }
        }
        
        if (result.getZl1() == 0) {
            result.setIndicator71(true);
            result.setZl1(1);
            result.setZl2(1);
            SubfileRecord emptyRecord = new SubfileRecord();
            subfileRecords.add(emptyRecord);
        }
        
        result.setRecords(subfileRecords);
        return result;
    }

    /**
     * Apply claim age filter
     */
    private boolean applyClaimFilters(HSG71LF2 claim, SubfileFilter filter) {
        if (filter.getFiltag() != 0 && claim.getStatusCodeSde() != 99) {
            try {
                LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getRepDatum()), ISO_DATE_FORMATTER);
                LocalDate currentDate = LocalDate.now();
                long daysDiff = ChronoUnit.DAYS.between(repairDate, currentDate);
                
                if (daysDiff > filter.getFiltag()) {
                    return false;
                }
            } catch (Exception e) {
                // Invalid date, skip filter
            }
        }
        
        // Apply claim type filter
        if (filter.getFilart() != null && !filter.getFilart().trim().isEmpty() && claim.getStatusCodeSde() != 99) {
            if (!checkClaimType(claim, filter.getFilart())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Check if claim is open
     */
    private boolean checkOpenClaim(HSG71LF2 claim) {
        boolean isOpen = false;
        
        if (claim.getStatusCodeSde() < 20 && claim.getStatusCodeSde() != 5) {
            isOpen = true;
        }
        
        List<HSG73PF> failures = hsg73Repository.findByPakzAndRechNrAndRechDatum(
            claim.getPakz(), claim.getRechNr(), claim.getRechDatum());
        
        if (failures.isEmpty()) {
            isOpen = true;
        }
        
        for (HSG73PF failure : failures) {
            if (failure.getStatusCode() == 0) {
                isOpen = true;
            }
        }
        
        return isOpen;
    }

    /**
     * Build subfile record from claim entity
     */
    private SubfileRecord buildSubfileRecord(HSG71LF2 claim) {
        SubfileRecord record = new SubfileRecord();
        record.setSub000(claim.getPakz());
        record.setSub010(claim.getRechNr());
        record.setSub020(formatDate(claim.getRechDatum()));
        record.setSub030(claim.getAuftragsNr());
        record.setSub040(String.valueOf(claim.getWete()));
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
        Optional<HSGSCPF> statusOpt = hsgscRepository.findById(claim.getStatusCodeSde());
        if (statusOpt.isPresent()) {
            record.setSubsta(statusOpt.get().getStatusDescription());
        } else {
            record.setSubsta("");
        }
        
        // Handle minimum claims
        if ("00000000".equals(claim.getClaimNrSde())) {
            if (claim.getStatusCodeSde() == 5) {
                record.setSubsta("Minimumantrag");
            } else if (claim.getStatusCodeSde() == 20) {
                record.setSubsta("Minimum ausgebucht");
            } else {
                record.setSubsta("Minimumantrag");
            }
        }
        
        // Set color indicators
        setColorIndicators(record, claim);
        
        return record;
    }

    /**
     * Set color indicators for claim status
     */
    private void setColorIndicators(SubfileRecord record, HSG71LF2 claim) {
        record.setIndicator41(false); // Red
        record.setIndicator42(false); // Yellow
        record.setIndicator43(false); // Blue
        
        if (!"00000000".equals(claim.getClaimNrSde())) {
            List<HSG73PF> failures = hsg73Repository.findByPakzAndRechNrAndClaimNr(
                claim.getPakz(), claim.getRechNr(), claim.getClaimNr());
            
            if (failures.isEmpty() && claim.getStatusCodeSde() == 20) {
                record.setIndicator41(true); // Red
            }
            
            for (HSG73PF failure : failures) {
                // Red for errors
                if (failure.getStatusCode() == 16 || failure.getStatusCode() == 30 || 
                    (failure.getStatusCode() == 0 && !claim.getClaimNrSde().trim().isEmpty())) {
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
            }
        }
        
        // Set color text
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
     * Apply status filter
     */
    private boolean applyStatusFilter(SubfileRecord record, SubfileFilter filter) {
        if (filter.getStatus() == null || filter.getStatus().trim().isEmpty()) {
            return true;
        }
        
        int statusFilter = Integer.parseInt(filter.getStatus());
        String operator = filter.getZeichen() != null ? filter.getZeichen() : "=";
        
        if ("=".equals(operator) || "*".equals(operator)) {
            return record.getSub170() == statusFilter;
        } else if (">".equals(operator)) {
            return record.getSub170() <= statusFilter;
        } else if ("<".equals(operator)) {
            return record.getSub170() >= statusFilter;
        }
        
        return true;
    }

    /**
     * Apply search filter
     */
    private boolean applySearchFilter(SubfileRecord record, SubfileFilter filter) {
        if (filter.getSuchen() == null || filter.getSuchen().trim().isEmpty()) {
            return true;
        }
        
        String searchString = filter.getSuchen().toUpperCase();
        String searchableContent = (
            record.getSub000() + record.getSub030() + record.getSub020() +
            record.getSub160() + record.getSub050() + record.getSub010() +
            record.getSub020() + record.getSub060() + record.getSub140() +
            record.getSub150() + record.getSub160() + record.getSub070() +
            record.getSub060() + record.getSubsta() + record.getSubcol()
        ).toUpperCase();
        
        return searchableContent.contains(searchString);
    }

    /**
     * Apply additional filters
     */
    private boolean applyAdditionalFilters(HSG71LF2 claim, SubfileFilter filter, String mainPkz) {
        // PKZ filter
        if (!filter.getFilpkz().equals(mainPkz) && !filter.getFilpkz().equals(claim.getPakz())) {
            return false;
        }
        
        // Vehicle filter
        if (filter.getFiltfg() != null && !filter.getFiltfg().trim().isEmpty() &&
            !filter.getFiltfg().equals(claim.getChassisNr())) {
            return false;
        }
        
        // Customer filter
        if (filter.getFiltkd() != null && !filter.getFiltkd().trim().isEmpty() &&
            !filter.getFiltkd().equals(claim.getKdNr())) {
            return false;
        }
        
        // SDE claim number filter
        if (filter.getFilsde() != null && !filter.getFilsde().trim().isEmpty() &&
            !filter.getFilsde().equals(claim.getClaimNrSde())) {
            return false;
        }
        
        return true;
    }

    /**
     * Check claim type
     */
    private boolean checkClaimType(HSG71LF2 claim, String claimType) {
        List<HSG73PF> failures = hsg73Repository.findByPakzAndRechNrAndClaimNr(
            claim.getPakz(), claim.getRechNr(), claim.getClaimNr());
        
        for (HSG73PF failure : failures) {
            // Implementation would check warranty scope based on demand code
            // This is a placeholder for the actual warranty scope check
            if ("G".equals(claimType)) {
                // Check if warranty scope
                return true;
            } else if ("K".equals(claimType)) {
                // Check if goodwill
                return false;
            }
        }
        
        return false;
    }

    /**
     * Create new claim from invoice
     */
    public ClaimCreationResult createClaimFromInvoice(ClaimCreationRequest request) {
        ClaimCreationResult result = new ClaimCreationResult();
        
        // Validate invoice exists
        Optional<HSAHKLF3> invoiceOpt = hsahkRepository.findByPakzAndRnrAndRdat(
            request.getPkz(), request.getInvoiceNr(), request.getInvoiceDate());
        
        if (!invoiceOpt.isPresent()) {
            result.setSuccess(false);
            result.setIndicator58(true);
            return result;
        }
        
        HSAHKLF3 invoice = invoiceOpt.get();
        
        // Check for cancellation
        if (checkInvoiceCancellation(invoice)) {
            result.setSuccess(false);
            result.setIndicator58(true);
            return result;
        }
        
        // Check if already transferred
        if (checkAlreadyTransferred(request)) {
            result.setSuccess(false);
            result.setIndicator60(true);
            return result;
        }
        
        // Create claim header
        HSG71LF2 claim = new HSG71LF2();
        claim.setPakz(invoice.getPakz());
        claim.setRechNr(invoice.getRnr());
        claim.setRechDatum(invoice.getRdat());
        claim.setAuftragsNr(invoice.getAnr());
        claim.setWete(invoice.getBerei().charAt(0));
        
        // Set vehicle information
        claim.setChassisNr(invoice.getFahrgNr().substring(0, 7));
        claim.setKennzeichen(invoice.getKz());
        
        // Set dates
        if ("1".equals(invoice.getWt())) {
            if (invoice.getSplitt().substring(1, 2).equals("4")) {
                claim.setRepDatum(parseDateToInt(invoice.getFertTag()));
            } else {
                claim.setRepDatum(parseDateToInt(invoice.getAnTag()));
            }
        } else {
            claim.setRepDatum(parseDateToInt(invoice.getAdat()));
        }
        
        if (claim.getRepDatum() == 0) {
            claim.setRepDatum(parseDateToInt(invoice.getAdat()));
        }
        
        // Set mileage
        try {
            int mileage = Integer.parseInt(invoice.getKm());
            claim.setKmStand(mileage / 1000);
        } catch (Exception e) {
            claim.setKmStand(0);
        }
        
        // Set product type
        claim.setProduktTyp(determineProductType(invoice));
        
        // Set customer information
        claim.setKdNr(invoice.getKundenNr());
        claim.setKdName(invoice.getName());
        
        // Initialize status
        claim.setStatusCodeSde(0);
        claim.setClaimNrSde("");
        claim.setAnzFehler(0);
        claim.setBereich(invoice.getBerei());
        claim.setAufNr(invoice.getAnr() + invoice.getBerei() + invoice.getWt() + invoice.getSplitt());
        
        // Generate claim number
        int claimNumber = generateClaimNumber(request.getPkz());
        claim.setClaimNr(String.format("%08d", claimNumber));
        
        // Save claim
        hsg71Repository.save(claim);
        
        // Transfer positions
        transferInvoicePositions(invoice, claim);
        
        result.setSuccess(true);
        result.setClaimNumber(claim.getClaimNr());
        return result;
    }

    /**
     * Check if invoice is cancelled
     */
    private boolean checkInvoiceCancellation(HSAHKLF3 invoice) {
        List<HSAHKLF3> cancellations = hsahkRepository.findByPakzAndAdatAndAnr(
            invoice.getPakz(), invoice.getAdat(), invoice.getAnr());
        
        for (HSAHKLF3 cancel : cancellations) {
            if ("S".equals(cancel.getKzS()) && invoice.getRnr().equals(cancel.getRnr())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if invoice already transferred to warranty
     */
    private boolean checkAlreadyTransferred(ClaimCreationRequest request) {
        List<HSG71LF2> existingClaims = hsg71Repository.findByPakzAndRechNrAndRechDatum(
            request.getPkz(), request.getInvoiceNr(), request.getInvoiceDate());
        
        for (HSG71LF2 claim : existingClaims) {
            if (claim.getStatusCodeSde() != 99) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Determine product type from vehicle number
     */
    private int determineProductType(HSAHKLF3 invoice) {
        String vehicleType = invoice.getFahrzeugArt();
        
        if (vehicleType != null && vehicleType.startsWith("M")) {
            return 3; // LKW
        }
        
        // Check product master for type
        // This is a placeholder - actual implementation would query product master
        return 1; // Default to truck
    }

    /**
     * Generate next claim number
     */
    private int generateClaimNumber(String pkz) {
        List<HSG71LF2> claims = hsg71Repository.findByPakzOrderByClaimNrDesc(pkz);
        
        if (claims.isEmpty()) {
            return 10000000;
        }
        
        HSG71LF2 lastClaim = claims.get(0);
        int lastNumber = Integer.parseInt(lastClaim.getClaimNr());
        
        return lastNumber + 1;
    }

    /**
     * Transfer invoice positions to warranty claim
     */
    private void transferInvoicePositions(HSAHKLF3 invoice, HSG71LF2 claim) {
        int positionCounter = 0;
        
        // Transfer parts
        List<HSAHTPR> parts = hsahwRepository.findPartsByInvoice(
            invoice.getPakz(), invoice.getRnr(), invoice.getRdat(), 
            invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        for (HSAHTPR part : parts) {
            if (part.getPartNumber() != null && !part.getPartNumber().trim().isEmpty() &&
                part.getNetPrice() != null && part.getNetPrice().compareTo(BigDecimal.ZERO) != 0) {
                positionCounter++;
                // Create warranty position record
                // Implementation would create HSGPSPF record
            }
        }
        
        // Transfer labor
        List<HSAHWPR> labor = hsahwRepository.findLaborByInvoice(
            invoice.getPakz(), invoice.getRnr(), invoice.getRdat(),
            invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        for (HSAHWPR work : labor) {
            positionCounter++;
            // Create warranty position record
            // Implementation would create HSGPSPF record
        }
        
        // Transfer external services
        List<HSFLALF1> externalServices = hsflaRepository.findByInvoice(
            invoice.getPakz(), invoice.getAdat(), invoice.getAnr());
        
        for (HSFLALF1 service : externalServices) {
            if (Integer.parseInt(service.getStatus()) > 3) {
                positionCounter++;
                // Create warranty position record
                // Implementation would create HSGPSPF record
            }
        }
    }

    /**
     * Format date from numeric to display format
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "0".equals(dateStr)) {
            return "";
        }
        
        try {
            LocalDate date = LocalDate.parse(dateStr, ISO_DATE_FORMATTER);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Parse date string to integer
     */
    private int parseDateToInt(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(dateStr);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Delete claim
     */
    public void deleteClaim(String pkz, String claimNr) {
        Optional<HSG71LF2> claimOpt = hsg71Repository.findByPakzAndClaimNr(pkz, claimNr);
        
        if (claimOpt.isPresent()) {
            HSG71LF2 claim = claimOpt.get();
            claim.setStatusCodeSde(99);
            hsg71Repository.save(claim);
            
            // Delete failure records
            List<HSG73PF> failures = hsg73Repository.findByPakzAndRechNrAndClaimNr(
                pkz, claim.getRechNr(), claimNr);
            hsg73Repository.deleteAll(failures);
            
            // Delete position records would be done here
        }
    }

    /**
     * Update claim status
     */
    public void updateClaimStatus(String pkz, String claimNr, int newStatus) {
        Optional<HSG71LF2> claimOpt = hsg71Repository.findByPakzAndClaimNr(pkz, claimNr);
        
        if (claimOpt.isPresent()) {
            HSG71LF2 claim = claimOpt.get();
            claim.setStatusCodeSde(newStatus);
            hsg71Repository.save(claim);
        }
    }

    /**
     * Process minimum claim posting
     */
    public void processMinimumClaim(String pkz, String claimNr) {
        Optional<HSG71LF2> claimOpt = hsg71Repository.findByPakzAndClaimNr(pkz, claimNr);
        
        if (claimOpt.isPresent()) {
            HSG71LF2 claim = claimOpt.get();
            
            if (claim.getStatusCodeSde() == 5) {
                claim.setStatusCodeSde(20);
                hsg71Repository.save(claim);
                
                // Call external program for posting
                // Implementation would call HS1219M or AX9999C
            }
        }
    }
}

// Supporting classes

class SubfileContext {
    private int zl4;
    private String sub15x;
    private String mark11;
    private String mark12;
    private String mark21;
    private String mark22;
    private String sub010;
    private boolean indicator50;
    private boolean indicator51;
    private boolean indicator52;
    private boolean indicator53;
    private boolean indicator54;
    private boolean indicator55;
    private boolean indicator56;
    private boolean indicator57;
    private boolean indicator58;
    
    // Getters and setters
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
}

class SubfileFilter {
    private boolean useLogicalFile;
    private int filtag;
    private String filart;
    private String filoff;
    private String status;
    private String zeichen;
    private String suchen;
    private String filpkz;
    private String filtfg;
    private String filtkd;
    private String filsde;
    
    // Getters and setters
    public boolean isUseLogicalFile() { return useLogicalFile; }
    public void setUseLogicalFile(boolean useLogicalFile) { this.useLogicalFile = useLogicalFile; }
    public int getFiltag() { return filtag; }
    public void setFiltag(int filtag) { this.filtag = filtag; }
    public String getFilart() { return filart; }
    public void setFilart(String filart) { this.filart = filart; }
    public String getFiloff() { return filoff; }
    public void setFiloff(String filoff) { this.filoff = filoff; }
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
}

class SubfileResult {
    private int zl1;
    private int zl2;
    private boolean indicator71;
    private List<SubfileRecord> records;
    
    // Getters and setters
    public int getZl1() { return zl1; }
    public void setZl1(int zl1) { this.zl1 = zl1; }
    public int getZl2() { return zl2; }
    public void setZl2(int zl2) { this.zl2 = zl2; }
    public boolean isIndicator71() { return indicator71; }
    public void setIndicator71(boolean indicator71) { this.indicator71 = indicator71; }
    public List<SubfileRecord> getRecords() { return records; }
    public void setRecords(List<SubfileRecord> records) { this.records = records; }
}

class SubfileRecord {
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
    private boolean indicator41;
    private boolean indicator42;
    private boolean indicator43;
    
    // Getters and setters
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
    public boolean isIndicator41() { return indicator41; }
    public void setIndicator41(boolean indicator41) { this.indicator41 = indicator41; }
    public boolean isIndicator42() { return indicator42; }
    public void setIndicator42(boolean indicator42) { this.indicator42 = indicator42; }
    public boolean isIndicator43() { return indicator43; }
    public void setIndicator43(boolean indicator43) { this.indicator43 = indicator43; }
}

class ClaimCreationRequest {
    private String pkz;
    private String invoiceNr;
    private String invoiceDate;
    private String orderNr;
    private String area;
    
    // Getters and setters
    public String getPkz() { return pkz; }
    public void setPkz(String pkz) { this.pkz = pkz; }
    public String getInvoiceNr() { return invoiceNr; }
    public void setInvoiceNr(String invoiceNr) { this.invoiceNr = invoiceNr; }
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getOrderNr() { return orderNr; }
    public void setOrderNr(String orderNr) { this.orderNr = orderNr; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}

class ClaimCreationResult {
    private boolean success;
    private String claimNumber;
    private boolean indicator58;
    private boolean indicator60;
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }
    public boolean isIndicator58() { return indicator58; }
    public void setIndicator58(boolean indicator58) { this.indicator58 = indicator58; }
    public boolean isIndicator60() { return indicator60; }
    public void setIndicator60(boolean indicator60) { this.indicator60 = indicator60; }
}