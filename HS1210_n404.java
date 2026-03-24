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
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Service class migrated from RPG subroutine n404 (SB10N and related subroutines).
 * Handles warranty claim processing, subfile management, and invoice operations.
 * 
 * Display File: HS1210D
 * - Manages claim list display with filtering and sorting
 * - Supports multiple views (ascending/descending, by claim/invoice)
 * - Provides selection options: 2=Edit, 4=Delete, 5=Display, 6=Service Card, 8=Warranty Info, 9=Status Change, 10=Send
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
    private HSGSCPRRepository hsgscRepository;
    
    @Autowired
    private HSBTSLRRepository hsbtsRepository;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter EUR_DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    // Working variables
    private String pkz;
    private String filpkz;
    private String status;
    private String zeichen;
    private String filtfg;
    private String filtkd;
    private String filsde;
    private String filart;
    private String filoff;
    private String filmin;
    private Integer filtag;
    private String suchen;
    private String betrieb;
    private Integer tage;
    private Integer tageMax;
    private LocalDate datAkt;
    private String rechte;
    private boolean cwpActive;
    private String axapta;

    /**
     * Subroutine SB10N - Initialize subfile variables
     */
    public void initializeSubfile() {
        // Initialize counters and flags
        int zl4 = 0;
        String sub15x = "";
        
        // Reset indicators 50-58
        // In Java, we use boolean flags instead of RPG indicators
        
        // Handle MARK12 logic
        String mark11 = "";
        String mark12 = "";
        if (mark12 == null || mark12.trim().isEmpty()) {
            mark12 = mark11;
            mark11 = " ";
        }
    }

    /**
     * Subroutine MARK - Handle selection field conversion '1 ' -> ' 1'
     */
    public void handleMarkConversion(String sub010, String mark11, String mark21) {
        String sub01x = sub010;
        String mark12 = "";
        String mark22 = "";
        
        if (mark12 == null || mark12.trim().isEmpty()) {
            mark12 = mark11;
            mark11 = " ";
        }
        
        if (mark22 == null || mark22.trim().isEmpty()) {
            mark22 = mark21;
            mark21 = " ";
        }
    }

    /**
     * Subroutine SB100 - Build and display subfile
     * Processes claims from HSG71LF2 and HSG71PF based on filters
     */
    public List<ClaimSubfileDTO> buildSubfile(boolean ascending, String pkz, ClaimFilterDTO filter) {
        List<ClaimSubfileDTO> subfileRecords = new ArrayList<>();
        int zl1 = 0;
        int zl2 = 0;
        
        // Determine sort direction and read claims
        List<HSG71LF2Entity> claims;
        if (ascending) {
            claims = hsg71Repository.findByPakzOrderByClaimNrAsc(pkz);
        } else {
            claims = hsg71Repository.findByPakzOrderByClaimNrDesc(pkz);
        }
        
        for (HSG71LF2Entity claim : claims) {
            if (zl1 >= 9999) break;
            
            // Apply claim age filter
            if (filter.getFilterDays() != null && filter.getFilterDays() > 0 && claim.getStatusCodeSde() != 99) {
                LocalDate repairDate = parseIsoDate(String.valueOf(claim.getRepDatum()));
                if (repairDate != null) {
                    long daysDiff = ChronoUnit.DAYS.between(repairDate, LocalDate.now());
                    if (daysDiff > filter.getFilterDays()) {
                        claim.setStatusCodeSde(99);
                    }
                }
            }
            
            // Apply claim type filter
            if (filter.getClaimType() != null && !filter.getClaimType().trim().isEmpty() && claim.getStatusCodeSde() != 99) {
                applyClaimTypeFilter(claim, filter.getClaimType());
            }
            
            // Check for open claims
            if ("J".equals(filter.getOpenOnly())) {
                boolean isOpen = checkOpenClaim(claim);
                if (!isOpen) {
                    claim.setStatusCodeSde(99);
                }
            }
            
            // Skip if marked as filtered out
            if (claim.getStatusCodeSde() == 99) {
                continue;
            }
            
            // Build subfile record
            ClaimSubfileDTO subfileRecord = new ClaimSubfileDTO();
            subfileRecord.setPakz(claim.getPakz());
            subfileRecord.setClaimNr(claim.getRechNr());
            subfileRecord.setClaimDate(formatDate(claim.getRechDatum()));
            subfileRecord.setOrderNr(claim.getAuftragsNr());
            subfileRecord.setChassisNr(claim.getChassisNr());
            subfileRecord.setLicensePlate(claim.getKennzeichen());
            subfileRecord.setRepairDate(formatDate(String.valueOf(claim.getRepDatum())));
            subfileRecord.setMileage(claim.getKmStand());
            subfileRecord.setCustomerNr(claim.getKdNr());
            subfileRecord.setCustomerName(claim.getKdName());
            subfileRecord.setClaimNrSde(claim.getClaimNrSde());
            subfileRecord.setStatusCode(claim.getStatusCodeSde());
            
            // Get status description
            String statusText = getStatusDescription(claim.getStatusCodeSde(), claim.getClaimNrSde());
            subfileRecord.setStatusText(statusText);
            
            // Apply status filter
            if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
                if (!matchesStatusFilter(claim.getStatusCodeSde(), filter.getStatus(), filter.getStatusOperator())) {
                    continue;
                }
            }
            
            // Apply search filter
            if (filter.getSearchString() != null && !filter.getSearchString().trim().isEmpty()) {
                if (!matchesSearchString(subfileRecord, filter.getSearchString())) {
                    continue;
                }
            }
            
            // Apply remaining filters
            if (applyRemainingFilters(claim, filter)) {
                // Set color indicators
                setColorIndicators(subfileRecord, claim);
                
                subfileRecords.add(subfileRecord);
                zl1++;
                zl2++;
            }
        }
        
        // If no records found, add empty indicator record
        if (subfileRecords.isEmpty()) {
            ClaimSubfileDTO emptyRecord = new ClaimSubfileDTO();
            emptyRecord.setStatusText("No records found");
            subfileRecords.add(emptyRecord);
        }
        
        return subfileRecords;
    }

    /**
     * Subroutine SR102 - Edit claim
     */
    public void editClaim(String pakz, String claimNr, String orderNr, String claimDate, String orderDate) {
        // Call HS1220 program
        callHS1220(pakz, claimNr);
        
        // Call HS1212 program with edit mode
        callHS1212("2", pakz, claimNr, claimDate, orderNr, orderDate);
    }

    /**
     * Subroutine SR104 - Delete claims
     */
    public void deleteClaims(List<ClaimSubfileDTO> selectedClaims) {
        for (ClaimSubfileDTO claim : selectedClaims) {
            if (" 4".equals(claim.getSelection())) {
                // Find and mark claim as deleted
                Optional<HSG71LF2Entity> claimOpt = hsg71Repository.findByPakzAndRechNr(claim.getPakz(), claim.getClaimNr());
                if (claimOpt.isPresent()) {
                    HSG71LF2Entity claimEntity = claimOpt.get();
                    claimEntity.setStatusCodeSde(99);
                    hsg71Repository.save(claimEntity);
                    
                    // Delete associated error records
                    List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(claim.getPakz(), claim.getClaimNr());
                    hsg73Repository.deleteAll(errors);
                }
            }
        }
    }

    /**
     * Subroutine SR105 - Display claim
     */
    public void displayClaim(String pakz, String claimNr, String orderNr, String claimDate, String orderDate) {
        // Call HS1220 program
        callHS1220(pakz, claimNr);
        
        // Call HS1212 program with display mode
        callHS1212("5", pakz, claimNr, claimDate, orderNr, orderDate);
    }

    /**
     * Subroutine SR106 - Print service card
     */
    public void printServiceCard(String licensePlate, String printOption) {
        String fgnr = String.format("%8s", licensePlate).replace(' ', '0');
        
        // Call HS0240C program based on print option
        switch (printOption) {
            case "A": // Display
            case "D": // Print
            case "Z": // Print to file
            case "C": // Copy
                callHS0240C(fgnr, printOption);
                break;
        }
    }

    /**
     * Subroutine SR108 - Display warranty info
     */
    public void displayWarrantyInfo(String licensePlate) {
        String fgnr17 = licensePlate;
        callHS0069C(fgnr17);
    }

    /**
     * Subroutine SR109 - Change status
     */
    public void changeStatus(String pakz, String claimNr) {
        Optional<HSG71LF2Entity> claimOpt = hsg71Repository.findByPakzAndRechNr(pakz, claimNr);
        if (claimOpt.isPresent()) {
            HSG71LF2Entity claim = claimOpt.get();
            if (claim.getStatusCodeSde() == 2) {
                // Prompt user and update status to 3
                claim.setStatusCodeSde(3);
                hsg71Repository.save(claim);
            }
        }
    }

    /**
     * Subroutine SR110 - Send claim directly
     */
    public void sendClaim(String pakz, String claimNr) {
        sendClaimInternal(pakz, claimNr, "", "");
    }

    /**
     * Subroutine SR110Alt - Alternative send claim with validation
     */
    public void sendClaimAlternative(String pakz, String claimNr) {
        boolean changed = false;
        
        Optional<HSG71LF2Entity> claimOpt = hsg71Repository.findByPakzAndRechNr(pakz, claimNr);
        if (claimOpt.isPresent()) {
            HSG71LF2Entity claim = claimOpt.get();
            
            if ((claim.getStatusCodeSde() == 2 || claim.getStatusCodeSde() == 3 || claim.getStatusCodeSde() > 9) 
                && !"00000000".equals(claim.getClaimNrSde())) {
                
                // Update status to 3
                claim.setStatusCodeSde(3);
                hsg71Repository.save(claim);
                
                // Process errors
                List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(pakz, claimNr);
                for (HSG73PFEntity error : errors) {
                    if (error.getStatusCode() == 0) {
                        if (isWarrantyScope(error.getDemandCode())) {
                            if (checkClaim()) {
                                callWPSC01(error.getPakz(), error.getClaimNr(), error.getFehlerNr(), error.getFolgeNr());
                                changed = true;
                            }
                        } else {
                            callHS1219C1("ACTION", pakz, claimNr, "RW_PARM");
                            changed = true;
                        }
                    }
                }
                
                if (changed) {
                    claim.setStatusCodeSde(10);
                    hsg71Repository.save(claim);
                    
                    if (isWarrantyScope("")) {
                        callHS1220(pakz, claimNr);
                    }
                }
            }
        }
    }

    /**
     * Subroutine SR04 - User guidance / selection list
     */
    public List<SelectionListDTO> buildSelectionList(String listType, String pakz, String splitt) {
        List<SelectionListDTO> selectionList = new ArrayList<>();
        int zla = 0;
        
        switch (listType) {
            case "HS1210W9": // Status list
                List<HSGSCPREntity> statusList = hsgscRepository.findAll();
                for (HSGSCPREntity status : statusList) {
                    SelectionListDTO item = new SelectionListDTO();
                    item.setCode(status.getStatusCode());
                    item.setDescription(status.getStatusDescription());
                    selectionList.add(item);
                    zla++;
                }
                break;
                
            case "HS1210W6": // Invoice list
                List<HSAHKLF3Entity> invoices = hsahkRepository.findByPakzAndSplitt(pkz, splitt);
                for (HSAHKLF3Entity invoice : invoices) {
                    // Skip if marked as deleted
                    if ("99999999".equals(invoice.getGaUebern())) {
                        continue;
                    }
                    
                    // Check date validity
                    String repairDate = invoice.getWt().equals("1") ? invoice.getFertTag() : invoice.getAdat();
                    if (!isValidDate(repairDate)) {
                        continue;
                    }
                    
                    // Check if within date range
                    LocalDate repDate = parseIsoDate(repairDate);
                    if (repDate != null && repDate.plusDays(tage).isBefore(datAkt)) {
                        continue;
                    }
                    
                    // Check if already processed
                    if (isAlreadyProcessed(invoice)) {
                        continue;
                    }
                    
                    SelectionListDTO item = new SelectionListDTO();
                    item.setInvoiceNr(invoice.getRnr());
                    item.setInvoiceDate(formatDate(invoice.getRdat()));
                    item.setOrderNr(invoice.getAnr());
                    item.setArea(invoice.getBerei());
                    item.setWt(invoice.getWt());
                    item.setSplitt(invoice.getSplitt());
                    item.setLicensePlate(invoice.getFahrgNr());
                    item.setCustomerNr(invoice.getKundenNr());
                    selectionList.add(item);
                    zla++;
                }
                break;
                
            case "BETRIEB":
            case "BETRIEB9": // Branch list
                List<HSBTSLREntity> branches = hsbtsRepository.findByPakz(pkz);
                for (HSBTSLREntity branch : branches) {
                    SelectionListDTO item = new SelectionListDTO();
                    item.setCode(branch.getBranchCode());
                    item.setDescription(branch.getBranchName());
                    selectionList.add(item);
                    zla++;
                }
                break;
        }
        
        return selectionList;
    }

    /**
     * Subroutine SR06 - Create new claim
     */
    public String createClaim(String invoiceNr, String invoiceDate, String orderNr, String area, String wt, String splitt) {
        // Initialize variables
        String neu1 = "";
        String neu2 = "";
        BigDecimal neu2x = BigDecimal.ZERO;
        String neu3 = "";
        String neu4 = "1";
        String neuwt = "1";
        String newSplitt = "04";
        
        // Validate and process invoice data
        if (invoiceNr == null || invoiceNr.trim().isEmpty()) {
            return null;
        }
        
        // Check if already processed
        List<HSG71LF2Entity> existingClaims = hsg71Repository.findByRechNr(invoiceNr);
        for (HSG71LF2Entity existing : existingClaims) {
            if (existing.getStatusCodeSde() != 99) {
                return null; // Already processed
            }
        }
        
        // Get invoice data
        Optional<HSAHKLF3Entity> invoiceOpt = hsahkRepository.findByPakzAndRnr(pkz, invoiceNr);
        if (!invoiceOpt.isPresent()) {
            return null;
        }
        
        HSAHKLF3Entity invoice = invoiceOpt.get();
        
        // Create claim record
        HSG71LF2Entity claim = new HSG71LF2Entity();
        claim.setPakz(invoice.getPakz());
        claim.setRechNr(invoice.getRnr());
        claim.setRechDatum(invoice.getRdat());
        claim.setAuftragsNr(invoice.getAnr());
        claim.setWete(invoice.getWt());
        
        // Set chassis number
        String chassisNr = invoice.getFahrgNr();
        if (chassisNr != null && chassisNr.length() > 7) {
            chassisNr = chassisNr.substring(chassisNr.length() - 7);
        }
        claim.setChassisNr(chassisNr);
        
        // Set license plate
        claim.setKennzeichen(invoice.getKz());
        
        // Set registration date
        String regDate = invoice.getZdat();
        claim.setZulDatum(parseDecimalDate(regDate));
        
        // Set repair date
        String repairDate = invoice.getWt().equals("1") ? invoice.getFertTag() : invoice.getAdat();
        claim.setRepDatum(parseDecimalDate(repairDate));
        
        // Calculate mileage
        try {
            BigDecimal mileage = new BigDecimal(invoice.getKm());
            claim.setKmStand(mileage.divide(new BigDecimal(1000)).intValue());
        } catch (Exception e) {
            claim.setKmStand(0);
        }
        
        // Set product type
        claim.setProduktTyp(determineProductType(chassisNr));
        
        // Set customer data
        claim.setKdNr(invoice.getKundenNr());
        claim.setKdName(invoice.getName());
        
        // Initialize claim number and status
        claim.setClaimNrSde("");
        claim.setStatusCodeSde(0);
        claim.setAnzFehler(0);
        claim.setBereich(invoice.getBerei());
        claim.setAufNr(invoice.getAnr() + invoice.getBerei() + invoice.getWt() + invoice.getSplitt());
        
        // Generate new claim number
        String newClaimNr = generateClaimNumber(pkz);
        claim.setRechNr(newClaimNr);
        
        // Save claim
        hsg71Repository.save(claim);
        
        // Copy parts and labor from invoice
        copyPartsFromInvoice(claim, invoice);
        copyLaborFromInvoice(claim, invoice);
        copyExternalServicesFromInvoice(claim, invoice);
        
        // Add attachments
        addAttachments(invoice.getWktId(), claim.getPakz(), claim.getRechNr(), "01");
        
        // Renumber positions
        reNumberPositions(claim.getPakz(), claim.getRechNr());
        
        return newClaimNr;
    }

    /**
     * Subroutine SR09 - List navigation
     */
    public Integer navigateToListPosition(String searchValue, List<ClaimSubfileDTO> subfile, boolean ascending) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return null;
        }
        
        for (int i = 0; i < subfile.size(); i++) {
            ClaimSubfileDTO record = subfile.get(i);
            String compareValue = record.getClaimNr();
            
            if (ascending && searchValue.compareTo(compareValue) <= 0) {
                return i + 1;
            } else if (!ascending && searchValue.compareTo(compareValue) >= 0) {
                return i + 1;
            }
        }
        
        return null;
    }

    /**
     * Subroutine SR11 - Toggle subfile view
     */
    public void toggleSubfileView(String currentView) {
        // Toggle between two subfile views
        // Implementation depends on UI framework
    }

    /**
     * Subroutine SR16 - Change sort order
     */
    public String changeSortOrder(String currentSort) {
        if ("CLAIM".equals(currentSort)) {
            return "INVOICE";
        } else {
            return "CLAIM";
        }
    }

    /**
     * Subroutine SR17 - Toggle ascending/descending
     */
    public boolean toggleSortDirection(boolean currentAscending) {
        return !currentAscending;
    }

    /**
     * Subroutine SR19 - Set filter
     */
    public ClaimFilterDTO setFilter(ClaimFilterDTO filter) {
        // Validate and process filter settings
        if ("00000000".equals(filter.getClaimNrSde())) {
            filter.setClaimNrSde("");
        }
        
        if ("J".equals(filter.getMinimumOnly())) {
            filter.setClaimNrSde("00000000");
        }
        
        if (filter.getBranchCode() == null || filter.getBranchCode().trim().isEmpty()) {
            filter.setBranchCode(pkz);
        }
        
        return filter;
    }

    /**
     * Subroutine SR20 - Process all open invoices
     */
    public int processAllOpenInvoices(String pkz, String splitt) {
        int count = 0;
        
        List<HSAHKLF3Entity> invoices = hsahkRepository.findByPakzAndSplitt(pkz, splitt);
        for (HSAHKLF3Entity invoice : invoices) {
            // Check date validity
            String repairDate = invoice.getWt().equals("1") ? invoice.getFertTag() : invoice.getAdat();
            if (!isValidDate(repairDate)) {
                continue;
            }
            
            // Check if within date range
            LocalDate repDate = parseIsoDate(repairDate);
            if (repDate != null && repDate.plusDays(tage).isBefore(datAkt)) {
                continue;
            }
            
            // Create claim
            String claimNr = createClaim(invoice.getRnr(), invoice.getRdat(), invoice.getAnr(), 
                                        invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
            if (claimNr != null) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * Subroutine SRDAT - Format date
     */
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() != 8) {
            return "";
        }
        
        try {
            String day = isoDate.substring(6, 8);
            String month = isoDate.substring(4, 6);
            String year = isoDate.substring(0, 4);
            return day + "." + month + "." + year;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Subroutine SRERR1 - Error handling for invalid selections
     */
    public List<String> validateSelections(List<ClaimSubfileDTO> selectedClaims) {
        List<String> errors = new ArrayList<>();
        
        for (ClaimSubfileDTO claim : selectedClaims) {
            String selection = claim.getSelection();
            if (selection == null || selection.trim().isEmpty()) {
                continue;
            }
            
            // Validate selection codes
            if (!" 2".equals(selection) && !" 4".equals(selection) && !" 5".equals(selection) &&
                !" 6".equals(selection) && !" 8".equals(selection) && !" 9".equals(selection) &&
                !"10".equals(selection)) {
                errors.add("Invalid selection code for claim " + claim.getClaimNr());
            }
            
            // Check if delete is allowed
            if (" 4".equals(selection)) {
                if (claim.getStatusCode() > 3) {
                    List<HSG73PFEntity> errors2 = hsg73Repository.findByPakzAndRechNr(claim.getPakz(), claim.getClaimNr());
                    if (!errors2.isEmpty() || claim.getStatusCode() == 20) {
                        errors.add("Delete not allowed for claim " + claim.getClaimNr());
                    }
                }
            }
        }
        
        return errors;
    }

    /**
     * Subroutine SR_FARBE - Set color indicators based on claim status
     */
    private void setColorIndicators(ClaimSubfileDTO subfile, HSG71LF2Entity claim) {
        boolean red = false;
        boolean yellow = false;
        boolean blue = false;
        
        if (!"00000000".equals(claim.getClaimNrSde())) {
            List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(claim.getPakz(), claim.getRechNr());
            
            if (errors.isEmpty() && claim.getStatusCodeSde() == 20) {
                red = true;
            }
            
            for (HSG73PFEntity error : errors) {
                // Red for errors
                if (error.getStatusCode() == 16 || error.getStatusCode() == 30 || 
                    (error.getStatusCode() == 0 && claim.getClaimNrSde() != null)) {
                    red = true;
                }
                
                // Yellow for rejections
                if (error.getStatusCode() == 11) {
                    yellow = true;
                }
                
                // Blue for waiting
                if (error.getStatusCode() == 3 || error.getStatusCode() == 11) {
                    blue = true;
                }
            }
        }
        
        subfile.setRedIndicator(red);
        subfile.setYellowIndicator(yellow);
        subfile.setBlueIndicator(blue);
        
        String color = "";
        if (red) color = "ROT";
        if (yellow) color += (color.isEmpty() ? "" : " ") + "GELB";
        if (blue) color += (color.isEmpty() ? "" : " ") + "BLAU";
        subfile.setColorIndicator(color);
    }

    /**
     * Subroutine SR_MINIMUM - Process minimum claim
     */
    public void processMinimumClaim(String pakz, String claimNr) {
        Optional<HSG71LF2Entity> claimOpt = hsg71Repository.findByPakzAndRechNr(pakz, claimNr);
        if (claimOpt.isPresent()) {
            HSG71LF2Entity claim = claimOpt.get();
            
            if (claim.getStatusCodeSde() == 5) {
                // Update status to 20 (minimum posted)
                claim.setStatusCodeSde(20);
                hsg71Repository.save(claim);
                
                // Call posting program
                if ("0".equals(axapta)) {
                    callHS1219M("ACTION", claimNr);
                } else {
                    // Call Axapta interface
                    callAX9999C(pakz, claimNr);
                }
            }
        }
    }

    /**
     * Subroutine SR_G70 - Request release for submission deadline
     */
    public void requestDeadlineRelease(String invoiceNr, String invoiceDate, String licensePlate) {
        if (invoiceDate == null || invoiceDate.isEmpty() || invoiceNr == null || invoiceNr.isEmpty()) {
            return;
        }
        
        Optional<HSG70FEntity> releaseOpt = hsg70Repository.findByKzlAndRNrAndRDat(pkz, invoiceNr, invoiceDate);
        if (!releaseOpt.isPresent()) {
            HSG70FEntity release = new HSG70FEntity();
            release.setKzl(pkz);
            release.setRNr(invoiceNr);
            release.setRDat(invoiceDate);
            release.setFgnr(licensePlate);
            release.setRepDat("");
            release.setStatus("");
            release.setCusNo(BigDecimal.ZERO);
            release.setDcNo(BigDecimal.ZERO);
            release.setDcFn("");
            
            hsg70Repository.save(release);
        }
    }

    /**
     * Subroutine SR_DMC - Get demand code
     */
    private String getDemandCode(String pakz, String claimNr) {
        List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(pakz, claimNr);
        if (!errors.isEmpty()) {
            return errors.get(0).getDemandCode();
        }
        return "";
    }

    /**
     * Subroutine SR_FILART - Apply claim type filter
     */
    private void applyClaimTypeFilter(HSG71LF2Entity claim, String filterType) {
        boolean found = false;
        
        List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(claim.getPakz(), claim.getRechNr());
        for (HSG73PFEntity error : errors) {
            String demandCode = error.getDemandCode();
            
            switch (filterType) {
                case "K": // Goodwill
                    if (isGoodwillScope(demandCode)) {
                        found = true;
                    }
                    break;
                case "G": // Warranty
                    if (isWarrantyScope(demandCode)) {
                        found = true;
                    }
                    break;
                default:
                    String scope = getScope(demandCode);
                    if (scope != null && scope.startsWith(filterType)) {
                        found = true;
                    }
                    break;
            }
            
            if (found) break;
        }
        
        if (!found) {
            claim.setStatusCodeSde(99);
        }
    }

    /**
     * Subroutine AxBranchSR - Get Axapta branch information
     */
    private void getAxaptaBranch(String kzl) {
        // Call AX9000 program to get branch information
        // Implementation depends on Axapta interface
    }

    // Helper methods

    private LocalDate parseIsoDate(String isoDate) {
        try {
            return LocalDate.parse(isoDate, ISO_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseDecimalDate(String dateStr) {
        try {
            return Integer.parseInt(dateStr);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, ISO_DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getStatusDescription(Integer statusCode, String claimNrSde) {
        if ("00000000".equals(claimNrSde)) {
            if (statusCode == 5) {
                return "Minimumantrag";
            } else if (statusCode == 20) {
                return "Minimum ausgebucht";
            } else {
                return "Minimumantrag";
            }
        }
        
        Optional<HSGSCPREntity> statusOpt = hsgscRepository.findByStatusCode(String.valueOf(statusCode));
        return statusOpt.map(HSGSCPREntity::getStatusDescription).orElse("");
    }

    private boolean matchesStatusFilter(Integer statusCode, String filterStatus, String operator) {
        try {
            int filterValue = Integer.parseInt(filterStatus);
            
            if ("=".equals(operator) || "*".equals(operator)) {
                return statusCode == filterValue;
            } else if (">".equals(operator)) {
                return statusCode > filterValue;
            } else if ("<".equals(operator)) {
                return statusCode < filterValue;
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }

    private boolean matchesSearchString(ClaimSubfileDTO record, String searchString) {
        String searchUpper = searchString.toUpperCase();
        String recordData = (record.getPakz() + record.getOrderNr() + record.getClaimDate() + 
                           record.getClaimNrSde() + record.getChassisNr() + record.getClaimNr() +
                           record.getRepairDate() + record.getLicensePlate() + record.getCustomerNr() +
                           record.getCustomerName() + record.getStatusText()).toUpperCase();
        
        return recordData.contains(searchUpper);
    }

    private boolean applyRemainingFilters(HSG71LF2Entity claim, ClaimFilterDTO filter) {
        if (filter.getBranchCode() != null && !filter.getBranchCode().equals(claim.getPakz())) {
            return false;
        }
        
        if (filter.getLicensePlate() != null && !filter.getLicensePlate().isEmpty() &&
            !filter.getLicensePlate().equals(claim.getKennzeichen())) {
            return false;
        }
        
        if (filter.getCustomerNr() != null && !filter.getCustomerNr().isEmpty() &&
            !filter.getCustomerNr().equals(claim.getKdNr())) {
            return false;
        }
        
        if (filter.getClaimNrSde() != null && !filter.getClaimNrSde().isEmpty() &&
            !filter.getClaimNrSde().equals(claim.getClaimNrSde())) {
            return false;
        }
        
        return true;
    }

    private boolean checkOpenClaim(HSG71LF2Entity claim) {
        if (claim.getStatusCodeSde() < 20 && claim.getStatusCodeSde() != 5) {
            return true;
        }
        
        List<HSG73PFEntity> errors = hsg73Repository.findByPakzAndRechNr(claim.getPakz(), claim.getRechNr());
        if (errors.isEmpty()) {
            return true;
        }
        
        for (HSG73PFEntity error : errors) {
            if (error.getStatusCode() == 0) {
                return true;
            }
        }
        
        return false;
    }

    private boolean isAlreadyProcessed(HSAHKLF3Entity invoice) {
        List<HSG71LF2Entity> claims = hsg71Repository.findByRechNr(invoice.getRnr());
        for (HSG71LF2Entity claim : claims) {
            if (claim.getStatusCodeSde() != 99) {
                return true;
            }
        }
        return false;
    }

    private int determineProductType(String chassisNr) {
        // Determine product type based on chassis number
        // Default to 1 (truck)
        return 1;
    }

    private String generateClaimNumber(String pakz) {
        // Generate next available claim number
        // Implementation depends on numbering scheme
        return "00001";
    }

    private void copyPartsFromInvoice(HSG71LF2Entity claim, HSAHKLF3Entity invoice) {
        // Copy parts from invoice to claim
        // Implementation depends on parts table structure
    }

    private void copyLaborFromInvoice(HSG71LF2Entity claim, HSAHKLF3Entity invoice) {
        // Copy labor from invoice to claim
        // Implementation depends on labor table structure
    }

    private void copyExternalServicesFromInvoice(HSG71LF2Entity claim, HSAHKLF3Entity invoice) {
        // Copy external services from invoice to claim
        List<HSFLALF1Entity> services = hsflaRepository.findByPkzAndAufnr(invoice.getPakz(), invoice.getAnr());
        for (HSFLALF1Entity service : services) {
            if (service.getStatus().compareTo("3") > 0) {
                // Create position record
                // Implementation depends on position table structure
            }
        }
    }

    private void addAttachments(BigDecimal wktId, String pakz, String claimNr, String type) {
        // Add attachments to claim
        // Implementation depends on attachment handling
    }

    private void reNumberPositions(String pakz, String claimNr) {
        // Renumber positions sequentially
        // Implementation depends on position table structure
    }

    private boolean isWarrantyScope(String demandCode) {
        // Check if demand code is warranty scope
        return demandCode != null && demandCode.startsWith("W");
    }

    private boolean isGoodwillScope(String demandCode) {
        // Check if demand code is goodwill scope
        return demandCode != null && demandCode.startsWith("K");
    }

    private String getScope(String demandCode) {
        // Get scope from demand code
        if (demandCode != null && !demandCode.isEmpty()) {
            return demandCode.substring(0, 1);
        }
        return "";
    }

    private boolean checkClaim() {
        // Check claim validity
        return true;
    }

    // External program calls (to be implemented based on actual interfaces)
    
    private void callHS1220(String pakz, String claimNr) {
        // Call HS1220 program
    }

    private void callHS1212(String mode, String pakz, String claimNr, String claimDate, String orderNr, String orderDate) {
        // Call HS1212 program
    }

    private void callHS0240C(String fgnr, String option) {
        // Call HS0240C program
    }

    private void callHS0069C(String fgnr17) {
        // Call HS0069C program
    }

    private void sendClaimInternal(String pakz, String claimNr, String param1, String param2) {
        // Send claim implementation
    }

    private void callWPSC01(String pakz, String claimNr, String errorNr, String sequenceNr) {
        // Call WP_SC01 program
    }

    private void callHS1219C1(String action, String pakz, String claimNr, String param) {
        // Call HS1219C1 program
    }

    private void callHS1219M(String action, String claimNr) {
        // Call HS1219M program
    }

    private void callAX9999C(String pakz, String claimNr) {
        // Call AX9999C program
    }
}

// DTOs for data transfer

class ClaimSubfileDTO {
    private String selection;
    private String pakz;
    private String claimNr;
    private String claimDate;
    private String orderNr;
    private String chassisNr;
    private String licensePlate;
    private String repairDate;
    private Integer mileage;
    private String customerNr;
    private String customerName;
    private String claimNrSde;
    private Integer statusCode;
    private String statusText;
    private boolean redIndicator;
    private boolean yellowIndicator;
    private boolean blueIndicator;
    private String colorIndicator;
    
    // Getters and setters
    public String getSelection() { return selection; }
    public void setSelection(String selection) { this.selection = selection; }
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getClaimNr() { return claimNr; }
    public void setClaimNr(String claimNr) { this.claimNr = claimNr; }
    public String getClaimDate() { return claimDate; }
    public void setClaimDate(String claimDate) { this.claimDate = claimDate; }
    public String getOrderNr() { return orderNr; }
    public void setOrderNr(String orderNr) { this.orderNr = orderNr; }
    public String getChassisNr() { return chassisNr; }
    public void setChassisNr(String chassisNr) { this.chassisNr = chassisNr; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getRepairDate() { return repairDate; }
    public void setRepairDate(String repairDate) { this.repairDate = repairDate; }
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public String getCustomerNr() { return customerNr; }
    public void setCustomerNr(String customerNr) { this.customerNr = customerNr; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getClaimNrSde() { return claimNrSde; }
    public void setClaimNrSde(String claimNrSde) { this.claimNrSde = claimNrSde; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public boolean isRedIndicator() { return redIndicator; }
    public void setRedIndicator(boolean redIndicator) { this.redIndicator = redIndicator; }
    public boolean isYellowIndicator() { return yellowIndicator; }
    public void setYellowIndicator(boolean yellowIndicator) { this.yellowIndicator = yellowIndicator; }
    public boolean isBlueIndicator() { return blueIndicator; }
    public void setBlueIndicator(boolean blueIndicator) { this.blueIndicator = blueIndicator; }
    public String getColorIndicator() { return colorIndicator; }
    public void setColorIndicator(String colorIndicator) { this.colorIndicator = colorIndicator; }
}

class ClaimFilterDTO {
    private String status;
    private String statusOperator;
    private String licensePlate;
    private String customerNr;
    private String claimNrSde;
    private String branchCode;
    private String claimType;
    private String openOnly;
    private String minimumOnly;
    private Integer filterDays;
    private String searchString;
    
    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusOperator() { return statusOperator; }
    public void setStatusOperator(String statusOperator) { this.statusOperator = statusOperator; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getCustomerNr() { return customerNr; }
    public void setCustomerNr(String customerNr) { this.customerNr = customerNr; }
    public String getClaimNrSde() { return claimNrSde; }
    public void setClaimNrSde(String claimNrSde) { this.claimNrSde = claimNrSde; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }
    public String getOpenOnly() { return openOnly; }
    public void setOpenOnly(String openOnly) { this.openOnly = openOnly; }
    public String getMinimumOnly() { return minimumOnly; }
    public void setMinimumOnly(String minimumOnly) { this.minimumOnly = minimumOnly; }
    public Integer getFilterDays() { return filterDays; }
    public void setFilterDays(Integer filterDays) { this.filterDays = filterDays; }
    public String getSearchString() { return searchString; }
    public void setSearchString(String searchString) { this.searchString = searchString; }
}

class SelectionListDTO {
    private String code;
    private String description;
    private String invoiceNr;
    private String invoiceDate;
    private String orderNr;
    private String area;
    private String wt;
    private String splitt;
    private String licensePlate;
    private String customerNr;
    
    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInvoiceNr() { return invoiceNr; }
    public void setInvoiceNr(String invoiceNr) { this.invoiceNr = invoiceNr; }
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getOrderNr() { return orderNr; }
    public void setOrderNr(String orderNr) { this.orderNr = orderNr; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getWt() { return wt; }
    public void setWt(String wt) { this.wt = wt; }
    public String getSplitt() { return splitt; }
    public void setSplitt(String splitt) { this.splitt = splitt; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getCustomerNr() { return customerNr; }
    public void setCustomerNr(String customerNr) { this.customerNr = customerNr; }
}

// JPA Entity classes

@Entity
@Table(name = "HSFLALF1")
class HSFLALF1Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PKZ", length = 3)
    private String pkz;
    
    @Column(name = "BES-DAT", length = 8)
    private String besdat;
    
    @Column(name = "BES-NR", length = 5)
    private String besnr;
    
    @Column(name = "LNR-FL", precision = 3, scale = 0)
    private Integer lnrfl;
    
    @Column(name = "KEN-FL", length = 8)
    private String kenfl;
    
    @Column(name = "LNR", precision = 3, scale = 0)
    private Integer lnr;
    
    @Column(name = "BESCHREIBUNG", length = 40)
    private String beschreibung;
    
    @Column(name = "TEXTZEILEN", precision = 3, scale = 0)
    private Integer textzeilen;
    
    @Column(name = "EK-PREIS", precision = 7, scale = 2)
    private BigDecimal ekpreis;
    
    @Column(name = "MENGE", precision = 5, scale = 0)
    private Integer menge;
    
    @Column(name = "EK-RENR", length = 10)
    private String ekrenr;
    
    @Column(name = "EK-REDAT", length = 8)
    private String ekredat;
    
    @Column(name = "EK BEMERKUNGEN 1", length = 60)
    private String ekbemerkungen1;
    
    @Column(name = "EK BEMERKUNGEN 2", length = 60)
    private String ekbemerkungen2;
    
    @Column(name = "EK-WERT", precision = 9, scale = 2)
    private BigDecimal ekwert;
    
    @Column(name = "ZUS %", length = 3)
    private String zusprozent;
    
    @Column(name = "VK-WERT", precision = 9, scale = 2)
    private BigDecimal vkwert;
    
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
    private String erlgrp;
    
    @Column(name = "RECNR", length = 5)
    private String recnr;
    
    @Column(name = "RECDAT", length = 8)
    private String recdat;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "SDPS JOB UUID", length = 40)
    private String sdpsjobuuid;
    
    @Column(name = "SDPS FLA UUID", length = 40)
    private String sdpsflauuid;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPkz() { return pkz; }
    public void setPkz(String pkz) { this.pkz = pkz; }
    public String getBesdat() { return besdat; }
    public void setBesdat(String besdat) { this.besdat = besdat; }
    public String getBesnr() { return besnr; }
    public void setBesnr(String besnr) { this.besnr = besnr; }
    public Integer getLnrfl() { return lnrfl; }
    public void setLnrfl(Integer lnrfl) { this.lnrfl = lnrfl; }
    public String getKenfl() { return kenfl; }
    public void setKenfl(String kenfl) { this.kenfl = kenfl; }
    public Integer getLnr() { return lnr; }
    public void setLnr(Integer lnr) { this.lnr = lnr; }
    public String getBeschreibung() { return beschreibung; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }
    public Integer getTextzeilen() { return textzeilen; }
    public void setTextzeilen(Integer textzeilen) { this.textzeilen = textzeilen; }
    public BigDecimal getEkpreis() { return ekpreis; }
    public void setEkpreis(BigDecimal ekpreis) { this.ekpreis = ekpreis; }
    public Integer getMenge() { return menge; }
    public void setMenge(Integer menge) { this.menge = menge; }
    public String getEkrenr() { return ekrenr; }
    public void setEkrenr(String ekrenr) { this.ekrenr = ekrenr; }
    public String getEkredat() { return ekredat; }
    public void setEkredat(String ekredat) { this.ekredat = ekredat; }
    public String getEkbemerkungen1() { return ekbemerkungen1; }
    public void setEkbemerkungen1(String ekbemerkungen1) { this.ekbemerkungen1 = ekbemerkungen1; }
    public String getEkbemerkungen2() { return ekbemerkungen2; }
    public void setEkbemerkungen2(String ekbemerkungen2) { this.ekbemerkungen2 = ekbemerkungen2; }
    public BigDecimal getEkwert() { return ekwert; }
    public void setEkwert(BigDecimal ekwert) { this.ekwert = ekwert; }
    public String getZusprozent() { return zusprozent; }
    public void setZusprozent(String zusprozent) { this.zusprozent = zusprozent; }
    public BigDecimal getVkwert() { return vkwert; }
    public void setVkwert(BigDecimal vkwert) { this.vkwert = vkwert; }
    public String getAufnr() { return aufnr; }
    public void setAufnr(String aufnr) { this.aufnr = aufnr; }
    public String getBerei() { return berei; }
    public void setBerei(String berei) { this.berei = berei; }
    public String getWt() { return wt; }
    public void setWt(String wt) { this.wt = wt; }
    public String getSpl() { return spl; }
    public void setSpl(String spl) { this.spl = spl; }
    public String getAufdat() { return aufdat; }
    public void setAufdat(String aufdat) { this.aufdat = aufdat; }
    public Integer getPos() { return pos; }
    public void setPos(Integer pos) { this.pos = pos; }
    public String getErlgrp() { return erlgrp; }
    public void setErlgrp(String erlgrp) { this.erlgrp = erlgrp; }
    public String getRecnr() { return recnr; }
    public void setRecnr(String recnr) { this.recnr = recnr; }
    public String getRecdat() { return recdat; }
    public void setRecdat(String recdat) { this.recdat = recdat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSdpsjobuuid() { return sdpsjobuuid; }
    public void setSdpsjobuuid(String sdpsjobuuid) { this.sdpsjobuuid = sdpsjobuuid; }
    public String getSdpsflauuid() { return sdpsflauuid; }
    public void setSdpsflauuid(String sdpsflauuid) { this.sdpsflauuid = sdpsflauuid; }
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
    private String rgnr10a;
    
    @Column(name = "RDAT", length = 8)
    private String rdat;
    
    @Column(name = "KZ S", length = 1)
    private String kzs;
    
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
    private String lrnr;
    
    @Column(name = "STO-BEZ-RE", length = 5)
    private String stobezre;
    
    @Column(name = "STO-BEZ-REDAT", length = 8)
    private String stobezredat;
    
    @Column(name = "KOR-BEZ-RE", length = 5)
    private String korbezre;
    
    @Column(name = "KOR-BEZ-REDAT", length = 8)
    private String korbezredat;
    
    @Column(name = "BFORT", length = 1)
    private String bfort;
    
    @Column(name = "MWST Y/N", length = 1)
    private String mwstyn;
    
    @Column(name = "MWST %", precision = 5, scale = 2)
    private BigDecimal mwstprozent;
    
    @Column(name = "MWST % R.", precision = 5, scale = 2)
    private BigDecimal mwstprozentr;
    
    @Column(name = "BA-SCHL�SSEL", length = 2)
    private String baschluessel;
    
    @Column(name = "KST LOHN", length = 5)
    private String kstlohn;
    
    @Column(name = "KST TEILE", length = 5)
    private String kstteile;
    
    @Column(name = "FIBU MWST", length = 6)
    private String fibumwst;
    
    @Column(name = "FIBU MWST AT", length = 6)
    private String fibumwstat;
    
    @Column(name = "FIBU INTERIM", length = 6)
    private String fibuinterim;
    
    @Column(name = "KTO INTAUF.", length = 6)
    private String ktointauf;
    
    @Column(name = "KTR INT AUF.", length = 7)
    private String ktrintauf;
    
    @Column(name = "KST INT AUF.", length = 5)
    private String kstintauf;
    
    @Column(name = "SPEZ-CODE", length = 10)
    private String spezcode;
    
    @Column(name = "BRANCH", length = 3)
    private String branch;
    
    @Column(name = "PROD-CODE", length = 10)
    private String prodcode;
    
    @Column(name = "PROJEKT", length = 10)
    private String projekt;
    
    @Column(name = "DOKUMENTENNUMMER", length = 20)
    private String dokumentennummer;
    
    @Column(name = "KOSTENCODE KONZINT.", length = 3)
    private String kostencodekonzint;
    
    @Column(name = "KUNDEN-NR.", length = 6)
    private String kundennr;
    
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
    private String bestellerkunde;
    
    @Column(name = "VALUTA", length = 1)
    private String valuta;
    
    @Column(name = "BONIT#T", length = 1)
    private String bonitaet;
    
    @Column(name = "ZAHLUNGSART", length = 1)
    private String zahlungsart;
    
    @Column(name = "RC", length = 3)
    private String rc;
    
    @Column(name = "RE KUNDEN-NR.", length = 6)
    private String rekundennr;
    
    @Column(name = "RE ANREDE", length = 1)
    private String reanrede;
    
    @Column(name = "RE NAME", length = 30)
    private String rename;
    
    @Column(name = "RE BRANCHE", length = 25)
    private String rebranche;
    
    @Column(name = "RE MATCH", length = 5)
    private String rematch;
    
    @Column(name = "RE STRASSE", length = 25)
    private String restrasse;
    
    @Column(name = "RE LAND", length = 3)
    private String reland;
    
    @Column(name = "RE PLZ", length = 5)
    private String replz;
    
    @Column(name = "RE ORT", length = 20)
    private String reort;
    
    @Column(name = "RE TELE.", length = 17)
    private String retele;
    
    @Column(name = "RE VALUTA", length = 1)
    private String revaluta;
    
    @Column(name = "RE BONIT#T", length = 1)
    private String rebonitaet;
    
    @Column(name = "RE ZART", length = 1)
    private String rezart;
    
    @Column(name = "RE RC", length = 3)
    private String rerc;
    
    @Column(name = "UST-ID-NR/OK", length = 20)
    private String ustidnrok;
    
    @Column(name = "FAHRG.-NR.", length = 17)
    private String fahrgnr;
    
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
    private String antag;
    
    @Column(name = "AN-ZEIT", length = 4)
    private String anzeit;
    
    @Column(name = "FERT-TAG", length = 8)
    private String ferttag;
    
    @Column(name = "FERT-ZEIT", length = 4)
    private String fertzeit;
    
    @Column(name = "BERATER", length = 20)
    private String berater;
    
    @Column(name = "LEITZAHL", length = 3)
    private String leitzahl;
    
    @Column(name = "TX.ANF", length = 3)
    private String txanf;
    
    @Column(name = "TX.ENDE", length = 3)
    private String txende;
    
    @Column(name = "MOTOR-NR", length = 10)
    private String motornr;
    
    @Column(name = "MOTOR-TYP", length = 20)
    private String motortyp;
    
    @Column(name = "USER AUFTRAG", length = 10)
    private String userauftrag;
    
    @Column(name = "USER RECHNUNG", length = 10)
    private String userrechnung;
    
    @Column(name = "RGS NETTO", precision = 9, scale = 2)
    private BigDecimal rgsnetto;
    
    @Column(name = "RGS BASIS AT", precision = 9, scale = 2)
    private BigDecimal rgsbasisat;
    
    @Column(name = "RGS BASIS MWST", precision = 9, scale = 2)
    private BigDecimal rgsbasismwst;
    
    @Column(name = "RGS MWST", precision = 9, scale = 2)
    private BigDecimal rgsmwst;
    
    @Column(name = "RGS MWST AT", precision = 9, scale = 2)
    private BigDecimal rgsmwstat;
    
    @Column(name = "RGS GES BRUTTO", precision = 9, scale = 2)
    private BigDecimal rgsgesbrutto;
    
    @Column(name = "EG-UMSATZ", length = 1)
    private String egumsatz;
    
    @Column(name = "STEUERFREI DRITTLAND", length = 1)
    private String steuerfreidrittland;
    
    @Column(name = "VERBUCHT?", length = 1)
    private String verbucht;
    
    @Column(name = "RESERVE", precision = 5, scale = 2)
    private BigDecimal reserve1;
    
    @Column(name = "RESERVE", precision = 9, scale = 2)
    private BigDecimal reserve2;
    
    @Column(name = "GA-�BERN.", length = 8)
    private String gauebern;
    
    @Column(name = "WKT-ID", precision = 9, scale = 0)
    private BigDecimal wktid;
    
    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve3;
    
    @Column(name = "RESERVE", precision = 2, scale = 0)
    private Integer reserve4;
    
    @Column(name = "F:V>0", precision = 3, scale = 0)
    private Integer fv0;
    
    @Column(name = "F:B>0", precision = 3, scale = 0)
    private Integer fb0;
    
    @Column(name = "KAMPAGNE-NR", precision = 6, scale = 0)
    private Integer kampagnenr;
    
    @Column(name = "SPO ORDER", length = 10)
    private String spoorder;
    
    @Column(name = "KEN-AV", length = 2)
    private String kenav;
    
    @Column(name = "KEN-PE", length = 2)
    private String kenpe;
    
    @Column(name = "KLR-BERECH", length = 1)
    private String klrberech;
    
    @Column(name = "KLR-BETRAG", precision = 5, scale = 2)
    private BigDecimal klrbetrag;
    
    @Column(name = "ASSI-VORGANG-NR", length = 15)
    private String assivorgang;
    
    @Column(name = "ZAGA-GUELTIG", length = 8)
    private String zagagueltig;
    
    @Column(name = "R&W FREIGABE-NR", length = 15)
    private String rwfreigabenr;
    
    @Column(name = "KL-ERWEITERUNG", precision = 5, scale = 0)
    private Integer klerweiterung;
    
    @Column(name = "KL-AUSNAHME IDNR", length = 3)
    private String klausnahmeidnr;
    
    @Column(name = "KL-AUSNAHME KLARTEXT", length = 40)
    private String klausnahmeklartext;
    
    @Column(name = "FAHRZEUG-ART", length = 20)
    private String fahrzeugart;
    
    @Column(name = "HERSTELLER", length = 20)
    private String hersteller;
    
    @Column(name = "AUFBAUART", length = 20)
    private String aufbauart;
    
    @Column(name = "HERSTELLER AUFBAU", length = 20)
    private String herstelleraufbau;
    
    @Column(name = "ZUSATZAUSR�STUNG 1", length = 20)
    private String zusatzausruestung1;
    
    @Column(name = "HERSTELLER ZUSATZ 1", length = 20)
    private String herstellerzusatz1;
    
    @Column(name = "ZUSATZAUSR�STUNG 2", length = 20)
    private String zusatzausruestung2;
    
    @Column(name = "HERSTELLER ZUSATZ 2", length = 20)
    private String herstellerzusatz2;
    
    @Column(name = "ZUSATZAUSR�STUNG 3", length = 20)
    private String zusatzausruestung3;
    
    @Column(name = "HERSTELLER ZUSATZ 3", length = 20)
    private String herstellerzusatz3;
    
    @Column(name = "EINSATZART", length = 20)
    private String einsatzart;
    
    @Column(name = "EURO-NORM", length = 10)
    private String euronorm;
    
    @Column(name = "PARTIKELFILTER", length = 1)
    private String partikelfilter;
    
    @Column(name = "IS-ART", length = 5)
    private String isart;
    
    @Column(name = "MAIL TO", length = 200)
    private String mailto;
    
    @Column(name = "MAIL CC", length = 200)
    private String mailcc;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRnr() { return rnr; }
    public void setRnr(String rnr) { this.rnr = rnr; }
    public String getRgnr10a() { return rgnr10a; }
    public void setRgnr10a(String rgnr10a) { this.rgnr10a = rgnr10a; }
    public String getRdat() { return rdat; }
    public void setRdat(String rdat) { this.rdat = rdat; }
    public String getKzs() { return kzs; }
    public void setKzs(String kzs) { this.kzs = kzs; }
    public String getAnr() { return anr; }
    public void setAnr(String anr) { this.anr = anr; }
    public String getBerei() { return berei; }
    public void setBerei(String berei) { this.berei = berei; }
    public String getWt() { return wt; }
    public void setWt(String wt) { this.wt = wt; }
    public String getSplitt() { return splitt; }
    public void setSplitt(String splitt) { this.splitt = splitt; }
    public String getAdat() { return adat; }
    public void setAdat(String adat) { this.adat = adat; }
    public String getAtext() { return atext; }
    public void setAtext(String atext) { this.atext = atext; }
    public String getLrnr() { return lrnr; }
    public void setLrnr(String lrnr) { this.lrnr = lrnr; }
    public String getStobezre() { return stobezre; }
    public void setStobezre(String stobezre) { this.stobezre = stobezre; }
    public String getStobezredat() { return stobezredat; }
    public void setStobezredat(String stobezredat) { this.stobezredat = stobezredat; }
    public String getKorbezre() { return korbezre; }
    public void setKorbezre(String korbezre) { this.korbezre = korbezre; }
    public String getKorbezredat() { return korbezredat; }
    public void setKorbezredat(String korbezredat) { this.korbezredat = korbezredat; }
    public String getBfort() { return bfort; }
    public void setBfort(String bfort) { this.bfort = bfort; }
    public String getMwstyn() { return mwstyn; }
    public void setMwstyn(String mwstyn) { this.mwstyn = mwstyn; }
    public BigDecimal getMwstprozent() { return mwstprozent; }
    public void setMwstprozent(BigDecimal mwstprozent) { this.mwstprozent = mwstprozent; }
    public BigDecimal getMwstprozentr() { return mwstprozentr; }
    public void setMwstprozentr(BigDecimal mwstprozentr) { this.mwstprozentr = mwstprozentr; }
    public String getBaschluessel() { return baschluessel; }
    public void setBaschluessel(String baschluessel) { this.baschluessel = baschluessel; }
    public String getKstlohn() { return kstlohn; }
    public void setKstlohn(String kstlohn) { this.kstlohn = kstlohn; }
    public String getKstteile() { return kstteile; }
    public void setKstteile(String kstteile) { this.kstteile = kstteile; }
    public String getFibumwst() { return fibumwst; }
    public void setFibumwst(String fibumwst) { this.fibumwst = fibumwst; }
    public String getFibumwstat() { return fibumwstat; }
    public void setFibumwstat(String fibumwstat) { this.fibumwstat = fibumwstat; }
    public String getFibuinterim() { return fibuinterim; }
    public void setFibuinterim(String fibuinterim) { this.fibuinterim = fibuinterim; }
    public String getKtointauf() { return ktointauf; }
    public void setKtointauf(String ktointauf) { this.ktointauf = ktointauf; }
    public String getKtrintauf() { return ktrintauf; }
    public void setKtrintauf(String ktrintauf) { this.ktrintauf = ktrintauf; }
    public String getKstintauf() { return kstintauf; }
    public void setKstintauf(String kstintauf) { this.kstintauf = kstintauf; }
    public String getSpezcode() { return spezcode; }
    public void setSpezcode(String spezcode) { this.spezcode = spezcode; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getProdcode() { return prodcode; }
    public void setProdcode(String prodcode) { this.prodcode = prodcode; }
    public String getProjekt() { return projekt; }
    public void setProjekt(String projekt) { this.projekt = projekt; }
    public String getDokumentennummer() { return dokumentennummer; }
    public void setDokumentennummer(String dokumentennummer) { this.dokumentennummer = dokumentennummer; }
    public String getKostencodekonzint() { return kostencodekonzint; }
    public void setKostencodekonzint(String kostencodekonzint) { this.kostencodekonzint = kostencodekonzint; }
    public String getKundennr() { return kundennr; }
    public void setKundennr(String kundennr) { this.kundennr = kundennr; }
    public String getAnrede() { return anrede; }
    public void setAnrede(String anrede) { this.anrede = anrede; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBranche() { return branche; }
    public void setBranche(String branche) { this.branche = branche; }
    public String getMatch() { return match; }
    public void setMatch(String match) { this.match = match; }
    public String getStrasse() { return strasse; }
    public void setStrasse(String strasse) { this.strasse = strasse; }
    public String getLand() { return land; }
    public void setLand(String land) { this.land = land; }
    public String getPlz() { return plz; }
    public void setPlz(String plz) { this.plz = plz; }
    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public String getBestellerkunde() { return bestellerkunde; }
    public void setBestellerkunde(String bestellerkunde) { this.bestellerkunde = bestellerkunde; }
    public String getValuta() { return valuta; }
    public void setValuta(String valuta) { this.valuta = valuta; }
    public String getBonitaet() { return bonitaet; }
    public void setBonitaet(String bonitaet) { this.bonitaet = bonitaet; }
    public String getZahlungsart() { return zahlungsart; }
    public void setZahlungsart(String zahlungsart) { this.zahlungsart = zahlungsart; }
    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }
    public String getRekundennr() { return rekundennr; }
    public void setRekundennr(String rekundennr) { this.rekundennr = rekundennr; }
    public String getReanrede() { return reanrede; }
    public void setReanrede(String reanrede) { this.reanrede = reanrede; }
    public String getRename() { return rename; }
    public void setRename(String rename) { this.rename = rename; }
    public String getRebranche() { return rebranche; }
    public void setRebranche(String rebranche) { this.rebranche = rebranche; }
    public String getRematch() { return rematch; }
    public void setRematch(String rematch) { this.rematch = rematch; }
    public String getRestrasse() { return restrasse; }
    public void setRestrasse(String restrasse) { this.restrasse = restrasse; }
    public String getReland() { return reland; }
    public void setReland(String reland) { this.reland = reland; }
    public String getReplz() { return replz; }
    public void setReplz(String replz) { this.replz = replz; }
    public String getReort() { return reort; }
    public void setReort(String reort) { this.reort = reort; }
    public String getRetele() { return retele; }
    public void setRetele(String retele) { this.retele = retele; }
    public String getRevaluta() { return revaluta; }
    public void setRevaluta(String revaluta) { this.revaluta = revaluta; }
    public String getRebonitaet() { return rebonitaet; }
    public void setRebonitaet(String rebonitaet) { this.rebonitaet = rebonitaet; }
    public String getRezart() { return rezart; }
    public void setRezart(String rezart) { this.rezart = rezart; }
    public String getRerc() { return rerc; }
    public void setRerc(String rerc) { this.rerc = rerc; }
    public String getUstidnrok() { return ustidnrok; }
    public void setUstidnrok(String ustidnrok) { this.ustidnrok = ustidnrok; }
    public String getFahrgnr() { return fahrgnr; }
    public void setFahrgnr(String fahrgnr) { this.fahrgnr = fahrgnr; }
    public String getKz() { return kz; }
    public void setKz(String kz) { this.kz = kz; }
    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }
    public String getBj() { return bj; }
    public void setBj(String bj) { this.bj = bj; }
    public String getZdat() { return zdat; }
    public void setZdat(String zdat) { this.zdat = zdat; }
    public String getWrg() { return wrg; }
    public void setWrg(String wrg) { this.wrg = wrg; }
    public String getAu() { return au; }
    public void setAu(String au) { this.au = au; }
    public String getGa() { return ga; }
    public void setGa(String ga) { this.ga = ga; }
    public String getSp() { return sp; }
    public void setSp(String sp) { this.sp = sp; }
    public String getTacho() { return tacho; }
    public void setTacho(String tacho) { this.tacho = tacho; }
    public String getKm() { return km; }
    public void setKm(String km) { this.km = km; }
    public String getHu() { return hu; }
    public void setHu(String hu) { this.hu = hu; }
    public String getAntag() { return antag; }
    public void setAntag(String antag) { this.antag = antag; }
    public String getAnzeit() { return anzeit; }
    public void setAnzeit(String anzeit) { this.anzeit = anzeit; }
    public String getFerttag() { return ferttag; }
    public void setFerttag(String ferttag) { this.ferttag = ferttag; }
    public String getFertzeit() { return fertzeit; }
    public void setFertzeit(String fertzeit) { this.fertzeit = fertzeit; }
    public String getBerater() { return berater; }
    public void setBerater(String berater) { this.berater = berater; }
    public String getLeitzahl() { return leitzahl; }
    public void setLeitzahl(String leitzahl) { this.leitzahl = leitzahl; }
    public String getTxanf() { return txanf; }
    public void setTxanf(String txanf) { this.txanf = txanf; }
    public String getTxende() { return txende; }
    public void setTxende(String txende) { this.txende = txende; }
    public String getMotornr() { return motornr; }
    public void setMotornr(String motornr) { this.motornr = motornr; }
    public String getMotortyp() { return motortyp; }
    public void setMotortyp(String motortyp) { this.motortyp = motortyp; }
    public String getUserauftrag() { return userauftrag; }
    public void setUserauftrag(String userauftrag) { this.userauftrag = userauftrag; }
    public String getUserrechnung() { return userrechnung; }
    public void setUserrechnung(String userrechnung) { this.userrechnung = userrechnung; }
    public BigDecimal getRgsnetto() { return rgsnetto; }
    public void setRgsnetto(BigDecimal rgsnetto) { this.rgsnetto = rgsnetto; }
    public BigDecimal getRgsbasisat() { return rgsbasisat; }
    public void setRgsbasisat(BigDecimal rgsbasisat) { this.rgsbasisat = rgsbasisat; }
    public BigDecimal getRgsbasismwst() { return rgsbasismwst; }
    public void setRgsbasismwst(BigDecimal rgsbasismwst) { this.rgsbasismwst = rgsbasismwst; }
    public BigDecimal getRgsmwst() { return rgsmwst; }
    public void setRgsmwst(BigDecimal rgsmwst) { this.rgsmwst = rgsmwst; }
    public BigDecimal getRgsmwstat() { return rgsmwstat; }
    public void setRgsmwstat(BigDecimal rgsmwstat) { this.rgsmwstat = rgsmwstat; }
    public BigDecimal getRgsgesbrutto() { return rgsgesbrutto; }
    public void setRgsgesbrutto(BigDecimal rgsgesbrutto) { this.rgsgesbrutto = rgsgesbrutto; }
    public String getEgumsatz() { return egumsatz; }
    public void setEgumsatz(String egumsatz) { this.egumsatz = egumsatz; }
    public String getSteuerfreidrittland() { return steuerfreidrittland; }
    public void setSteuerfreidrittland(String steuerfreidrittland) { this.steuerfreidrittland = steuerfreidrittland; }
    public String getVerbucht() { return verbucht; }
    public void setVerbucht(String verbucht) { this.verbucht = verbucht; }
    public BigDecimal getReserve1() { return reserve1; }
    public void setReserve1(BigDecimal reserve1) { this.reserve1 = reserve1; }
    public BigDecimal getReserve2() { return reserve2; }
    public void setReserve2(BigDecimal reserve2) { this.reserve2 = reserve2; }
    public String getGauebern() { return gauebern; }
    public void setGauebern(String gauebern) { this.gauebern = gauebern; }
    public BigDecimal getWktid() { return wktid; }
    public void setWktid(BigDecimal wktid) { this.wktid = wktid; }
    public Integer getReserve3() { return reserve3; }
    public void setReserve3(Integer reserve3) { this.reserve3 = reserve3; }
    public Integer getReserve4() { return reserve4; }
    public void setReserve4(Integer reserve4) { this.reserve4 = reserve4; }
    public Integer getFv0() { return fv0; }
    public void setFv0(Integer fv0) { this.fv0 = fv0; }
    public Integer getFb0() { return fb0; }
    public void setFb0(Integer fb0) { this.fb0 = fb0; }
    public Integer getKampagnenr() { return kampagnenr; }
    public void setKampagnenr(Integer kampagnenr) { this.kampagnenr = kampagnenr; }
    public String getSpoorder() { return spoorder; }
    public void setSpoorder(String spoorder) { this.spoorder = spoorder; }
    public String getKenav() { return kenav; }
    public void setKenav(String kenav) { this.kenav = kenav; }
    public String getKenpe() { return kenpe; }
    public void setKenpe(String kenpe) { this.kenpe = kenpe; }
    public String getKlrberech() { return klrberech; }
    public void setKlrberech(String klrberech) { this.klrberech = klrberech; }
    public BigDecimal getKlrbetrag() { return klrbetrag; }
    public void setKlrbetrag(BigDecimal klrbetrag) { this.klrbetrag = klrbetrag; }
    public String getAssivorgang() { return assivorgang; }
    public void setAssivorgang(String assivorgang) { this.assivorgang = assivorgang; }
    public String getZagagueltig() { return zagagueltig; }
    public void setZagagueltig(String zagagueltig) { this.zagagueltig = zagagueltig; }
    public String getRwfreigabenr() { return rwfreigabenr; }
    public void setRwfreigabenr(String rwfreigabenr) { this.rwfreigabenr = rwfreigabenr; }
    public Integer getKlerweiterung() { return klerweiterung; }
    public void setKlerweiterung(Integer klerweiterung) { this.klerweiterung = klerweiterung; }
    public String getKlausnahmeidnr() { return klausnahmeidnr; }
    public void setKlausnahmeidnr(String klausnahmeidnr) { this.klausnahmeidnr = klausnahmeidnr; }
    public String getKlausnahmeklartext() { return klausnahmeklartext; }
    public void setKlausnahmeklartext(String klausnahmeklartext) { this.klausnahmeklartext = klausnahmeklartext; }
    public String getFahrzeugart() { return fahrzeugart; }
    public void setFahrzeugart(String fahrzeugart) { this.fahrzeugart = fahrzeugart; }
    public String getHersteller() { return hersteller; }
    public void setHersteller(String hersteller) { this.hersteller = hersteller; }
    public String getAufbauart() { return aufbauart; }
    public void setAufbauart(String aufbauart) { this.aufbauart = aufbauart; }
    public String getHerstelleraufbau() { return herstelleraufbau; }
    public void setHerstelleraufbau(String herstelleraufbau) { this.herstelleraufbau = herstelleraufbau; }
    public String getZusatzausruestung1() { return zusatzausruestung1; }
    public void setZusatzausruestung1(String zusatzausruestung1) { this.zusatzausruestung1 = zusatzausruestung1; }
    public String getHerstellerzusatz1() { return herstellerzusatz1; }
    public void setHerstellerzusatz1(String herstellerzusatz1) { this.herstellerzusatz1 = herstellerzusatz1; }
    public String getZusatzausruestung2() { return zusatzausruestung2; }
    public void setZusatzausruestung2(String zusatzausruestung2) { this.zusatzausruestung2 = zusatzausruestung2; }
    public String getHerstellerzusatz2() { return herstellerzusatz2; }
    public void setHerstellerzusatz2(String herstellerzusatz2) { this.herstellerzusatz2 = herstellerzusatz2; }
    public String getZusatzausruestung3() { return zusatzausruestung3; }
    public void setZusatzausruestung3(String zusatzausruestung3) { this.zusatzausruestung3 = zusatzausruestung3; }
    public String getHerstellerzusatz3() { return herstellerzusatz3; }
    public void setHerstellerzusatz3(String herstellerzusatz3) { this.herstellerzusatz3 = herstellerzusatz3; }
    public String getEinsatzart() { return einsatzart; }
    public void setEinsatzart(String einsatzart) { this.einsatzart = einsatzart; }
    public String getEuronorm() { return euronorm; }
    public void setEuronorm(String euronorm) { this.euronorm = euronorm; }
    public String getPartikelfilter() { return partikelfilter; }
    public void setPartikelfilter(String partikelfilter) { this.partikelfilter = partikelfilter; }
    public String getIsart() { return isart; }
    public void setIsart(String isart) { this.isart = isart; }
    public String getMailto() { return mailto; }
    public void setMailto(String mailto) { this.mailto = mailto; }
    public String getMailcc() { return mailcc; }
    public void setMailcc(String mailcc) { this.mailcc = mailcc; }
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
    private String rgnr10a;
    
    @Column(name = "RDAT", length = 8)
    private String rdat;
    
    @Column(name = "KZ S", length = 1)
    private String kzs;
    
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
    private Integer lnrpak;
    
    @Column(name = "PAKET-NR.", length = 8)
    private String paketnr;
    
    @Column(name = "SORT RZ", precision = 3, scale = 0)
    private Integer sortrz;
    
    @Column(name = "LNR RZ", precision = 3, scale = 0)
    private Integer lnrrz;
    
    @Column(name = "AG", length = 8)
    private String ag;
    
    @Column(name = "L.NR.", length = 3)
    private String lnr;
    
    @Column(name = "BEZ.", length = 40)
    private String bez;
    
    @Column(name = "WERKSZEIT", precision = 5, scale = 2)
    private BigDecimal werkszeit;
    
    @Column(name = "AW-STUNDEN", precision = 5, scale = 2)
    private BigDecimal awstunden;
    
    @Column(name = "ZE", precision = 5, scale = 0)
    private Integer ze;
    
    @Column(name = "PE", precision = 5, scale = 0)
    private Integer pe;
    
    @Column(name = "SATZ-PE", precision = 5, scale = 2)
    private BigDecimal satzpe;
    
    @Column(name = "GEW-ZE", length = 1)
    private String gewze;
    
    @Column(name = "PREIS", precision = 9, scale = 2)
    private BigDecimal preis;
    
    @Column(name = "MONTEUR", length = 3)
    private String monteur;
    
    @Column(name = "BC", length = 2)
    private String bc;
    
    @Column(name = "V-SATZ", precision = 5, scale = 2)
    private BigDecimal vsatz;
    
    @Column(name = "M-STUNDEN", precision = 5, scale = 2)
    private BigDecimal mstunden;
    
    @Column(name = "V-DM-NETTO", precision = 9, scale = 2)
    private BigDecimal vdmnetto;
    
    @Column(name = "V-DM BRUTTO", precision = 9, scale = 2)
    private BigDecimal vdmbrutto;
    
    @Column(name = "V-STUNDEN", precision = 9, scale = 2)
    private BigDecimal vstunden;
    
    @Column(name = "ZUSCHLAG", precision = 5, scale = 2)
    private BigDecimal zuschlag;
    
    @Column(name = "RABATT", precision = 5, scale = 2)
    private BigDecimal rabatt;
    
    @Column(name = "KZ S/AW", length = 1)
    private String kzsaw;
    
    @Column(name = "KZ-MWST", length = 1)
    private String kzmwst;
    
    @Column(name = "VERDICHTEN", length = 1)
    private String verdichten;
    
    @Column(name = "TXT-KEY", length = 3)
    private String txtkey;
    
    @Column(name = "RG BRUTTO", precision = 9, scale = 2)
    private BigDecimal rgbrutto;
    
    @Column(name = "RG RABATT", precision = 9, scale = 2)
    private BigDecimal rgrabatt;
    
    @Column(name = "RG NETTO", precision = 9, scale = 2)
    private BigDecimal rgnetto;
    
    @Column(name = "KEN.RE2SUM", length = 1)
    private String kenre2sum;
    
    @Column(name = "URSPR-FAK/H MON", precision = 5, scale = 2)
    private BigDecimal ursprfakhmon;
    
    @Column(name = "URSPR-NETTO MON", precision = 9, scale = 2)
    private BigDecimal ursprnettomon;
    
    @Column(name = "EINSTANDSPREIS", precision = 9, scale = 2)
    private BigDecimal einstandspreis;
    
    @Column(name = "EPS NAME", length = 20)
    private String epsname;
    
    @Column(name = "EPS MINDERUNG %", precision = 5, scale = 2)
    private BigDecimal epsminderung;
    
    @Column(name = "VARIANTE", length = 500)
    private String variante;
    
    @Column(name = "ARBEITSBESCHREIBUNG", length = 2000)
    private String arbeitsbeschreibung;
    
    @Column(name = "RECHNUNGSTEXT", length = 2000)
    private String rechnungstext;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRnr() { return rnr; }
    public void setRnr(String rnr) { this.rnr = rnr; }
    public String getRgnr10a() { return rgnr10a; }
    public void setRgnr10a(String rgnr10a) { this.rgnr10a = rgnr10a; }
    public String getRdat() { return rdat; }
    public void setRdat(String rdat) { this.rdat = rdat; }
    public String getKzs() { return kzs; }
    public void setKzs(String kzs) { this.kzs = kzs; }
    public String getAnr() { return anr; }
    public void setAnr(String anr) { this.anr = anr; }
    public String getBerei() { return berei; }
    public void setBerei(String berei) { this.berei = berei; }
    public String getWt() { return wt; }
    public void setWt(String wt) { this.wt = wt; }
    public String getSplitt() { return splitt; }
    public void setSplitt(String splitt) { this.splitt = splitt; }
    public Integer getPos() { return pos; }
    public void setPos(Integer pos) { this.pos = pos; }
    public String getEc() { return ec; }
    public void setEc(String ec) { this.ec = ec; }
    public Integer getLnrpak() { return lnrpak; }
    public void setLnrpak(Integer lnrpak) { this.lnrpak = lnrpak; }
    public String getPaketnr() { return paketnr; }
    public void setPaketnr(String paketnr) { this.paketnr = paketnr; }
    public Integer getSortrz() { return sortrz; }
    public void setSortrz(Integer sortrz) { this.sortrz = sortrz; }
    public Integer getLnrrz() { return lnrrz; }
    public void setLnrrz(Integer lnrrz) { this.lnrrz = lnrrz; }
    public String getAg() { return ag; }
    public void setAg(String ag) { this.ag = ag; }
    public String getLnr() { return lnr; }
    public void setLnr(String lnr) { this.lnr = lnr; }
    public String getBez() { return bez; }
    public void setBez(String bez) { this.bez = bez; }
    public BigDecimal getWerkszeit() { return werkszeit; }
    public void setWerkszeit(BigDecimal werkszeit) { this.werkszeit = werkszeit; }
    public BigDecimal getAwstunden() { return awstunden; }
    public void setAwstunden(BigDecimal awstunden) { this.awstunden = awstunden; }
    public Integer getZe() { return ze; }
    public void setZe(Integer ze) { this.ze = ze; }
    public Integer getPe() { return pe; }
    public void setPe(Integer pe) { this.pe = pe; }
    public BigDecimal getSatzpe() { return satzpe; }
    public void setSatzpe(BigDecimal satzpe) { this.satzpe = satzpe; }
    public String getGewze() { return gewze; }
    public void setGewze(String gewze) { this.gewze = gewze; }
    public BigDecimal getPreis() { return preis; }
    public void setPreis(BigDecimal preis) { this.preis = preis; }
    public String getMonteur() { return monteur; }
    public void setMonteur(String monteur) { this.monteur = monteur; }
    public String getBc() { return bc; }
    public void setBc(String bc) { this.bc = bc; }
    public BigDecimal getVsatz() { return vsatz; }
    public void setVsatz(BigDecimal vsatz) { this.vsatz = vsatz; }
    public BigDecimal getMstunden() { return mstunden; }
    public void setMstunden(BigDecimal mstunden) { this.mstunden = mstunden; }
    public BigDecimal getVdmnetto() { return vdmnetto; }
    public void setVdmnetto(BigDecimal vdmnetto) { this.vdmnetto = vdmnetto; }
    public BigDecimal getVdmbrutto() { return vdmbrutto; }
    public void setVdmbrutto(BigDecimal vdmbrutto) { this.vdmbrutto = vdmbrutto; }
    public BigDecimal getVstunden() { return vstunden; }
    public void setVstunden(BigDecimal vstunden) { this.vstunden = vstunden; }
    public BigDecimal getZuschlag() { return zuschlag; }
    public void setZuschlag(BigDecimal zuschlag) { this.zuschlag = zuschlag; }
    public BigDecimal getRabatt() { return rabatt; }
    public void setRabatt(BigDecimal rabatt) { this.rabatt = rabatt; }
    public String getKzsaw() { return kzsaw; }
    public void setKzsaw(String kzsaw) { this.kzsaw = kzsaw; }
    public String getKzmwst() { return kzmwst; }
    public void setKzmwst(String kzmwst) { this.kzmwst = kzmwst; }
    public String getVerdichten() { return verdichten; }
    public void setVerdichten(String verdichten) { this.verdichten = verdichten; }
    public String getTxtkey() { return txtkey; }
    public void setTxtkey(String txtkey) { this.txtkey = txtkey; }
    public BigDecimal getRgbrutto() { return rgbrutto; }
    public void setRgbrutto(BigDecimal rgbrutto) { this.rgbrutto = rgbrutto; }
    public BigDecimal getRgrabatt() { return rgrabatt; }
    public void setRgrabatt(BigDecimal rgrabatt) { this.rgrabatt = rgrabatt; }
    public BigDecimal getRgnetto() { return rgnetto; }
    public void setRgnetto(BigDecimal rgnetto) { this.rgnetto = rgnetto; }
    public String getKenre2sum() { return kenre2sum; }
    public void setKenre2sum(String kenre2sum) { this.kenre2sum = kenre2sum; }
    public BigDecimal getUrsprfakhmon() { return ursprfakhmon; }
    public void setUrsprfakhmon(BigDecimal ursprfakhmon) { this.ursprfakhmon = ursprfakhmon; }
    public BigDecimal getUrsprnettomon() { return ursprnettomon; }
    public void setUrsprnettomon(BigDecimal ursprnettomon) { this.ursprnettomon = ursprnettomon; }
    public BigDecimal getEinstandspreis() { return einstandspreis; }
    public void setEinstandspreis(BigDecimal einstandspreis) { this.einstandspreis = einstandspreis; }
    public String getEpsname() { return epsname; }
    public void setEpsname(String epsname) { this.epsname = epsname; }
    public BigDecimal getEpsminderung() { return epsminderung; }
    public void setEpsminderung(BigDecimal epsminderung) { this.epsminderung = epsminderung; }
    public String getVariante() { return variante; }
    public void setVariante(String variante) { this.variante = variante; }
    public String getArbeitsbeschreibung() { return arbeitsbeschreibung; }
    public void setArbeitsbeschreibung(String arbeitsbeschreibung) { this.arbeitsbeschreibung = arbeitsbeschreibung; }
    public String getRechnungstext() { return rechnungstext; }
    public void setRechnungstext(String rechnungstext) { this.rechnungstext = rechnungstext; }
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
    private BigDecimal cusno;
    
    @Column(name = "D.C.NO.", precision = 8, scale = 0)
    private BigDecimal dcno;
    
    @Column(name = "D.C.FN.", length = 5)
    private String dcfn;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKzl() { return kzl; }
    public void setKzl(String kzl) { this.kzl = kzl; }
    public String getRnr() { return rnr; }
    public void setRnr(String rnr) { this.rnr = rnr; }
    public String getRdat() { return rdat; }
    public void setRdat(String rdat) { this.rdat = rdat; }
    public String getFgnr() { return fgnr; }
    public void setFgnr(String fgnr) { this.fgnr = fgnr; }
    public String getRepdat() { return repdat; }
    public void setRepdat(String repdat) { this.repdat = repdat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getCusno() { return cusno; }
    public void setCusno(BigDecimal cusno) { this.cusno = cusno; }
    public BigDecimal getDcno() { return dcno; }
    public void setDcno(BigDecimal dcno) { this.dcno = dcno; }
    public String getDcfn() { return dcfn; }
    public void setDcfn(String dcfn) { this.dcfn = dcfn; }
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
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getZulDatum() { return zulDatum; }
    public void setZulDatum(Integer zulDatum) { this.zulDatum = zulDatum; }
    public Integer getRepDatum() { return repDatum; }
    public void setRepDatum(Integer repDatum) { this.repDatum = repDatum; }
    public Integer getKmStand() { return kmStand; }
    public void setKmStand(Integer kmStand) { this.kmStand = kmStand; }
    public Integer getProduktTyp() { return produktTyp; }
    public void setProduktTyp(Integer produktTyp) { this.produktTyp = produktTyp; }
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
    public Integer getStatusCodeSde() { return statusCodeSde; }
    public void setStatusCodeSde(Integer statusCodeSde) { this.statusCodeSde = statusCodeSde; }
    public Integer getAnzFehler() { return anzFehler; }
    public void setAnzFehler(Integer anzFehler) { this.anzFehler = anzFehler; }
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    public String getAufNr() { return aufNr; }
    public void setAufNr(String aufNr) { this.aufNr = aufNr; }
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
    private String schadc1;
    
    @Column(name = "SCHAD.-C2", length = 2)
    private String schadc2;
    
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
    private Integer vrepDatum;
    
    @Column(name = "V.-KM-STAND", precision = 3, scale = 0)
    private Integer vkmStand;
    
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
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getRechNr() { return rechNr; }
    public void setRechNr(String rechNr) { this.rechNr = rechNr; }
    public String getRechDatum() { return rechDatum; }
    public void setRechDatum(String rechDatum) { this.rechDatum = rechDatum; }
    public String getAuftragsNr() { return auftragsNr; }
    public void setAuftragsNr(String auftragsNr) { this.auftragsNr = auftragsNr; }
    public String getBereich() { return bereich; }
    public void setBereich(String bereich) { this.bereich = bereich; }
    public String getClaimNr() { return claimNr; }
    public void setClaimNr(String claimNr) { this.claimNr = claimNr; }
    public String getFehlerNr() { return fehlerNr; }
    public void setFehlerNr(String fehlerNr) { this.fehlerNr = fehlerNr; }
    public String getFolgeNr() { return folgeNr; }
    public void setFolgeNr(String folgeNr) { this.folgeNr = folgeNr; }
    public String getFehlerTeil() { return fehlerTeil; }
    public void setFehlerTeil(String fehlerTeil) { this.fehlerTeil = fehlerTeil; }
    public String getHauptgruppe() { return hauptgruppe; }
    public void setHauptgruppe(String hauptgruppe) { this.hauptgruppe = hauptgruppe; }
    public String getNebengruppe() { return nebengruppe; }
    public void setNebengruppe(String nebengruppe) { this.nebengruppe = nebengruppe; }
    public String getSchadc1() { return schadc1; }
    public void setSchadc1(String schadc1) { this.schadc1 = schadc1; }
    public String getSchadc2() { return schadc2; }
    public void setSchadc2(String schadc2) { this.schadc2 = schadc2; }
    public String getText1() { return text1; }
    public void setText1(String text1) { this.text1 = text1; }
    public String getText2() { return text2; }
    public void setText2(String text2) { this.text2 = text2; }
    public String getSteuerCode() { return steuerCode; }
    public void setSteuerCode(String steuerCode) { this.steuerCode = steuerCode; }
    public String getBewCode1() { return bewCode1; }
    public void setBewCode1(String bewCode1) { this.bewCode1 = bewCode1; }
    public Integer getBewCode2() { return bewCode2; }
    public void setBewCode2(Integer bewCode2) { this.bewCode2 = bewCode2; }
    public Integer getBewDatum() { return bewDatum; }
    public void setBewDatum(Integer bewDatum) { this.bewDatum = bewDatum; }
    public Integer getVergMat() { return vergMat; }
    public void setVergMat(Integer vergMat) { this.vergMat = vergMat; }
    public Integer getVergArb() { return vergArb; }
    public void setVergArb(Integer vergArb) { this.vergArb = vergArb; }
    public Integer getVergSpez() { return vergSpez; }
    public void setVergSpez(Integer vergSpez) { this.vergSpez = vergSpez; }
    public BigDecimal getBeantrMat() { return beantrMat; }
    public void setBeantrMat(BigDecimal beantrMat) { this.beantrMat = beantrMat; }
    public BigDecimal getBeantrgArb() { return beantrgArb; }
    public void setBeantrgArb(BigDecimal beantrgArb) { this.beantrgArb = beantrgArb; }
    public BigDecimal getBeantrgSpez() { return beantrgSpez; }
    public void setBeantrgSpez(BigDecimal beantrgSpez) { this.beantrgSpez = beantrgSpez; }
    public Integer getClaimArt() { return claimArt; }
    public void setClaimArt(Integer claimArt) { this.claimArt = claimArt; }
    public Integer getVrepDatum() { return vrepDatum; }
    public void setVrepDatum(Integer vrepDatum) { this.vrepDatum = vrepDatum; }
    public Integer getVkmStand() { return vkmStand; }
    public void setVkmStand(Integer vkmStand) { this.vkmStand = vkmStand; }
    public Integer getFeldtestNr() { return feldtestNr; }
    public void setFeldtestNr(Integer feldtestNr) { this.feldtestNr = feldtestNr; }
    public String getKampagnenNr() { return kampagnenNr; }
    public void setKampagnenNr(String kampagnenNr) { this.kampagnenNr = kampagnenNr; }
    public String getEps() { return eps; }
    public void setEps(String eps) { this.eps = eps; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Integer getVariantCode() { return variantCode; }
    public void setVariantCode(Integer variantCode) { this.variantCode = variantCode; }
    public Integer getActionCode() { return actionCode; }
    public void setActionCode(Integer actionCode) { this.actionCode = actionCode; }
    public String getText3() { return text3; }
    public void setText3(String text3) { this.text3 = text3; }
    public String getText4() { return text4; }
    public void setText4(String text4) { this.text4 = text4; }
    public String getFehlerNrSde() { return fehlerNrSde; }
    public void setFehlerNrSde(String fehlerNrSde) { this.fehlerNrSde = fehlerNrSde; }
    public String getAnhang() { return anhang; }
    public void setAnhang(String anhang) { this.anhang = anhang; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getComplain() { return complain; }
    public void setComplain(String complain) { this.complain = complain; }
    public String getSymptom() { return symptom; }
    public void setSymptom(String symptom) { this.symptom = symptom; }
    public String getFailure() { return failure; }
    public void setFailure(String failure) { this.failure = failure; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRepair() { return repair; }
    public void setRepair(String repair) { this.repair = repair; }
    public String getErgCode() { return ergCode; }
    public void setErgCode(String ergCode) { this.ergCode = ergCode; }
    public String getResult1() { return result1; }
    public void setResult1(String result1) { this.result1 = result1; }
    public String getResult2() { return result2; }
    public void setResult2(String result2) { this.result2 = result2; }
    public String getFault1() { return fault1; }
    public void setFault1(String fault1) { this.fault1 = fault1; }
    public String getFault2() { return fault2; }
    public void setFault2(String fault2) { this.fault2 = fault2; }
    public String getReply1() { return reply1; }
    public void setReply1(String reply1) { this.reply1 = reply1; }
    public String getReply2() { return reply2; }
    public void setReply2(String reply2) { this.reply2 = reply2; }
    public String getExplanation1() { return explanation1; }
    public void setExplanation1(String explanation1) { this.explanation1 = explanation1; }
    public String getExplanation2() { return explanation2; }
    public void setExplanation2(String explanation2) { this.explanation2 = explanation2; }
    
    public String getDemandCode() {
        return hauptgruppe;
    }
}

// Repository interfaces

interface HSFLALF1Repository extends JpaRepository<HSFLALF1Entity, Long> {
    List<HSFLALF1Entity> findByPkzAndAufnr(String pkz, String aufnr);
}

interface HSAHKLF3Repository extends JpaRepository<HSAHKLF3Entity, Long> {
    List<HSAHKLF3Entity> findByPakzAndSplitt(String pakz, String splitt);
    Optional<HSAHKLF3Entity> findByPakzAndRnr(String pakz, String rnr);
}

interface HSAHWPFRepository extends JpaRepository<HSAHWPFEntity, Long> {
}

interface HSG70FRepository extends JpaRepository<HSG70FEntity, Long> {
    Optional<HSG70FEntity> findByKzlAndRNrAndRDat(String kzl, String rnr, String rdat);
}

interface HSG71LF2Repository extends JpaRepository<HSG71LF2Entity, Long> {
    List<HSG71LF2Entity> findByPakzOrderByClaimNrAsc(String pakz);
    List<HSG71LF2Entity> findByPakzOrderByClaimNrDesc(String pakz);
    List<HSG71LF2Entity> findByRechNr(String rechNr);
    Optional<HSG71LF2Entity> findByPakzAndRechNr(String pakz, String rechNr);
}

interface HSG73PFRepository extends JpaRepository<HSG73PFEntity, Long> {
    List<HSG73PFEntity> findByPakzAndRechNr(String pakz, String rechNr);
}

interface HSGSCPRRepository extends JpaRepository<HSGSCPREntity, Long> {
    Optional<HSGSCPREntity> findByStatusCode(String statusCode);
}

interface HSBTSLRRepository extends JpaRepository<HSBTSLREntity, Long> {
    List<HSBTSLREntity> findByPakz(String pakz);
}

@Entity
@Table(name = "HSGSCPF")
class HSGSCPREntity {
    @Id
    private String statusCode;
    private String statusDescription;
    
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
}

@Entity
@Table(name = "HSBTSLF1")
class HSBTSLREntity {
    @Id
    private Long id;
    private String pakz;
    private String branchCode;
    private String branchName;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
}