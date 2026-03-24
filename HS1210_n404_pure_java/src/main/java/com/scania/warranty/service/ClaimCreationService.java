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
        // @origin HS1210 L941-941 (CHAIN)
        Optional<Invoice> invoiceOpt = invoiceRepository.findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
                pakz, invoiceNumber, invoiceDate, orderNumber, area, "1", "04");
        
        // @origin HS1210 L830-833 (IF)
        if (invoiceOpt.isEmpty()) {
            // @origin HS1210 L895-895 (EXSR)
            throw new IllegalArgumentException("Invoice not found");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        // @origin HS1210 L1027-1027 (CHAIN)
        List<Claim> existingClaims = claimRepository.findByPakzAndRechNrAndRechDatum(
                pakz, invoiceNumber, invoiceDate);
        
        // @origin HS1210 L884-1012 (DOW)
        for (Claim existing : existingClaims) {
            // @origin HS1210 L841-844 (IF)
            if (existing.getStatusCodeSde() != null && existing.getStatusCodeSde() != 99) {
                // Idempotent behaviour: return existing active claim instead of failing
                return existing;
            }
        }
        
        Claim claim = new Claim();
        // @origin HS1210 L887-887 (EVAL)
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
        
        // @origin HS1210 L860-860 (WRITE)
        claim = claimRepository.save(claim);
        
        copyWorkPositionsToClaim(invoice, claim);
        copyExternalServicesToClaim(invoice, claim);
        
        return claim;
    }
    
    private String generateNextClaimNumber(String pakz) {
        // @origin HS1210 L1035-1035 (CHAIN)
        Optional<String> maxClaimNr = claimRepository.findMaxClaimNrByPakz(pakz);
        
        // @origin HS1210 L845-848 (IF)
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
        // @origin HS1210 L1100-1100 (CHAIN)
        List<WorkPosition> workPositions = workPositionRepository.findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplittOrderByPosAsc(
                invoice.getPakz(), invoice.getRnr(), invoice.getRdat(), 
                invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        int positionCounter = 1;
        // @origin HS1210 L908-913 (DOW)
        for (WorkPosition workPosition : workPositions) {
            // @origin HS1210 L894-896 (IF)
            if (!shouldIncludeWorkPosition(workPosition)) {
                continue;
            }
            ClaimError claimError = new ClaimError();
            // @origin HS1210 L1183-1183 (EVAL)
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
            // @origin HS1210 L861-861 (WRITE)
            claimErrorRepository.save(claimError);
            positionCounter++;
        }
    }

    private boolean shouldIncludeWorkPosition(WorkPosition workPosition) {
        // @origin HS1210 L899-917 (IF)
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
        // @origin HS1210 L1106-1106 (CHAIN)
        List<HSFLALF1> externalServices = hsflalf1Repository.findByPkzAndBesNrAndBesDatAndAufnrAndBereiAndWtAndSpl(
                invoice.getPakz(), invoice.getRnr(), invoice.getRdat(), 
                invoice.getAnr(), invoice.getBerei(), invoice.getWt(), invoice.getSplitt());
        
        int positionCounter = claimErrorRepository.findByPakzAndClaimNr(claim.getPakz(), claim.getClaimNr()).size() + 1;
        // @origin HS1210 L1028-1036 (DOW)
        for (HSFLALF1 service : externalServices) {
            // @origin HS1210 L919-996 (IF)
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
            // @origin HS1210 L1618-1618 (EVAL)
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
            // @origin HS1210 L989-989 (WRITE)
            claimErrorRepository.save(claimError);
            positionCounter++;
        }
    }
    
    public void updateClaimStatus(String pakz, String claimNumber, int newStatus) {
        // @origin HS1210 L1135-1135 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNumber);
        
        // @origin HS1210 L959-970 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            // @origin HS1210 L1746-1746 (EVAL)
            claim.setStatusCodeSde(newStatus);
            // @origin HS1210 L990-990 (WRITE)
            claimRepository.save(claim);
        }
    }
}