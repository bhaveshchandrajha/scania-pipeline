package com.scania.warranty.claim.service;

import com.scania.warranty.claim.entity.*;
import com.scania.warranty.claim.repository.*;
import com.scania.warranty.claim.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HS1212 - Warranty Claim Management Service
 * Migrated from RPG program HS1212
 * Purpose: Manage warranty claims - View and Edit
 */
@Service
@Transactional
public class HS1212ClaimService {

    // Repositories
    private final HSG71PFRepository hsg71Repository;
    private final HSG73PFRepository hsg73Repository;
    private final HSGPSPFRepository hsgpsRepository;
    private final S3F003Repository s3f003Repository;
    private final HSGSCPFRepository hsgscRepository;
    private final AUFWSKFRepository aufwskRepository;
    private final HSAHKPFRepository hsahkRepository;
    private final HSEPALRepository hsepalRepository;
    private final S3F018Repository s3f018Repository;
    private final S3F019Repository s3f019Repository;
    private final S3F085Repository s3f085Repository;
    private final S3F084Repository s3f084Repository;
    private final ITLSMFRepository itlsmfRepository;
    private final PIPPMSTRepository pippmstRepository;
    private final WPEPCFRepository wpepcfRepository;
    private final CWPMSGFRepository cwpmsgfRepository;
    private final CWPMSRFRepository cwpmsrfRepository;
    private final HSGPTFRepository hsgptfRepository;
    private final SDE001Repository sde001Repository;
    private final S3L091ARepository s3l091aRepository;
    private final S3F009Repository s3f009Repository;
    private final HSBTSLFRepository hsbtslfRepository;
    private final FARSTLFRepository farstlfRepository;

    // External service calls
    private final ExternalServiceAdapter externalServiceAdapter;
    private final CWPServiceAdapter cwpServiceAdapter;
    private final EPSServiceAdapter epsServiceAdapter;

    // Constants
    private static final String TYP1 = "1 = Original Mont.  ";
    private static final String TYP2 = "2 = Orig. teil mont.";
    private static final String TYP3 = "4 = Orig. ab Lager  ";
    private static final String TYP4 = "6 = IRM Vertrag     ";
    private static final String TYP5 = "8 = SP. Maintenance ";
    private static final String TYP6 = "9 = EPC             ";

    // Working variables
    private LocalDate aktdat = LocalDate.now();
    private String pkz;
    private String locale;
    private int tage = 14;

    public HS1212ClaimService(
            HSG71PFRepository hsg71Repository,
            HSG73PFRepository hsg73Repository,
            HSGPSPFRepository hsgpsRepository,
            S3F003Repository s3f003Repository,
            HSGSCPFRepository hsgscRepository,
            AUFWSKFRepository aufwskRepository,
            HSAHKPFRepository hsahkRepository,
            HSEPALRepository hsepalRepository,
            S3F018Repository s3f018Repository,
            S3F019Repository s3f019Repository,
            S3F085Repository s3f085Repository,
            S3F084Repository s3f084Repository,
            ITLSMFRepository itlsmfRepository,
            PIPPMSTRepository pippmstRepository,
            WPEPCFRepository wpepcfRepository,
            CWPMSGFRepository cwpmsgfRepository,
            CWPMSRFRepository cwpmsrfRepository,
            HSGPTFRepository hsgptfRepository,
            SDE001Repository sde001Repository,
            S3L091ARepository s3l091aRepository,
            S3F009Repository s3f009Repository,
            HSBTSLFRepository hsbtslfRepository,
            FARSTLFRepository farstlfRepository,
            ExternalServiceAdapter externalServiceAdapter,
            CWPServiceAdapter cwpServiceAdapter,
            EPSServiceAdapter epsServiceAdapter) {
        this.hsg71Repository = hsg71Repository;
        this.hsg73Repository = hsg73Repository;
        this.hsgpsRepository = hsgpsRepository;
        this.s3f003Repository = s3f003Repository;
        this.hsgscRepository = hsgscRepository;
        this.aufwskRepository = aufwskRepository;
        this.hsahkRepository = hsahkRepository;
        this.hsepalRepository = hsepalRepository;
        this.s3f018Repository = s3f018Repository;
        this.s3f019Repository = s3f019Repository;
        this.s3f085Repository = s3f085Repository;
        this.s3f084Repository = s3f084Repository;
        this.itlsmfRepository = itlsmfRepository;
        this.pippmstRepository = pippmstRepository;
        this.wpepcfRepository = wpepcfRepository;
        this.cwpmsgfRepository = cwpmsgfRepository;
        this.cwpmsrfRepository = cwpmsrfRepository;
        this.hsgptfRepository = hsgptfRepository;
        this.sde001Repository = sde001Repository;
        this.s3l091aRepository = s3l091aRepository;
        this.s3f009Repository = s3f009Repository;
        this.hsbtslfRepository = hsbtslfRepository;
        this.farstlfRepository = farstlfRepository;
        this.externalServiceAdapter = externalServiceAdapter;
        this.cwpServiceAdapter = cwpServiceAdapter;
        this.epsServiceAdapter = epsServiceAdapter;
    }

