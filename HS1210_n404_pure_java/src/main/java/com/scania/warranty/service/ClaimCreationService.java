package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaimCreationService {
    
    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkPositionRepository workPositionRepository;
    private final HSFLALF1Repository hsflalf1Repository;
    
    @Autowired
    public ClaimCreationService(
            ClaimRepository claimRepository,
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
    
    public Claim createClaimFromInvoice(String pakz, String invoiceNumber, String invoiceDate, 
                                        String orderNumber, String area) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
                pakz, invoiceNumber, invoiceDate, orderNumber, area, "1", "04");
        
        if (invoiceOpt.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        List<Claim> existingClaims = claimRepository.findByPakzAndRechNrAndRechDatum(
                pakz, invoiceNumber, invoiceDate);
        
        for (Claim existing : existingClaims) {
            if (existing.getStatusCodeSde() != null && existing.getStatusCodeSde() != 99) {
                // Idempotent behaviour: return existing active claim instead of failing
                return existing;
            }
        }
        
        Claim claim = new Claim();
        claim.setPakz(invoice.getPakz());
        claim.setRechNr(invoice.getRnr());
        claim.setRechDatum(invoice.getRdat());
        claim.setAuftragsNr(invoice.getAnr());
        claim.setWete(invoice.getBerei());
        
        String nextClaimNumber = generateNextClaimNumber(pakz);
        claim.setClaimNr(nextClaimNumber);
        
        claim.setChassisNr(extractChassisNumber(invoice.getFahrgNr()));
        claim.setKennzeichen(invoice.getKz());
        
        claim.setZulDatum(parseDate(invoice.getZdat()));
        claim.setRepDatum(parseDate(invoice.getAnTag()));
        
        claim.setKmStand(parseKilometers(invoice.getKm()));
        claim.setProduktTyp(1);
        claim.setAnhang(" ");
        claim.setAuslaender(" ");
        
        claim.setKdNr(invoice.getKundenNr());
        claim.setKdName(invoice.getName());
        
        claim.setClaimNrSde("");
        claim.setStatusCodeSde(0);
        claim.setAnzFehler(0);
        claim.setBereich(invoice.getBerei());
        claim.setAufNr(invoice.getAnr() + invoice.getBerei() + invoice.getWt() + invoice.getSplitt());
        
        claim = claimRepository.save(claim);
        
        copyWorkPositionsToClaim(invoice, claim);
        copyExternalServicesToClaim(invoice, claim);
        
        return claim;
    }
    
    private String generateNextClaimNumber(String pakz) {
        Optional<String> maxClaimNr = claimRepository.findMaxClaimNrByPakz(pakz);
        
        if (maxClaimNr.isPresent()) {
            try {
                int nextNumber = Integer.parseInt(maxClaimNr.get()) + 1;
                return String.format("%08d", nextNumber);
            } catch (NumberFormatException e) {
                return "00000001";
            }
        }
        
        return "00000001";
    }
    
    private String extractChassisNumber(String vehicleNumber) {
        if (vehicleNumber != null && vehicleNumber.length() >= 7) {
            return vehicleNumber.substring(vehicleNumber.length() - 7);
        }
        return vehicleNumber;
    }
    
    private Integer parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(dateString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private Integer parseKilometers(String kmString) {
        if (kmString == null || kmString.isBlank()) {
            return 0;
        }
        
        try {
            int km = Integer.parseInt(kmString);
            return km / 1000;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void copyWorkPositionsToClaim(Invoice invoice, Claim claim) {
        List<WorkPosition> workPositions = workPositionRepository.findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplittOrderByPosAsc(
                invoice.getPakz(), invoice.getRnr(), invoice.getRdat(), 
                invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        int positionCounter = 1;
        for (WorkPosition workPosition : workPositions) {
            if (!shouldIncludeWorkPosition(workPosition)) {
                continue;
            }
            ClaimError claimError = new ClaimError();
            claimError.setPakz(claim.getPakz());
            claimError.setRechNr(claim.getRechNr());
            claimError.setRechDatum(claim.getRechDatum());
            claimError.setAuftragsNr(claim.getAuftragsNr());
            claimError.setBereich(claim.getBereich());
            claimError.setClaimNr(claim.getClaimNr());
            claimError.setFehlerNr(String.format("%02d", positionCounter));
            claimError.setFolgeNr("00");
            claimError.setFehlerTeil(determineItemType(workPosition));
            claimError.setText1(workPosition.getBez() != null ? workPosition.getBez() : "");
            claimError.setText2("");
            claimError.setBeantrMat(workPosition.getRgNetto() != null ? workPosition.getRgNetto() : BigDecimal.ZERO);
            claimError.setBeantrgArb(BigDecimal.ZERO);
            claimError.setBeantrgSpez(BigDecimal.ZERO);
            claimError.setStatusCode(0);
            claimError.setVergMat(0);
            claimError.setVergArb(0);
            claimError.setVergSpez(0);
            claimErrorRepository.save(claimError);
            positionCounter++;
        }
    }

    private boolean shouldIncludeWorkPosition(WorkPosition workPosition) {
        if (workPosition.getRgNetto() == null || workPosition.getRgNetto().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        if ("A".equals(workPosition.getKzSAw())) {
            return false;
        }
        if (workPosition.getlNr() != null && !workPosition.getlNr().isBlank()) {
            return false;
        }
        return true;
    }

    private String determineItemType(WorkPosition workPosition) {
        if ("8".equals(workPosition.getEc())) {
            return "TXT";
        }
        if (workPosition.getAg() != null && !workPosition.getAg().isBlank()) {
            return "ARB";
        }
        return "SAR";
    }
    
    private void copyExternalServicesToClaim(Invoice invoice, Claim claim) {
        List<HSFLALF1> externalServices = hsflalf1Repository.findByPkzAndBesNrAndBesDatAndAufnrAndBereiAndWtAndSpl(
                invoice.getPakz(), invoice.getRnr(), invoice.getRdat(), 
                invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        int positionCounter = claimErrorRepository.findByPakzAndClaimNr(claim.getPakz(), claim.getClaimNr()).size() + 1;
        for (HSFLALF1 service : externalServices) {
            if (service.getStatus() == null) {
                continue;
            }
            int status;
            try {
                status = Integer.parseInt(service.getStatus());
            } catch (NumberFormatException e) {
                continue;
            }
            if (status <= 3) {
                continue;
            }
            if (service.getVkWert() == null || service.getVkWert().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ClaimError claimError = new ClaimError();
            claimError.setPakz(claim.getPakz());
            claimError.setRechNr(claim.getRechNr());
            claimError.setRechDatum(claim.getRechDatum());
            claimError.setAuftragsNr(claim.getAuftragsNr());
            claimError.setBereich(claim.getBereich());
            claimError.setClaimNr(claim.getClaimNr());
            claimError.setFehlerNr(String.format("%02d", positionCounter));
            claimError.setFolgeNr("00");
            claimError.setFehlerTeil("SMA");
            claimError.setText1(service.getBeschreibung() != null ? service.getBeschreibung() : "");
            claimError.setText2("");
            claimError.setBeantrgSpez(service.getVkWert());
            claimError.setBeantrMat(BigDecimal.ZERO);
            claimError.setBeantrgArb(BigDecimal.ZERO);
            claimError.setStatusCode(0);
            claimError.setVergMat(0);
            claimError.setVergArb(0);
            claimError.setVergSpez(0);
            claimErrorRepository.save(claimError);
            positionCounter++;
        }
    }
    
    public void updateClaimStatus(String pakz, String claimNumber, int newStatus) {
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNumber);
        
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            claim.setStatusCodeSde(newStatus);
            claimRepository.save(claim);
        }
    }
}