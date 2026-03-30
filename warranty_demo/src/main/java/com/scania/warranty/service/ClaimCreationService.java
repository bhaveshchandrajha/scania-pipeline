/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClaimCreationService {

    private final InvoiceRepository invoiceRepository; // @rpg-trace: n958
    private final ClaimRepository claimRepository; // @rpg-trace: n958
    private final LaborRepository laborRepository; // @rpg-trace: n958
    private final ExternalServiceRepository externalServiceRepository; // @rpg-trace: n958
    private final ClaimErrorRepository claimErrorRepository; // @rpg-trace: n958
    private final FailedClaimService failedClaimService;
    private final int maxRepairAgeDays;

    public ClaimCreationService(InvoiceRepository invoiceRepository, ClaimRepository claimRepository, LaborRepository laborRepository, ExternalServiceRepository externalServiceRepository, ClaimErrorRepository claimErrorRepository, FailedClaimService failedClaimService,
                                @Value("${warranty.claim.max-repair-age-days:19}") int maxRepairAgeDays) {
        this.invoiceRepository = invoiceRepository;
        this.claimRepository = claimRepository;
        this.laborRepository = laborRepository;
        this.externalServiceRepository = externalServiceRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.failedClaimService = failedClaimService;
        this.maxRepairAgeDays = maxRepairAgeDays;
    }

    public String createClaimFromInvoice(String companyCode, String invoiceNumber, String invoiceDate, String workshopCode, String serviceType) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByKey(companyCode, invoiceNumber, invoiceDate, workshopCode); // @rpg-trace: n984

        // Fallback: findByKey uses ahk020=invoiceDate; seed may use ahk080 for order date. Try findByCompany + filter.
        if (invoiceOpt.isEmpty()) {
            invoiceOpt = invoiceRepository.findByCompany(companyCode).stream()
                    .filter(i -> invoiceNumber.equals(i.getAhk010())
                            && workshopCode.equals(i.getAhk040())
                            && (invoiceDate.equals(i.getAhk020()) || invoiceDate.equals(i.getAhk080())))
                    .findFirst();
        }

        if (invoiceOpt.isEmpty()) { // @rpg-trace: n984
            throw new IllegalArgumentException("Invoice not found: " + companyCode + "/" + invoiceNumber + "/" + invoiceDate + "/" + workshopCode); // @rpg-trace: n985
        }

        Invoice invoice = invoiceOpt.get(); // @rpg-trace: n984

        validateRepairAgeOrRecordFailure(invoice);

        List<Invoice> cancellations = invoiceRepository.findStornoByKey(companyCode, invoiceDate, invoiceNumber); // @rpg-trace: n975
        if (!cancellations.isEmpty()) { // @rpg-trace: n977
            throw new IllegalArgumentException("Invoice has been cancelled"); // @rpg-trace: n978
        }

        List<Claim> existingClaims = claimRepository.findByInvoiceKeyPartial(companyCode, invoiceNumber, invoiceDate, workshopCode); // @rpg-trace: n964
        for (Claim existing : existingClaims) { // @rpg-trace: n965
            if (existing.getG71170() != 99) { // @rpg-trace: n967
                throw new IllegalArgumentException("Claim already exists for this invoice"); // @rpg-trace: n968
            }
        }

        String nextClaimNumber = generateNextClaimNumber(companyCode); // @rpg-trace: n1091

        Claim claim = new Claim(); // @rpg-trace: n990
        claim.setG71000(invoice.getAhk000()); // @rpg-trace: n990
        claim.setG71010(invoice.getAhk010()); // @rpg-trace: n990
        claim.setG71020(invoice.getAhk080()); // @rpg-trace: n990
        claim.setG71030(invoice.getAhk040()); // @rpg-trace: n990
        claim.setG71040(invoice.getAhk060()); // @rpg-trace: n990
        claim.setG71060(invoice.getAhk510().trim()); // @rpg-trace: n995
        claim.setG71070(invoice.getAhk520()); // @rpg-trace: n998
        claim.setG71080(new BigDecimal(invoice.getAhk550())); // @rpg-trace: n1008
        claim.setG71090(parseRepairDateAsBigDecimal(invoice)); // @rpg-trace: n998
        claim.setG71100(parseMileageAsBigDecimal(invoice.getAhk815())); // @rpg-trace: n1009
        claim.setG71110(determineVehicleTypeAsBigDecimal(invoice)); // @rpg-trace: n1021
        claim.setG71140(invoice.getAhk230()); // @rpg-trace: n1070
        claim.setG71150(invoice.getAhk250()); // @rpg-trace: n1071
        claim.setG71120(" "); // @rpg-trace: required
        claim.setG71130(" "); // @rpg-trace: required
        claim.setG71160(""); // @rpg-trace: n1071
        claim.setG71170(0); // @rpg-trace: n1073
        claim.setG71180(0); // @rpg-trace: n1081
        claim.setG71190(""); // @rpg-trace: n1082
        claim.setG71200(invoice.getAhk040() + invoice.getAhk050() + invoice.getAhk060() + invoice.getAhk070()); // @rpg-trace: n1088
        claim.setG71050(nextClaimNumber); // @rpg-trace: n1110

        claimRepository.save(claim); // @rpg-trace: n1109

        return nextClaimNumber; // @rpg-trace: n1114
    }

    private String generateNextClaimNumber(String companyCode) {
        Optional<String> maxClaimNumber = claimRepository.findMaxClaimNrByCompany(companyCode); // @rpg-trace: n1093
        if (maxClaimNumber.isPresent()) { // @rpg-trace: n1094
            String lastNumber = maxClaimNumber.get(); // @rpg-trace: n1094
            long nextNumber = Long.parseLong(lastNumber) + 1; // @rpg-trace: n1092
            return String.format("%08d", nextNumber); // @rpg-trace: n1092
        } else { // @rpg-trace: n1096
            return "00000001"; // @rpg-trace: n1104
        }
    }

    private Integer parseRegistrationDate(String registrationDate) {
        if (registrationDate == null || registrationDate.isBlank()) { // @rpg-trace: n1008
            return 0; // @rpg-trace: n1008
        }
        try {
            return Integer.parseInt(registrationDate); // @rpg-trace: n1008
        } catch (NumberFormatException e) {
            return 0; // @rpg-trace: n1008
        }
    }

    private void validateRepairAgeOrRecordFailure(Invoice invoice) {
        Optional<LocalDate> repairOpt = repairDateAsLocalDate(invoice);
        if (repairOpt.isEmpty()) {
            return;
        }
        long daysPast = ChronoUnit.DAYS.between(repairOpt.get(), LocalDate.now());
        if (daysPast > maxRepairAgeDays) {
            String invDate = invoice.getAhk080() != null ? invoice.getAhk080() : invoice.getAhk020();
            failedClaimService.recordFailure(
                invoice.getAhk000(),
                invoice.getAhk010(),
                invDate != null ? invDate : "",
                invoice.getAhk040(),
                "Repair date is " + daysPast + " days before today (maximum allowed is " + maxRepairAgeDays + " days)",
                (int) daysPast
            );
            throw new IllegalArgumentException(
                "Claim rejected: repair date is more than " + maxRepairAgeDays + " days in the past.");
        }
    }

    private Optional<LocalDate> repairDateAsLocalDate(Invoice invoice) {
        BigDecimal bd = parseRepairDateAsBigDecimal(invoice);
        if (bd == null || bd.signum() <= 0) {
            return Optional.empty();
        }
        try {
            long v = bd.longValue();
            if (v < 19000101L || v > 29991231L) {
                return Optional.empty();
            }
            String s = String.format("%08d", v);
            return Optional.of(LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private BigDecimal parseRepairDateAsBigDecimal(Invoice invoice) {
        String repairDate; // @rpg-trace: n999
        if ("1".equals(invoice.getAhk060())) { // @rpg-trace: n999
            if ("04".substring(1, 2).equals("4")) { // @rpg-trace: n1000
                repairDate = invoice.getAhk600(); // @rpg-trace: n1001
            } else { // @rpg-trace: n1003
                repairDate = invoice.getAhk595(); // @rpg-trace: n1003
            }
        } else { // @rpg-trace: n1006
            repairDate = invoice.getAhk080(); // @rpg-trace: n1006
        }

        if (repairDate == null || repairDate.equals("0")) { // @rpg-trace: n1008
            repairDate = invoice.getAhk080(); // @rpg-trace: n1008
        }

        try {
            return new BigDecimal(repairDate); // @rpg-trace: n1008
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO; // @rpg-trace: n1008
        }
    }

    private BigDecimal parseMileageAsBigDecimal(BigDecimal mileage) {
        try {
            if (mileage == null) return BigDecimal.ZERO;
            BigDecimal result = mileage.divide(new BigDecimal("1000"), java.math.RoundingMode.HALF_UP); // @rpg-trace: n1012
            return result.compareTo(new BigDecimal("999")) > 0 ? new BigDecimal("999") : result; // G71100 NUMERIC(3,0) max 999
        } catch (Exception e) {
            return BigDecimal.ZERO; // @rpg-trace: n1015
        }
    }

    private BigDecimal determineVehicleTypeAsBigDecimal(Invoice invoice) {
        String vehicleTypeCode = invoice.getAhk540(); // @rpg-trace: n1021
        if (vehicleTypeCode != null && vehicleTypeCode.startsWith("M")) { // @rpg-trace: n1021
            return new BigDecimal(3); // @rpg-trace: n1024
        }
        return BigDecimal.ONE; // @rpg-trace: n1026
    }
}