    /**
     * Main entry point - Load and manage claim
     */
    public ClaimManagementResponse manageClaim(ClaimManagementRequest request) {
        // Initialize
        initializeSession(request);

        // Load claim header
        HSG71PF claimHeader = loadClaimHeader(
                request.getDealerId(),
                request.getCustomerId(),
                request.getClaimDate(),
                request.getOrderNumber(),
                request.getClaimType()
        );

        if (claimHeader == null) {
            return ClaimManagementResponse.notFound();
        }

        // Determine area (Bereich)
        String bereich = determineBereich(claimHeader);

        // Load claim details
        List<HSG73PF> claimDetails = loadClaimDetails(claimHeader);

        // Check if minimum claim
        boolean isMinimumClaim = "00000000".equals(claimHeader.getG71160());

        // Load positions
        PositionsData positions = readPositions(
                claimHeader.getG71000(),
                claimHeader.getG71050()
        );

        // Build response
        ClaimManagementResponse response = new ClaimManagementResponse();
        response.setClaimHeader(convertToDTO(claimHeader));
        response.setClaimDetails(convertDetailsToDTOs(claimDetails));
        response.setPositions(positions);
        response.setBereich(bereich);
        response.setMinimumClaim(isMinimumClaim);

        return response;
    }

    /**
     * Create or update claim failure
     */
    public ClaimFailureResponse createOrUpdateFailure(ClaimFailureRequest request) {
        // Load claim header
        HSG71PF claimHeader = hsg71Repository.findById(
                new HSG71PFKey(
                        request.getDealerId(),
                        request.getCustomerId(),
                        request.getClaimDate(),
                        request.getOrderNumber(),
                        request.getClaimType()
                )
        ).orElseThrow(() -> new ClaimNotFoundException("Claim not found"));

        // Determine next failure number
        String failureNumber = determineNextFailureNumber(claimHeader);

        // Validate claim data
        ValidationResult validation = validateClaimData(request, claimHeader);
        if (!validation.isValid()) {
            return ClaimFailureResponse.validationError(validation.getErrors());
        }

        // Check for campaigns
        String campaignResult = checkCampaigns(claimHeader, request);
        if (campaignResult != null) {
            return ClaimFailureResponse.campaignHandled(campaignResult);
        }

        // Load or create failure record
        HSG73PF failure = loadOrCreateFailure(claimHeader, failureNumber, request);

        // Update percentages based on demand code
        updatePercentages(failure, request);

        // Auto-assign positions if new
        if (request.isAutoAssign()) {
            autoAssignPositions(claimHeader, failure);
        }

        // Save text
        saveClaimText(failure, request.getTextLines());

        // Save failure
        hsg73Repository.save(failure);

        // Update header
        updateClaimHeader(claimHeader, failure);

        return ClaimFailureResponse.success(convertToDTO(failure));
    }

    /**
     * Delete claim failure
     */
    public void deleteFailure(String dealerId, String claimNo, String failNo) {
        HSG73PFKey key = new HSG73PFKey(dealerId, claimNo, failNo, "00");
        HSG73PF failure = hsg73Repository.findById(key)
                .orElseThrow(() -> new ClaimNotFoundException("Failure not found"));

        // Delete positions
        deleteFailurePositions(dealerId, claimNo, failNo);

        // Delete failure
        hsg73Repository.delete(failure);

        // Update header count
        HSG71PF header = hsg71Repository.findById(
                new HSG71PFKey(dealerId, failure.getG73010(), failure.getG73020(),
                        failure.getG73030(), failure.getG73040())
        ).orElse(null);

        if (header != null) {
            header.setG71180(header.getG71180() - 1);
            hsg71Repository.save(header);
        }
    }

