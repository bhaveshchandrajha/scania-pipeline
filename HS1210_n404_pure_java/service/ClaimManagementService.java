/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for claim management operations (create, update, delete, status changes).
 */
@Service
public class ClaimManagementService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkPositionRepository workPositionRepository;
    private final HSFLALF1Repository hsflalf1Repository;
    
    @Autowired
    public ClaimManagementService(ClaimRepository claimRepository,
                                  ClaimErrorRepository claimErrorRepository,
                                  InvoiceRepository invoiceRepository,
                                  WorkPositionRepository workPositionRepository,
                                  HSFLALF1Repository hsflalf1Repository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.workPositionRepository = workPositionRepository;
        this.hsflalf1Repository = hsflalf1Repository;
    }
    
    @Transactional
    public Claim createClaimFromInvoice(String companyCode, String invoiceNumber, String invoiceDate, 
                                        String orderNumber, String workshopTheke) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceKey(companyCode, invoiceNumber, invoiceDate, orderNumber, workshopTheke);
        if (invoiceOpt.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        Claim claim = new Claim();
        claim.setCompanyCode(invoice.getCompanyCode());
        claim.setInvoiceNumber(invoice.getInvoiceNumber());
        claim.setInvoiceDate(invoice.getInvoiceDate());
        claim.setOrderNumber(invoice.getOrderNumber());
        claim.setWorkshopTheke(invoice.getWorkshopTheke());
        claim.setChassisNumber(invoice.getFahrgNr() != null && invoice.getFahrgNr().length() >= 7 
                               ? invoice.getFahrgNr().substring(0, 7) : "");
        claim.setLicensePlate(invoice.getKz());
        
        String zdat = invoice.getZdat();
        if (zdat != null && !zdat.isBlank() && !zdat.equals("0")) {
            try {
                claim.setRegistrationDate(Integer.parseInt(zdat));
            } catch (NumberFormatException e) {
                claim.setRegistrationDate(0);
            }
        } else {
            claim.setRegistrationDate(0);
        }
        
        String repairDateStr = invoice.getWorkshopTheke().equals("1") ? invoice.getFertTag() : invoice.getOrderDate();
        if (repairDateStr != null && !repairDateStr.isBlank() && !repairDateStr.equals("0")) {
            try {
                claim.setRepairDate(Integer.parseInt(repairDateStr));
            } catch (NumberFormatException e) {
                claim.setRepairDate(0);
            }
        } else {
            claim.setRepairDate(0);
        }
        
        String kmStr = invoice.getKm();
        if (kmStr != null && !kmStr.isBlank()) {
            try {
                int mileageValue = Integer.parseInt(kmStr);
                claim.setMileage(mileageValue / 1000);
            } catch (NumberFormatException e) {
                claim.setMileage(0);
            }
        } else {
            claim.setMileage(0);
        }
        
        claim.setProductType(1);
        claim.setAttachment(" ");
        claim.setForeigner(" ");
        claim.setCustomerNumber(invoice.getKundenNr());
        claim.setCustomerName(invoice.getName());
        claim.setClaimNumberSde("");
        claim.setStatusCodeSde(0);
        claim.setErrorCount(0);
        claim.setArea(invoice.getArea());
        claim.setOrderNumberSdps(invoice.getOrderNumber() + invoice.getWorkshopTheke() + invoice.getArea() + invoice.getSplit());
        
        String nextClaimNumber = generateNextClaimNumber(companyCode);
        claim.setClaimNumber(nextClaimNumber);
        
        Claim savedClaim = claimRepository.save(claim);
        
        copyWorkPositionsToClaim(savedClaim);
        copyExternalServicesToClaim(savedClaim);
        
        return savedClaim;
    }
    
    private String generateNextClaimNumber(String companyCode) {
        List<Claim> allClaims = claimRepository.findActiveClaimsByCompanyCodeDescending(companyCode);
        if (allClaims.isEmpty()) {
            return "00000001";
        }
        
        String lastClaimNumber = allClaims.get(0).getClaimNumber();
        try {
            int nextNumber = Integer.parseInt(lastClaimNumber) + 1;
            return String.format("%08d", nextNumber);
        } catch (NumberFormatException e) {
            return "00000001";
        }
    }
    
    private void copyWorkPositionsToClaim(Claim claim) {
        List<WorkPosition> workPositions = workPositionRepository.findByInvoiceKey(
            claim.getCompanyCode(), claim.getInvoiceNumber(), claim.getInvoiceDate(), 
            claim.getOrderNumber(), claim.getWorkshopTheke()
        );
        
        int positionCounter = 0;
        for (WorkPosition wp : workPositions) {
            if (wp.getDescription() == null || wp.getDescription().isBlank()) {
                continue;
            }
            
            if (wp.getRgNetto() == null || wp.getRgNetto().compareTo(java.math.BigDecimal.ZERO) == 0) {
                continue;
            }
            
            positionCounter++;
            
            ClaimError error = new ClaimError();
            error.setCompanyCode(claim.getCompanyCode());
            error.setInvoiceNumber(claim.getInvoiceNumber());
            error.setInvoiceDate(claim.getInvoiceDate());
            error.setOrderNumber(claim.getOrderNumber());
            error.setArea(claim.getArea());
            error.setClaimNumber(claim.getClaimNumber());
            error.setErrorNumber(String.format("%02d", positionCounter));
            error.setSequenceNumber("00");
            error.setErrorPart("");
            error.setMainGroup("");
            error.setSubGroup("");
            error.setDamageCode1("");
            error.setDamageCode2("");
            error.setText1(wp.getDescription());
            error.setText2("");
            error.setControlCode("");
            error.setAssessmentCode1("");
            error.setAssessmentCode2(0);
            error.setAssessmentDate(0);
            error.setCompensationMaterial(0);
            error.setCompensationLabor(0);
            error.setCompensationSpecial(0);
            error.setRequestedMaterial(java.math.BigDecimal.ZERO);
            error.setRequestedLabor(wp.getRgNetto() != null ? wp.getRgNetto() : java.math.BigDecimal.ZERO);
            error.setRequestedSpecial(java.math.BigDecimal.ZERO);
            error.setClaimType(0);
            error.setPreviousRepairDate(0);
            error.setPreviousMileage(0);
            error.setFieldTestNumber(0);
            error.setCampaignNumber("");
            error.setEps(wp.getEpsName() != null ? wp.getEpsName() : "");
            error.setStatusCode(0);
            error.setVariantCode(0);
            error.setActionCode(0);
            error.setText3("");
            error.setText4("");
            error.setErrorNumberSde("");
            error.setAttachment(" ");
            error.setSource("");
            error.setComplain("");
            error.setSymptom("");
            error.setFailure("");
            error.setLocation("");
            error.setRepair("");
            error.setResultCode("");
            error.setResult1("");
            error.setResult2("");
            error.setFault1("");
            error.setFault2("");
            error.setReply1("");
            error.setReply2("");
            error.setExplanation1("");
            error.setExplanation2("");
            
            claimErrorRepository.save(error);
        }
    }
    
    private void copyExternalServicesToClaim(Claim claim) {
        List<HSFLALF1> externalServices = hsflalf1Repository.findByOrderKey(
            claim.getCompanyCode(), claim.getInvoiceNumber(), claim.getInvoiceDate(), 
            claim.getOrderNumber(), claim.getWorkshopTheke(), 0
        );
        
        List<ClaimError> existingErrors = claimErrorRepository.findByClaimKey(claim.getCompanyCode(), claim.getClaimNumber());
        int positionCounter = existingErrors.size();
        
        for (HSFLALF1 es : externalServices) {
            if (es.getStatus() == null || !es.getStatus().equals("3")) {
                continue;
            }
            
            if (es.getVkWert() == null || es.getVkWert().compareTo(java.math.BigDecimal.ZERO) == 0) {
                continue;
            }
            
            positionCounter++;
            
            ClaimError error = new ClaimError();
            error.setCompanyCode(claim.getCompanyCode());
            error.setInvoiceNumber(claim.getInvoiceNumber());
            error.setInvoiceDate(claim.getInvoiceDate());
            error.setOrderNumber(claim.getOrderNumber());
            error.setArea(claim.getArea());
            error.setClaimNumber(claim.getClaimNumber());
            error.setErrorNumber(String.format("%02d", positionCounter));
            error.setSequenceNumber("00");
            error.setErrorPart(es.getBeschreibung() != null ? es.getBeschreibung() : "");
            error.setMainGroup("");
            error.setSubGroup("");
            error.setDamageCode1("");
            error.setDamageCode2("");
            error.setText1(es.getBeschreibung() != null ? es.getBeschreibung() : "");
            error.setText2("");
            error.setControlCode("");
            error.setAssessmentCode1("");
            error.setAssessmentCode2(0);
            error.setAssessmentDate(0);
            error.setCompensationMaterial(0);
            error.setCompensationLabor(0);
            error.setCompensationSpecial(0);
            error.setRequestedMaterial(java.math.BigDecimal.ZERO);
            error.setRequestedLabor(java.math.BigDecimal.ZERO);
            error.setRequestedSpecial(es.getVkWert() != null ? es.getVkWert() : java.math.BigDecimal.ZERO);
            error.setClaimType(0);
            error.setPreviousRepairDate(0);
            error.setPreviousMileage(0);
            error.setFieldTestNumber(0);
            error.setCampaignNumber("");
            error.setEps("");
            error.setStatusCode(0);
            error.setVariantCode(0);
            error.setActionCode(0);
            error.setText3("");
            error.setText4("");
            error.setErrorNumberSde("");
            error.setAttachment(" ");
            error.setSource("");
            error.setComplain("");
            error.setSymptom("");
            error.setFailure("");
            error.setLocation("");
            error.setRepair("");
            error.setResultCode("");
            error.setResult1("");
            error.setResult2("");
            error.setFault1("");
            error.setFault2("");
            error.setReply1("");
            error.setReply2("");
            error.setExplanation1("");
            error.setExplanation2("");
            
            claimErrorRepository.save(error);
        }
    }
    
    @Transactional
    public void deleteClaimAndErrors(String companyCode, String claimNumber) {
        Optional<Claim> claimOpt = claimRepository.findByCompanyAndClaimNumber(companyCode, claimNumber);
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        
        Claim claim = claimOpt.get();
        claim.setStatusCodeSde(99);
        claimRepository.save(claim);
        
        List<ClaimError> errors = claimErrorRepository.findByClaimKey(companyCode, claimNumber);
        claimErrorRepository.deleteAll(errors);
    }
    
    @Transactional
    public void updateClaimStatus(String companyCode, String claimNumber, int newStatus) {
        Optional<Claim> claimOpt = claimRepository.findByCompanyAndClaimNumber(companyCode, claimNumber);
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Claim not found");
        }
        
        Claim claim = claimOpt.get();
        claim.setStatusCodeSde(newStatus);
        claimRepository.save(claim);
    }
}