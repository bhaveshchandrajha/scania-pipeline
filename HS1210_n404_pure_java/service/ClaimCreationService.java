package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimCreationService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkPositionRepository workPositionRepository;
    private final ExternalServiceRepository externalServiceRepository;
    private final SubmissionDeadlineReleaseRepository submissionDeadlineReleaseRepository;
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ClaimCreationService(ClaimRepository claimRepository,
                                ClaimErrorRepository claimErrorRepository,
                                InvoiceRepository invoiceRepository,
                                WorkPositionRepository workPositionRepository,
                                ExternalServiceRepository externalServiceRepository,
                                SubmissionDeadlineReleaseRepository submissionDeadlineReleaseRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.workPositionRepository = workPositionRepository;
        this.externalServiceRepository = externalServiceRepository;
        this.submissionDeadlineReleaseRepository = submissionDeadlineReleaseRepository;
    }

    @Transactional
    public String createClaimFromInvoice(String companyCode, String invoiceNumber, 
                                        String invoiceDate, String orderNumber, 
                                        String workshopCounter, String split) {
        
        Optional<Invoice> invoiceOpt = invoiceRepository.findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndWorkshopCounterAndSplit(
            companyCode, invoiceNumber, invoiceDate, orderNumber, workshopCounter, split);
        
        if (invoiceOpt.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        List<Claim> existingClaims = claimRepository.findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndWorkshopCounter(
            companyCode, invoiceNumber, invoiceDate, orderNumber, workshopCounter);
        
        for (Claim existingClaim : existingClaims) {
            if (existingClaim.getStatusCodeSde() != null && existingClaim.getStatusCodeSde() != 99) {
                throw new IllegalStateException("Claim already exists for this invoice");
            }
        }
        
        String newClaimNumber = generateNextClaimNumber(companyCode);
        
        Claim claim = new Claim();
        claim.setCompanyCode(invoice.getCompanyCode());
        claim.setInvoiceNumber(invoice.getInvoiceNumber());
        claim.setInvoiceDate(invoice.getInvoiceDate());
        claim.setOrderNumber(invoice.getOrderNumber());
        claim.setWorkshopCounter(invoice.getWorkshopCounter());
        claim.setClaimNumber(newClaimNumber);
        claim.setChassisNumber(extractChassisNumber(invoice.getVehicleNumber()));
        claim.setLicensePlate(invoice.getLicensePlate());
        claim.setRegistrationDate(parseDate(invoice.getRegistrationDate()));
        claim.setRepairDate(parseRepairDate(invoice));
        claim.setMileage(calculateMileage(invoice.getKilometers()));
        claim.setProductType(determineProductType(invoice.getVehicleCategory()));
        claim.setAttachment(" ");
        claim.setForeigner(determineForeignerStatus(invoice));
        claim.setCustomerNumber(invoice.getCustomerNumber());
        claim.setCustomerName(invoice.getName());
        claim.setClaimNumberSde("");
        claim.setStatusCodeSde(0);
        claim.setErrorCount(0);
        claim.setArea(invoice.getArea());
        claim.setJobNumber(buildJobNumber(invoice));
        
        claimRepository.save(claim);
        
        copyWorkPositionsToClaim(invoice, claim);
        copyExternalServicesToClaim(invoice, claim);
        
        return newClaimNumber;
    }

    private String generateNextClaimNumber(String companyCode) {
        Optional<Claim> lastClaim = claimRepository.findFirstByCompanyCodeOrderByClaimNumberDesc(companyCode);
        
        if (lastClaim.isEmpty()) {
            return "00000001";
        }
        
        try {
            long lastNumber = Long.parseLong(lastClaim.get().getClaimNumber());
            long nextNumber = lastNumber + 1;
            return String.format("%08d", nextNumber);
        } catch (NumberFormatException e) {
            return "00000001";
        }
    }

    private String extractChassisNumber(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.length() < 7) {
            return "";
        }
        return vehicleNumber.substring(vehicleNumber.length() - 7);
    }

    private Integer parseDate(String dateString) {
        if (dateString == null || dateString.isBlank() || dateString.equals("0")) {
            return 0;
        }
        try {
            return Integer.parseInt(dateString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer parseRepairDate(Invoice invoice) {
        if ("1".equals(invoice.getWorkshopCounter())) {
            String repairDate = invoice.getAcceptanceDate();
            if (repairDate == null || repairDate.isBlank() || repairDate.equals("0")) {
                repairDate = invoice.getOrderDate();
            }
            return parseDate(repairDate);
        } else {
            return parseDate(invoice.getOrderDate());
        }
    }

    private Integer calculateMileage(String kilometers) {
        if (kilometers == null || kilometers.isBlank()) {
            return 0;
        }
        try {
            long km = Long.parseLong(kilometers);
            return (int) (km / 1000);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer determineProductType(String vehicleCategory) {
        if (vehicleCategory == null || vehicleCategory.isBlank()) {
            return 1;
        }
        
        if (vehicleCategory.startsWith("M")) {
            return 3;
        }
        
        return 1;
    }

    private String determineForeignerStatus(Invoice invoice) {
        return "A".equals(invoice.getSalutation()) ? "Y" : " ";
    }

    private String buildJobNumber(Invoice invoice) {
        return invoice.getOrderNumber() + invoice.getWorkshopCounter() + 
               invoice.getArea() + invoice.getSplit();
    }

    private void copyWorkPositionsToClaim(Invoice invoice, Claim claim) {
        List<WorkPosition> workPositions = workPositionRepository.findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndAreaAndWorkshopCounterAndSplit(
            invoice.getCompanyCode(), invoice.getInvoiceNumber(), invoice.getInvoiceDate(),
            invoice.getOrderNumber(), invoice.getArea(), invoice.getWorkshopCounter(), invoice.getSplit());
        
        int positionCounter = 1;
        
        for (WorkPosition workPosition : workPositions) {
            if (shouldIncludeWorkPosition(workPosition)) {
                ClaimError claimError = new ClaimError();
                claimError.setCompanyCode(claim.getCompanyCode());
                claimError.setInvoiceNumber(claim.getInvoiceNumber());
                claimError.setInvoiceDate(claim.getInvoiceDate());
                claimError.setOrderNumber(claim.getOrderNumber());
                claimError.setArea(claim.getArea());
                claimError.setClaimNumber(claim.getClaimNumber());
                claimError.setErrorNumber(String.format("%02d", positionCounter));
                claimError.setSequenceNumber("00");
                
                String itemType = determineItemType(workPosition);
                claimError.setFaultyPart(itemType);
                
                claimError.setText1(workPosition.getDescription());
                claimError.setText2("");
                
                claimError.setRequestedLabor(workPosition.getInvoiceNet() != null ? workPosition.getInvoiceNet() : BigDecimal.ZERO);
                claimError.setRequestedMaterial(BigDecimal.ZERO);
                claimError.setRequestedSpecial(BigDecimal.ZERO);
                
                claimError.setStatusCode(0);
                claimError.setCompensationLabor(0);
                claimError.setCompensationMaterial(0);
                claimError.setCompensationSpecial(0);
                
                claimErrorRepository.save(claimError);
                positionCounter++;
            }
        }
    }

    private boolean shouldIncludeWorkPosition(WorkPosition workPosition) {
        if (workPosition.getInvoiceNet() == null || workPosition.getInvoiceNet().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        
        if ("A".equals(workPosition.getIndicatorSAw())) {
            return false;
        }
        
        if (workPosition.getLineNumber() != null && !workPosition.getLineNumber().isBlank()) {
            return false;
        }
        
        return true;
    }

    private String determineItemType(WorkPosition workPosition) {
        if ("8".equals(workPosition.getEntryCode())) {
            return "TXT";
        }
        
        if (workPosition.getOperationCode() != null && !workPosition.getOperationCode().isBlank()) {
            return "ARB";
        }
        
        return "SAR";
    }

    private void copyExternalServicesToClaim(Invoice invoice, Claim claim) {
        List<ExternalService> externalServices = externalServiceRepository.findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndJobNumberAndAreaAndWorkshopCounterAndSplit(
            invoice.getCompanyCode(), invoice.getInvoiceNumber(), invoice.getInvoiceDate(),
            invoice.getOrderNumber(), invoice.getArea(), invoice.getWorkshopCounter(), invoice.getSplit());
        
        int positionCounter = claimErrorRepository.findByCompanyCodeAndClaimNumber(
            claim.getCompanyCode(), claim.getClaimNumber()).size() + 1;
        
        for (ExternalService externalService : externalServices) {
            if (shouldIncludeExternalService(externalService)) {
                ClaimError claimError = new ClaimError();
                claimError.setCompanyCode(claim.getCompanyCode());
                claimError.setInvoiceNumber(claim.getInvoiceNumber());
                claimError.setInvoiceDate(claim.getInvoiceDate());
                claimError.setOrderNumber(claim.getOrderNumber());
                claimError.setArea(claim.getArea());
                claimError.setClaimNumber(claim.getClaimNumber());
                claimError.setErrorNumber(String.format("%02d", positionCounter));
                claimError.setSequenceNumber("00");
                
                claimError.setFaultyPart("SMA");
                claimError.setText1(externalService.getDescription());
                claimError.setText2("");
                
                claimError.setRequestedSpecial(externalService.getSalesValue() != null ? externalService.getSalesValue() : BigDecimal.ZERO);
                claimError.setRequestedLabor(BigDecimal.ZERO);
                claimError.setRequestedMaterial(BigDecimal.ZERO);
                
                claimError.setStatusCode(0);
                claimError.setCompensationLabor(0);
                claimError.setCompensationMaterial(0);
                claimError.setCompensationSpecial(0);
                
                claimErrorRepository.save(claimError);
                positionCounter++;
            }
        }
    }

    private boolean shouldIncludeExternalService(ExternalService externalService) {
        if (externalService.getStatus() == null || !"3".equals(externalService.getStatus())) {
            return false;
        }
        
        if (externalService.getSalesValue() == null || externalService.getSalesValue().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        return true;
    }
}