    /**
     * Load claim details for display
     */
    public ClaimDetailResponse loadClaimForDisplay(String dealerId, String claimNo, String failNo) {
        HSG73PFKey key = new HSG73PFKey(dealerId, claimNo, failNo, "00");
        HSG73PF failure = hsg73Repository.findById(key)
                .orElseThrow(() -> new ClaimNotFoundException("Failure not found"));

        ClaimDetailResponse response = new ClaimDetailResponse();
        response.setFailure(convertToDTO(failure));

        // Load descriptions
        loadDescriptions(failure, response);

        // Load CWP codes if applicable
        if (isWarrScope(failure.getG73140())) {
            loadCWPCodes(failure, response);
        }

        // Load claim values
        BigDecimal requested = getClaimValues("Requested", dealerId, claimNo, failNo, "00");
        BigDecimal compensated = getClaimValues("Compensated", dealerId, claimNo, failNo, "00");
        response.setRequestedAmount(requested);
        response.setCompensatedAmount(compensated);

        // Load credit note info
        String creditNote = getCreditNoteNumber(dealerId, claimNo, failNo, failure.getG73140());
        response.setCreditNoteNumber(creditNote);

        return response;
    }

    /**
     * Check if claim data is valid
     */
    private ValidationResult validateClaimData(ClaimFailureRequest request, HSG71PF header) {
        ValidationResult result = new ValidationResult();

        // Validate product type
        if (request.getProductType() < 1 || request.getProductType() > 5) {
            result.addError("IN50", "Invalid product type");
        }

        // Validate chassis number
        if (request.getChassisNo() != null && !request.getChassisNo().trim().isEmpty()) {
            boolean chassisValid = validateChassisNumber(
                    request.getChassisNo(),
                    request.getProductType()
            );
            if (!chassisValid && !isEPCOrV4Claim(header)) {
                result.addError("IN51", "Invalid chassis number");
            }
        }

        // Validate repair date
        if (request.getRepairDate() == null || !isValidDate(request.getRepairDate())) {
            result.addError("IN52", "Invalid repair date");
        }

        // Validate mileage
        if (request.getChassisNo() != null && !request.getChassisNo().trim().isEmpty()) {
            if (request.getMileage() <= 0) {
                result.addError("IN53", "Mileage must be greater than 0");
            }
        } else {
            if (request.getMileage() != 0) {
                result.addError("IN54", "Mileage must be 0 when no chassis number");
            }
        }

        // Validate claim type
        if (!isValidClaimType(request.getClaimType())) {
            result.addError("IN50", "Invalid claim type");
        }

        // Validate demand code
        if (request.getDemandCode() != null && !request.getDemandCode().trim().isEmpty()) {
            if (!validateDemandCode(request, header)) {
                result.addError("IN58", "Invalid demand code");
            }
        }

        // Validate main group
        if (request.getMainGroup() != null && !request.getMainGroup().trim().isEmpty()) {
            if (!validateMainGroup(request.getMainGroup())) {
                result.addError("IN55", "Invalid main group");
            }
        }

        // Validate damage code
        if (request.getDamageCode() != null && !request.getDamageCode().trim().isEmpty()) {
            if (!validateDamageCode(request.getDamageCode())) {
                result.addError("IN57", "Invalid damage code");
            }
        }

        // Validate damaged part
        if (request.getDamagedPart() != null && !request.getDamagedPart().trim().isEmpty()) {
            if (request.getDamagedPart().length() > 8) {
                result.addError("IN56", "Part number too long");
            }
        }

        // Validate text
        if (isTextEmpty(request.getTextLines())) {
            result.addError("IN54", "Description required");
        }

        // Validate EPC claim
        if ("9".equals(request.getClaimType()) && request.getDemandCode() != null &&
                !request.getDemandCode().trim().isEmpty()) {
            result.addError("IN75", "EPC claim must have blank demand code");
        }

        // Check CWP codes if warranty scope
        if (isWarrScope(request.getDemandCode())) {
            ValidationResult cwpValidation = validateCWPCodes(request);
            result.merge(cwpValidation);
        }

        return result;
    }

