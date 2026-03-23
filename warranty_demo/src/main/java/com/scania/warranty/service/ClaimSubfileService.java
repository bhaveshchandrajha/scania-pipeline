/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimSubfileService {

    private final ClaimRepository claimRepository; // @rpg-trace: n428
    private final ClaimErrorRepository claimErrorRepository; // @rpg-trace: n457
    private final InvoiceRepository invoiceRepository; // @rpg-trace: n984
    private final LaborRepository laborRepository; // @rpg-trace: n1213
    private final ExternalServiceRepository externalServiceRepository; // @rpg-trace: n1320
    private final ClaimReleaseRequestRepository claimReleaseRequestRepository; // @rpg-trace: n1708

    public ClaimSubfileService(ClaimRepository claimRepository, ClaimErrorRepository claimErrorRepository, InvoiceRepository invoiceRepository, LaborRepository laborRepository, ExternalServiceRepository externalServiceRepository, ClaimReleaseRequestRepository claimReleaseRequestRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.invoiceRepository = invoiceRepository;
        this.laborRepository = laborRepository;
        this.externalServiceRepository = externalServiceRepository;
        this.claimReleaseRequestRepository = claimReleaseRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<ClaimListItemDto> buildSubfileList(String companyCode, boolean ascending, Integer filterAgeDays, String filterType, String filterStatus, String filterCompany, String filterChassis, String filterCustomer, String filterSdeNumber, boolean filterOpenOnly, String searchString) { // @rpg-trace: n428
        List<Claim> claims; // @rpg-trace: n428
        if (ascending) { // @rpg-trace: n428
            claims = claimRepository.findActiveClaimsByCompanyAsc(companyCode); // @rpg-trace: n428
        } else { // @rpg-trace: n433
            claims = claimRepository.findActiveClaimsByCompanyDesc(companyCode); // @rpg-trace: n433
        }

        List<ClaimListItemDto> result = new ArrayList<>(); // @rpg-trace: n436
        LocalDate currentDate = LocalDate.now(); // @rpg-trace: n439
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // @rpg-trace: n439

        for (Claim claim : claims) { // @rpg-trace: n436
            if (filterAgeDays != null && filterAgeDays > 0 && claim.getG71170() != 99) { // @rpg-trace: n439
                try {
                    LocalDate claimDate = LocalDate.parse(String.valueOf(claim.getG71090()), formatter); // @rpg-trace: n441
                    long daysDifference = ChronoUnit.DAYS.between(claimDate, currentDate); // @rpg-trace: n442
                    if (daysDifference > filterAgeDays) { // @rpg-trace: n443
                        claim.setG71170(99); // @rpg-trace: n443
                        continue; // @rpg-trace: n443
                    }
                } catch (Exception e) { // @rpg-trace: n439
                    continue; // @rpg-trace: n439
                }
            }

            if (filterType != null && !filterType.isBlank() && claim.getG71170() != 99) { // @rpg-trace: n447
                boolean typeMatches = checkClaimType(claim, filterType); // @rpg-trace: n448
                if (!typeMatches) { // @rpg-trace: n447
                    claim.setG71170(99); // @rpg-trace: n447
                    continue; // @rpg-trace: n447
                }
            }

            if (filterOpenOnly) { // @rpg-trace: n452
                boolean isOpen = checkOpenClaim(claim); // @rpg-trace: n452
                if (!isOpen) { // @rpg-trace: n471
                    claim.setG71170(99); // @rpg-trace: n471
                    continue; // @rpg-trace: n471
                }
            }

            if (claim.getG71170() != 99) { // @rpg-trace: n471
                List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050());
                ClaimListItemDto dto = new ClaimListItemDto(
                    claim.getG71000(),
                    claim.getG71010(),
                    formatDate(claim.getG71020()),
                    claim.getG71030(),
                    claim.getG71040(),
                    claim.getG71050(),
                    claim.getG71060(),
                    claim.getG71070(),
                    claim.getG71090() != null ? claim.getG71090().toString() : "",
                    claim.getG71100() != null ? claim.getG71100().toString() : "",
                    claim.getG71140() != null ? claim.getG71140() : "",
                    claim.getG71150() != null ? claim.getG71150() : "",
                    claim.getG71160() != null ? claim.getG71160() : "",
                    claim.getG71170(),
                    resolveStatusText(claim),
                    claim.getG71190() != null ? claim.getG71190() : "",
                    claim.getG71200() != null ? claim.getG71200() : "",
                    errors.size()
                ); // @rpg-trace: n509
                result.add(dto); // @rpg-trace: n509
            }
        }

        return result; // @rpg-trace: n523
    }

    @Transactional(readOnly = true)
    public List<Claim> loadSubfileData(String companyCode, boolean ascending, Integer filterAgeDays, String filterType, String filterOpen) { // @rpg-trace: n428
        List<Claim> claims; // @rpg-trace: n428
        if (ascending) { // @rpg-trace: n428
            claims = claimRepository.findActiveClaimsByCompanyAsc(companyCode); // @rpg-trace: n428
        } else { // @rpg-trace: n433
            claims = claimRepository.findActiveClaimsByCompanyDesc(companyCode); // @rpg-trace: n433
        }

        List<Claim> filteredClaims = new ArrayList<>(); // @rpg-trace: n436
        LocalDate currentDate = LocalDate.now(); // @rpg-trace: n439
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // @rpg-trace: n439

        for (Claim claim : claims) { // @rpg-trace: n436
            if (filterAgeDays != null && filterAgeDays > 0 && claim.getG71170() != 99) { // @rpg-trace: n439
                try {
                    LocalDate claimDate = LocalDate.parse(String.valueOf(claim.getG71090()), formatter); // @rpg-trace: n441
                    long daysDifference = ChronoUnit.DAYS.between(claimDate, currentDate); // @rpg-trace: n442
                    if (daysDifference > filterAgeDays) { // @rpg-trace: n443
                        claim.setG71170(99); // @rpg-trace: n443
                        continue; // @rpg-trace: n443
                    }
                } catch (Exception e) { // @rpg-trace: n439
                    continue; // @rpg-trace: n439
                }
            }

            if (filterType != null && !filterType.isBlank() && claim.getG71170() != 99) { // @rpg-trace: n447
                boolean typeMatches = checkClaimType(claim, filterType); // @rpg-trace: n448
                if (!typeMatches) { // @rpg-trace: n447
                    claim.setG71170(99); // @rpg-trace: n447
                    continue; // @rpg-trace: n447
                }
            }

            if ("J".equals(filterOpen)) { // @rpg-trace: n452
                boolean isOpen = checkOpenClaim(claim); // @rpg-trace: n452
                if (!isOpen) { // @rpg-trace: n471
                    claim.setG71170(99); // @rpg-trace: n471
                    continue; // @rpg-trace: n471
                }
            }

            if (claim.getG71170() != 99) { // @rpg-trace: n471
                filteredClaims.add(claim); // @rpg-trace: n509
            }
        }

        return filteredClaims; // @rpg-trace: n523
    }

    private String formatDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank() || yyyyMMdd.length() < 8) return "";
        String year = yyyyMMdd.substring(0, 4);
        String month = yyyyMMdd.substring(4, 6);
        String day = yyyyMMdd.substring(6, 8);
        if ("0000".equals(year)) return "";
        return day + "." + month + "." + year;
    }

    private String resolveStatusText(Claim claim) {
        if ("00000000".equals(claim.getG71160())) {
            if (claim.getG71170() == ClaimStatus.MINIMUM.getCode()) {
                return "Minimumantrag";
            } else if (claim.getG71170() == ClaimStatus.APPROVED.getCode()) {
                return "Minimum ausgebucht";
            } else {
                return "Minimumantrag";
            }
        }
        return "Status " + claim.getG71170();
    }

    private boolean checkClaimType(Claim claim, String filterType) { // @rpg-trace: n448
        List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n1745
        for (ClaimError error : errors) { // @rpg-trace: n1748
            String demandCode = error.getG73140(); // @rpg-trace: n1748
            if ("K".equals(filterType)) { // @rpg-trace: n1750
                return true; // @rpg-trace: n1754
            } else if ("G".equals(filterType)) { // @rpg-trace: n1757
                return true; // @rpg-trace: n1759
            } else { // @rpg-trace: n1762
                return true; // @rpg-trace: n1765
            }
        }
        return false; // @rpg-trace: n1770
    }

    private boolean checkOpenClaim(Claim claim) { // @rpg-trace: n452
        boolean isOpen = false; // @rpg-trace: n451
        if (claim.getG71170() < 20 && claim.getG71170() != 5) { // @rpg-trace: n454
            isOpen = true; // @rpg-trace: n454
        }
        List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n457
        if (errors.isEmpty()) { // @rpg-trace: n459
            isOpen = true; // @rpg-trace: n459
        }
        for (ClaimError error : errors) { // @rpg-trace: n463
            if (error.getG73290() == 0) { // @rpg-trace: n464
                isOpen = true; // @rpg-trace: n464
            }
        }
        return isOpen; // @rpg-trace: n471
    }

    @Transactional
    public void updateClaimStatus(String companyCode, String claimNumber, Integer newStatus) { // @rpg-trace: n654
        Optional<Claim> claimOpt = claimRepository.findByCompanyAndClaimNr(companyCode, claimNumber); // @rpg-trace: n654
        if (claimOpt.isPresent()) { // @rpg-trace: n657
            Claim claim = claimOpt.get(); // @rpg-trace: n657
            if (claim.getG71170() == 2) { // @rpg-trace: n657
                claim.setG71170(3); // @rpg-trace: n658
                claimRepository.save(claim); // @rpg-trace: n658
            }
        }
    }

    @Transactional
    public void deleteClaimAndRelatedData(String companyCode, String claimNumber) { // @rpg-trace: n587
        Optional<Claim> claimOpt = claimRepository.findByCompanyAndClaimNr(companyCode, claimNumber); // @rpg-trace: n587
        if (claimOpt.isPresent()) { // @rpg-trace: n587
            Claim claim = claimOpt.get(); // @rpg-trace: n587
            claim.setG71170(99); // @rpg-trace: n589
            claimRepository.save(claim); // @rpg-trace: n589

            List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(companyCode, claimNumber); // @rpg-trace: n592
            for (ClaimError error : errors) { // @rpg-trace: n593
                claimErrorRepository.delete(error); // @rpg-trace: n593
            }
        }
    }

    @Transactional
    public void createClaimFromInvoice(String companyCode, String invoiceNumber, String invoiceDate, String branchCode, String workshopType) { // @rpg-trace: n984
        Optional<Invoice> invoiceOpt = invoiceRepository.findByKey(companyCode, invoiceNumber, invoiceDate, branchCode); // @rpg-trace: n984
        if (!invoiceOpt.isPresent()) { // @rpg-trace: n985
            throw new IllegalArgumentException("Invoice not found"); // @rpg-trace: n987
        }

        Invoice invoice = invoiceOpt.get(); // @rpg-trace: n984
        List<Claim> existingClaims = claimRepository.findByInvoiceKeyPartial(companyCode, invoiceNumber, invoiceDate, branchCode); // @rpg-trace: n965
        for (Claim existing : existingClaims) { // @rpg-trace: n966
            if (existing.getG71170() != 99) { // @rpg-trace: n967
                throw new IllegalArgumentException("Claim already exists for this invoice"); // @rpg-trace: n968
            }
        }

        Claim newClaim = new Claim(); // @rpg-trace: n990
        newClaim.setG71000(invoice.getAhk000()); // @rpg-trace: n990
        newClaim.setG71010(invoice.getAhk010()); // @rpg-trace: n990
        newClaim.setG71020(invoice.getAhk020()); // @rpg-trace: n990
        newClaim.setG71030(invoice.getAhk040()); // @rpg-trace: n990
        newClaim.setG71040(invoice.getAhk060()); // @rpg-trace: n990
        newClaim.setG71060(invoice.getAhk510().trim()); // @rpg-trace: n995
        newClaim.setG71070(invoice.getAhk520()); // @rpg-trace: n995
        newClaim.setG71080(new BigDecimal(invoice.getAhk550())); // @rpg-trace: n998
        newClaim.setG71090(new BigDecimal(invoice.getAhk080())); // @rpg-trace: n1006
        newClaim.setG71100(BigDecimal.ZERO); // @rpg-trace: n1018
        newClaim.setG71110(BigDecimal.ONE); // @rpg-trace: n1021
        newClaim.setG71140(invoice.getAhk230()); // @rpg-trace: n1070
        newClaim.setG71150(invoice.getAhk250()); // @rpg-trace: n1070
        newClaim.setG71160(""); // @rpg-trace: n1071
        newClaim.setG71170(0); // @rpg-trace: n1079
        newClaim.setG71180(0); // @rpg-trace: n1081
        newClaim.setG71190(""); // @rpg-trace: n1084
        newClaim.setG71200(invoice.getAhk040() + invoice.getAhk050() + invoice.getAhk060() + invoice.getAhk070()); // @rpg-trace: n1088

        String nextClaimNumber = generateNextClaimNumber(companyCode); // @rpg-trace: n1092
        newClaim.setG71050(nextClaimNumber); // @rpg-trace: n1110

        claimRepository.save(newClaim); // @rpg-trace: n1112

        List<Labor> laborRecords = laborRepository.findByInvoiceKey(companyCode, invoiceNumber, invoiceDate, branchCode, workshopType); // @rpg-trace: n1213
        for (Labor labor : laborRecords) { // @rpg-trace: n1213
        }

        List<ExternalService> externalServices = externalServiceRepository.findExternalServicesForClaim(companyCode, invoiceDate, branchCode); // @rpg-trace: n1320
        for (ExternalService service : externalServices) { // @rpg-trace: n1323
        }
    }

    private String generateNextClaimNumber(String companyCode) { // @rpg-trace: n1092
        List<Claim> allClaims = claimRepository.findAllByCompanyAsc(companyCode); // @rpg-trace: n1104
        int maxClaimNumber = 0; // @rpg-trace: n1104
        for (Claim claim : allClaims) { // @rpg-trace: n1104
            try {
                int claimNum = Integer.parseInt(claim.getG71050()); // @rpg-trace: n1104
                if (claimNum > maxClaimNumber) { // @rpg-trace: n1104
                    maxClaimNumber = claimNum; // @rpg-trace: n1104
                }
            } catch (NumberFormatException e) { // @rpg-trace: n1104
            }
        }
        return String.format("%08d", maxClaimNumber + 1); // @rpg-trace: n1110
    }

    @Transactional
    public void requestClaimRelease(String companyCode, String invoiceNumber, String invoiceDate) { // @rpg-trace: n1708
        Optional<ClaimReleaseRequest> existingRequest = claimReleaseRequestRepository.findByG70KzlAndG70RnrAndG70Rdat(companyCode, invoiceNumber, invoiceDate); // @rpg-trace: n1708
        if (!existingRequest.isPresent()) { // @rpg-trace: n1709
            ClaimReleaseRequest newRequest = new ClaimReleaseRequest(); // @rpg-trace: n1711
            newRequest.setG70Kzl(companyCode); // @rpg-trace: n1711
            newRequest.setG70Rnr(invoiceNumber); // @rpg-trace: n1712
            newRequest.setG70Rdat(invoiceDate); // @rpg-trace: n1713
            newRequest.setG70Fgnr(""); // @rpg-trace: n1714
            newRequest.setG70Dat(""); // @rpg-trace: n1715
            newRequest.setG70Status(""); // @rpg-trace: n1722

            Optional<Invoice> invoiceOpt = invoiceRepository.findByKey(companyCode, invoiceNumber, invoiceDate, ""); // @rpg-trace: n1717
            if (invoiceOpt.isPresent()) { // @rpg-trace: n1718
                Invoice invoice = invoiceOpt.get(); // @rpg-trace: n1718
                newRequest.setG70Fgnr(invoice.getAhk510()); // @rpg-trace: n1719
                newRequest.setG70Dat(invoice.getAhk620()); // @rpg-trace: n1720
            }

            claimReleaseRequestRepository.save(newRequest); // @rpg-trace: n1723
        }
    }
}