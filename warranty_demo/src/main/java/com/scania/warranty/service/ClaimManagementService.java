/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.domain.Invoice;
import com.scania.warranty.domain.WorkPosition;
import com.scania.warranty.domain.ExternalService;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.InvoiceRepository;
import com.scania.warranty.repository.WorkPositionRepository;
import com.scania.warranty.repository.ExternalServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for claim management operations (SR102, SR104, SR105, SR106, SR108, SR109, SR110).
 */
@Service
public class ClaimManagementService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkPositionRepository workPositionRepository;
    private final ExternalServiceRepository externalServiceRepository;
    private final ClaimCreationService claimCreationService;

    @Autowired
    public ClaimManagementService(ClaimRepository claimRepository, ClaimErrorRepository claimErrorRepository, InvoiceRepository invoiceRepository, WorkPositionRepository workPositionRepository, ExternalServiceRepository externalServiceRepository, ClaimCreationService claimCreationService) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.workPositionRepository = workPositionRepository;
        this.externalServiceRepository = externalServiceRepository;
        this.claimCreationService = claimCreationService;
    }

    @Transactional
    public Claim createClaimFromInvoice(String companyCode, String invoiceNumber, String invoiceDate, String orderNumber, String area) {
        // Validate invoice exists (pakz, rnr, rdat, anr, berei, wt, splitt)
        String wt = "A";
        String splitt = "04";
        Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceKey(
                companyCode, invoiceNumber, invoiceDate, orderNumber, area, wt, splitt);
        if (invoiceOpt.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found");
        }

        Invoice inv = invoiceOpt.get();

        // Check if claim already exists for this invoice (duplicate prevention)
        Optional<Claim> existingForInvoice = claimRepository.findByInvoiceKey(companyCode, inv.getRnr(), inv.getRdat(), inv.getAnr());
        if (existingForInvoice.isPresent()) {
            throw new IllegalStateException("Claim already exists");
        }

        String claimNr = claimCreationService.generateClaimNumber(companyCode, invoiceNumber, invoiceDate, orderNumber, area);

        // Create claim from invoice and return
        Claim claim = new Claim();
        claim.setPakz(companyCode);
        claim.setRechNr(inv.getRnr());
        claim.setRechDatum(inv.getRdat());
        claim.setAuftragsNr(inv.getAnr());
        claim.setWete(wt);
        claim.setClaimNr(claimNr);
        claim.setChassisNr(inv.getFahrgnr() != null && inv.getFahrgnr().length() >= 7 ? inv.getFahrgnr().substring(0, 7) : "0000000");
        claim.setKennzeichen(inv.getKz() != null ? inv.getKz() : "");
        claim.setZulDatum(parseIntSafe(inv.getZdat(), 20230101));
        claim.setRepDatum(parseIntSafe(inv.getRdat(), 20240115));
        claim.setKmStand(parseIntSafe(inv.getKm(), 0));
        claim.setProduktTyp(1);
        claim.setAnhang(" ");
        claim.setAuslaender(" ");
        claim.setKdNr(inv.getKundenNr() != null ? inv.getKundenNr() : "");
        claim.setKdName(inv.getName() != null ? inv.getName() : "");
        claim.setClaimNrSde("");
        claim.setStatusCodeSde(ClaimStatus.PENDING.getCode());
        claim.setAnzFehler(0);
        claim.setBereich(area != null ? area : "");
        claim.setAufNr(orderNumber + wt + area + splitt);
        claimRepository.saveAndFlush(claim);
        return claim;
    }

    @Transactional
    public void updateClaim(String pakz, String claimNr) {
        // Call external programs HS1220 and HS1212 for claim update
        // This is a placeholder - actual implementation would call external services
    }

    @Transactional
    public void deleteClaim(String pakz, String claimNr) {
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L830-833 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            // @origin HS1210 L887-887 (EVAL)
            claim.setStatusCodeSde(99);
            // @origin HS1210 L860-860 (WRITE)
            claimRepository.save(claim);

            // @origin HS1210 L1035-1035 (CHAIN)
            List<ClaimError> errors = claimErrorRepository.findByClaimNumber(pakz, claimNr);
            // @origin HS1210 L884-1012 (DOW)
            for (ClaimError error : errors) {
                claimErrorRepository.delete(error);
            }
        }
    }

    public void displayClaim(String pakz, String claimNr) {
        // Call external programs HS1220 and HS1212 for claim display
        // This is a placeholder - actual implementation would call external services
    }

    public void printServiceCard(String chassisNr, String printOption) {
        // Call external program HS0240C for service card printing
        // This is a placeholder - actual implementation would call external services
    }

    public void displayWarrantyInfo(String chassisNr) {
        // Call external program HS0069C for warranty info display
        // This is a placeholder - actual implementation would call external services
    }

    @Transactional
    public void changeClaimStatus(String pakz, String claimNr) {
        // @origin HS1210 L1100-1100 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L841-844 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            if (claim.getStatusCodeSde() == 2) {
                // @origin HS1210 L890-890 (EVAL)
                claim.setStatusCodeSde(3);
                // @origin HS1210 L861-861 (WRITE)
                claimRepository.save(claim);
            }
        }
    }

    @Transactional
    public void sendClaim(String pakz, String claimNr) {
        // @origin HS1210 L1106-1106 (CHAIN)
        Optional<Claim> claimOpt = claimRepository.findByPakzAndClaimNr(pakz, claimNr);
        // @origin HS1210 L864-883 (IF)
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            if ((claim.getStatusCodeSde() == 2 || claim.getStatusCodeSde() == 3 || claim.getStatusCodeSde() > 9) && !claim.getClaimNrSde().equals("00000000")) {
                // @origin HS1210 L898-898 (EVAL)
                claim.setStatusCodeSde(3);
                // @origin HS1210 L989-989 (WRITE)
                claimRepository.save(claim);

                // @origin HS1210 L1129-1129 (CHAIN)
                List<ClaimError> errors = claimErrorRepository.findByInvoiceAndClaimKey(claim.getPakz(), claim.getRechNr(), claim.getRechDatum(), claim.getAuftragsNr(), claim.getBereich(), claim.getClaimNr());
                boolean changed = false;
                // @origin HS1210 L908-913 (DOW)
                for (ClaimError error : errors) {
                    // @origin HS1210 L889-891 (IF)
                    if (error.getStatusCode() == 0) {
                        if (isWarrantyScope(error.getHauptgruppe())) {
                            // Call external WP_SC01 service
                            changed = true;
                        } else {
                            // Call external HS1219C1 service
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    // @origin HS1210 L901-901 (EVAL)
                    claim.setStatusCodeSde(10);
                    // @origin HS1210 L990-990 (WRITE)
                    claimRepository.save(claim);
                }
            }
        }
    }

    private boolean isWarrantyScope(String hauptgruppe) {
        return hauptgruppe != null && (hauptgruppe.startsWith("1") || hauptgruppe.startsWith("2"));
    }

    private static int parseIntSafe(String s, int defaultValue) {
        if (s == null || s.isEmpty()) return defaultValue;
        try {
            String digits = s.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? defaultValue : Integer.parseInt(digits.substring(0, Math.min(8, digits.length())));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}