    /**
     * Determine next failure number
     */
    private String determineNextFailureNumber(HSG71PF header) {
        List<HSG73PF> failures = hsg73Repository.findByClaimHeader(
                header.getG71000(),
                header.getG71010(),
                header.getG71020(),
                header.getG71030(),
                header.getG71040()
        );

        int maxFailNo = 0;
        for (HSG73PF failure : failures) {
            int failNo = Integer.parseInt(failure.getG73060());
            if (failNo > maxFailNo) {
                maxFailNo = failNo;
            }
        }

        int nextFailNo = maxFailNo + 1;
        return String.format("%02d", nextFailNo);
    }

    /**
     * Load or create failure record
     */
    private HSG73PF loadOrCreateFailure(HSG71PF header, String failNo, ClaimFailureRequest request) {
        HSG73PFKey key = new HSG73PFKey(header.getG71000(), header.getG71050(), failNo, "00");
        Optional<HSG73PF> existing = hsg73Repository.findById(key);

        HSG73PF failure;
        if (existing.isPresent()) {
            failure = existing.get();
        } else {
            failure = new HSG73PF();
            failure.setG73000(header.getG71000());
            failure.setG73010(header.getG71010());
            failure.setG73020(header.getG71020());
            failure.setG73030(header.getG71030());
            failure.setG73040(header.getG71040());
            failure.setG73050(header.getG71050());
            failure.setG73060(failNo);
            failure.setG73065("0" + failNo.substring(0, 1));
            failure.setG73290(0);
        }

        // Update from request
        failure.setG73070(request.getDamagedPart());
        failure.setG73080(request.getMainGroup());
        failure.setG73100(request.getDamageCode());
        failure.setG73140(request.getDemandCode());
        failure.setG73240(Integer.parseInt(request.getClaimType()));
        failure.setG73280(request.getCampaignNumber());

        // Set previous repair data
        if (request.getPreviousRepairDate() != null) {
            failure.setG73250(convertToISODate(request.getPreviousRepairDate()));
        }
        failure.setG73260(request.getPreviousMileage());

        // Set text
        failure.setG73120(getTextLine(request.getTextLines(), 0));
        failure.setG73130(getTextLine(request.getTextLines(), 1));
        failure.setG73320(getTextLine(request.getTextLines(), 2));
        failure.setG73330(getTextLine(request.getTextLines(), 3));

        // Set CWP codes
        failure.setG73360(request.getCodeC1());
        failure.setG73370(request.getCodeC2());
        failure.setG73380(request.getCodeC3());
        failure.setG73390(request.getCodeC4());
        failure.setG73400(request.getCodeC5());
        failure.setG73410(request.getCodeC6());
        failure.setG73285(request.getEpsName());

        return failure;
    }

    /**
     * Update percentages based on demand code
     */
    private void updatePercentages(HSG73PF failure, ClaimFailureRequest request) {
        if (request.getDemandCode() == null || request.getDemandCode().trim().isEmpty()) {
            failure.setG73180(100);
            failure.setG73190(100);
            failure.setG73200(100);
            return;
        }

        // Get percentages from S3F085
        Optional<S3F085> demandCode = s3f085Repository.findById(request.getDemandCode());
        if (demandCode.isPresent()) {
            S3F085 dc = demandCode.get();
            if (dc.getDFTAWF() == 0) {
                failure.setG73180(dc.getDMCPWF());
                failure.setG73190(dc.getDLCPWF());
                failure.setG73200(dc.getDSCPWF());
            } else {
                // Check S3F084 for age-based percentages
                int age = calculateClaimAge(failure, request);
                Optional<S3F084> agePerc = s3f084Repository.findByAge(dc.getDFTAWF(), age);
                if (agePerc.isPresent()) {
                    S3F084 ap = agePerc.get();
                    failure.setG73180(ap.getDMCPWE());
                    failure.setG73190(ap.getDLCPWE());
                    failure.setG73200(ap.getDSCPWE());
                } else {
                    failure.setG73180(0);
                    failure.setG73190(0);
                    failure.setG73200(0);
                }
            }
        }

        // Override with EPS percentages if applicable
        if (request.getEpsName() != null && !request.getEpsName().trim().isEmpty()) {
            applyEPSPercentages(failure, request);
        }
    }

    /**
     * Apply EPS percentages
     */
    private void applyEPSPercentages(HSG73PF failure, ClaimFailureRequest request) {
        String epsType = epsServiceAdapter.getEpsType(request.getEpsName(), request.getChassisNo());
        BigDecimal epsComp = epsServiceAdapter.getEpsComp(request.getEpsName(), request.getChassisNo());

        if (epsComp != null) {
            int comp = epsComp.intValue();
            switch (epsType) {
                case "1":
                    failure.setG73180(comp);
                    failure.setG73190(comp);
                    failure.setG73200(comp);
                    break;
                case "2":
                    failure.setG73180(comp);
                    failure.setG73190(0);
                    failure.setG73200(0);
                    break;
                case "3":
                    failure.setG73180(0);
                    failure.setG73190(comp);
                    failure.setG73200(comp);
                    break;
                default:
                    failure.setG73180(comp);
                    failure.setG73190(comp);
                    failure.setG73200(comp);
                    break;
            }
        }
    }

    /**
     * Calculate claim age in months
     */
    private int calculateClaimAge(HSG73PF failure, ClaimFailureRequest request) {
        try {
            LocalDate repairDate = request.getRepairDate();
            LocalDate compareDate;

            if ("1".equals(request.getClaimType())) {
                // Original mounting - compare to registration date
                compareDate = request.getRegistrationDate();
            } else {
                // Compare to previous repair date
                compareDate = request.getPreviousRepairDate();
            }

            if (repairDate != null && compareDate != null) {
                long months = ChronoUnit.MONTHS.between(compareDate, repairDate);
                return (int) Math.min(months, 99);
            }
        } catch (Exception e) {
            // Return default
        }
        return 99;
    }

    /**
     * Auto-assign positions to failure
     */
    private void autoAssignPositions(HSG71PF header, HSG73PF failure) {
        externalServiceAdapter.callHS1217(
                header.getG71000(),
                header.getG71010(),
                header.getG71020(),
                header.getG71030(),
                header.getG71040(),
                header.getG71050(),
                failure.getG73060(),
                "J"
        );
    }

    /**
     * Save claim text
     */
    private void saveClaimText(HSG73PF failure, List<String> textLines) {
        // Delete existing text
        hsgptfRepository.deleteByClaimAndFailure(
                failure.getG73000(),
                failure.getG73050(),
                failure.getG73060()
        );

        // Save new text
        for (int i = 0; i < textLines.size() && i < 99; i++) {
            String text = textLines.get(i);
            if (text != null && !text.trim().isEmpty()) {
                HSGPTF textRecord = new HSGPTF();
                textRecord.setGPT_KZL(failure.getG73000());
                textRecord.setGPT_CNR(failure.getG73050());
                textRecord.setGPT_FNR(failure.getG73060());
                textRecord.setGPT_WNR(failure.getG73065());
                textRecord.setGPT_ZNR(i + 1);
                textRecord.setGPT_TXT(text);
                hsgptfRepository.save(textRecord);
            }
        }
    }

    /**
     * Update claim header after failure change
     */
    private void updateClaimHeader(HSG71PF header, HSG73PF failure) {
        // Count failures
        long failureCount = hsg73Repository.countByClaimHeader(
                header.getG71000(),
                header.getG71010(),
                header.getG71020(),
                header.getG71030(),
                header.getG71040()
        );

        header.setG71180((int) failureCount);

        // Update status
        if (header.getG71170() < 2) {
            header.setG71170(1);
        }

        hsg71Repository.save(header);
    }

    /**
     * Delete failure positions
     */
    private void deleteFailurePositions(String dealerId, String claimNo, String failNo) {
        List<HSGPSPF> positions = hsgpsRepository.findByClaimAndFailure(dealerId, claimNo, failNo);

        for (HSGPSPF position : positions) {
            if (position.getGPS130() != null && !position.getGPS130().trim().isEmpty()) {
                // Marked position - just clear failure reference
                position.setGPS020("");
                position.setGPS025("");
                position.setGPS035(0);
                hsgpsRepository.save(position);
            } else {
                // Delete position
                hsgpsRepository.delete(position);
            }
        }
    }

    /**
     * Check for campaigns
     */
    private String checkCampaigns(HSG71PF header, ClaimFailureRequest request) {
        String returnValue = externalServiceAdapter.callHS1213(
                header.getG71000(),
                header.getG71010(),
                header.getG71020(),
                header.getG71030(),
                determineBereich(header),
                header.getG71040(),
                header.getG71060(),
                header.getG71050(),
                request.getFailureNumber(),
                "00"
        );

        return returnValue;
    }

    /**
     * Read positions for claim
     */
    private PositionsData readPositions(String dealerId, String claimNo) {
        PositionsData data = new PositionsData();

        List<Object[]> results = hsgpsRepository.findDistinctPositionNumbers(dealerId, claimNo);

        List<Integer> lineNumbers = new ArrayList<>();
        for (Object[] result : results) {
            if (result[0] != null) {
                lineNumbers.add((Integer) result[0]);
            }
        }

        data.setLineNumbers(lineNumbers);
        return data;
    }

    /**
     * Load claim header
     */
    private HSG71PF loadClaimHeader(String dealerId, String customerId, String claimDate,
                                    String orderNumber, String claimType) {
        HSG71PFKey key = new HSG71PFKey(dealerId, customerId, claimDate, orderNumber, claimType);
        return hsg71Repository.findById(key).orElse(null);
    }

    /**
     * Load claim details
     */
    private List<HSG73PF> loadClaimDetails(HSG71PF header) {
        return hsg73Repository.findByClaimHeader(
                header.getG71000(),
                header.getG71010(),
                header.getG71020(),
                header.getG71030(),
                header.getG71040()
        );
    }

    /**
     * Determine area (Bereich)
     */
    private String determineBereich(HSG71PF header) {
        if (header.getG71190() != null && !header.getG71190().trim().isEmpty()) {
            return header.getG71190();
        }
        if (header.getG71110() == 2) {
            return "3"; // Bus
        }
        if (header.getG71110() == 3) {
            return "6"; // Engine
        }
        return "1"; // Truck
    }

    /**
     * Validate chassis number
     */
    private boolean validateChassisNumber(String chassisNo, int productType) {
        return pippmstRepository.existsByChassisAndType(chassisNo, productType);
    }

    /**
     * Validate demand code
     */
    private boolean validateDemandCode(ClaimFailureRequest request, HSG71PF header) {
        return externalServiceAdapter.chkDmdCode(
                request.getDemandCode(),
                header.getG71200().substring(7, 9),
                String.valueOf(header.getG71110()),
                header.getG71060(),
                String.valueOf(header.getG71090())
        );
    }

    /**
     * Validate main group
     */
    private boolean validateMainGroup(String mainGroup) {
        try {
            int mg = Integer.parseInt(mainGroup);
            return s3f018Repository.existsById(mg);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate damage code
     */
    private boolean validateDamageCode(String damageCode) {
        try {
            int dc = Integer.parseInt(damageCode);
            return s3f019Repository.existsById(dc);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate CWP codes
     */
    private ValidationResult validateCWPCodes(ClaimFailureRequest request) {
        ValidationResult result = new ValidationResult();

        // C1 - Failure
        if (request.getCodeC1() == null || request.getCodeC1().trim().isEmpty()) {
            if (!"3".equals(request.getCodeC1())) { // Campaign exception
                result.addError("IN66", "Code C1 required");
            }
        }

        // C2 - Failure area
        if (request.getCodeC2() == null || request.getCodeC2().trim().isEmpty()) {
            if (!"3".equals(request.getCodeC1())) {
                result.addError("IN67", "Code C2 required");
            }
        }

        // C3 - Symptom
        if (request.getCodeC3() == null || request.getCodeC3().trim().isEmpty()) {
            if (!"3".equals(request.getCodeC1())) {
                result.addError("IN68", "Code C3 required");
            }
        }

        return result;
    }

    /**
     * Get claim values (requested/compensated)
     */
    private BigDecimal getClaimValues(String valueType, String dealerId, String claimNo,
                                      String failNo, String reconNo) {
        return externalServiceAdapter.getClaimValues(valueType, dealerId, claimNo, failNo, reconNo);
    }

    /**
     * Get credit note number
     */
    private String getCreditNoteNumber(String dealerId, String claimNo, String failNo, String demandCode) {
        String scope = cwpServiceAdapter.getScope(demandCode);

        if (scope != null && scope.startsWith("A")) {
            // Aftermarket
            return s3l091aRepository.findCreditNote(claimNo, failNo, dealerId);
        } else if (scope != null && scope.startsWith("R")) {
            // R&W
            return s3l091aRepository.findCreditNote(claimNo, failNo, dealerId);
        } else {
            // Warranty
            return s3l091aRepository.findCreditNote(claimNo, failNo, dealerId);
        }
}
}
