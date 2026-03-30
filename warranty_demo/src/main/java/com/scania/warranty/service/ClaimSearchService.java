/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.dto.*;
import com.scania.warranty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClaimSearchService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final ExternalServiceRepository externalServiceRepository;
    private final ClaimReleaseRequestRepository claimReleaseRequestRepository;
    private final InvoiceRepository invoiceRepository;

    public ClaimSearchService(ClaimRepository claimRepository,
                              ClaimErrorRepository claimErrorRepository,
                              ExternalServiceRepository externalServiceRepository,
                              ClaimReleaseRequestRepository claimReleaseRequestRepository,
                              InvoiceRepository invoiceRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.externalServiceRepository = externalServiceRepository;
        this.claimReleaseRequestRepository = claimReleaseRequestRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<ClaimListItemDto> searchClaims(ClaimSearchRequestDto request) {
        List<Claim> claims; // @rpg-trace: n436
        String pkz = request.companyCode(); // @rpg-trace: n428

        if (request.sortByClaimNr()) { // @rpg-trace: n428
            claims = request.sortDirection() == SortDirection.ASCENDING
                ? claimRepository.findActiveClaimsByCompanyAsc(pkz) // @rpg-trace: n429
                : claimRepository.findActiveClaimsByCompanyDesc(pkz); // @rpg-trace: n433
        } else {
            claims = request.sortDirection() == SortDirection.ASCENDING
                ? claimRepository.findAllByCompanyAsc(pkz) // @rpg-trace: n430
                : claimRepository.findAllByCompanyDesc(pkz); // @rpg-trace: n434
        }

        return claims.stream()
            .filter(claim -> claim.getG71000().equals(pkz)) // @rpg-trace: n437
            .filter(claim -> applyAgeFilter(claim, request.filterAgeDays())) // @rpg-trace: n439
            .filter(claim -> applyTypeFilter(claim, request.filterType())) // @rpg-trace: n447
            .filter(claim -> applyOpenFilter(claim, request.filterOpenOnly())) // @rpg-trace: n452
            .filter(claim -> claim.getG71170() != ClaimStatus.EXCLUDED.getCode()) // @rpg-trace: n471
            .filter(claim -> applyStatusFilter(claim, request.status(), request.statusCompareSign())) // @rpg-trace: n490
            .filter(claim -> applySearchFilter(claim, request.searchString())) // @rpg-trace: n501
            .filter(claim -> applyBranchFilter(claim, request.filterBranch(), pkz)) // @rpg-trace: n503
            .filter(claim -> applyCustomerFilter(claim, request.filterCustomer())) // @rpg-trace: n505
            .filter(claim -> applySdeFilter(claim, request.filterSdeClaimNr())) // @rpg-trace: n506
            .map(claim -> mapToListItem(claim)) // @rpg-trace: n509
            .limit(9999) // @rpg-trace: n436
            .collect(Collectors.toList());
    }

    private boolean applyAgeFilter(Claim claim, int filterAgeDays) {
        if (filterAgeDays == 0 || claim.getG71170() == ClaimStatus.EXCLUDED.getCode()) { // @rpg-trace: n439
            return true;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // @rpg-trace: n440
            LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getG71090()), formatter); // @rpg-trace: n441
            long daysBetween = ChronoUnit.DAYS.between(repairDate, LocalDate.now()); // @rpg-trace: n442
            if (daysBetween > filterAgeDays) { // @rpg-trace: n443
                return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private boolean applyTypeFilter(Claim claim, String filterType) {
        if (filterType == null || filterType.isBlank() || claim.getG71170() == ClaimStatus.EXCLUDED.getCode()) { // @rpg-trace: n447
            return true;
        }
        List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n1744
        if (errors.isEmpty()) { // @rpg-trace: n1770
            return false;
        }
        FilterOption option = FilterOption.fromCode(filterType); // @rpg-trace: n1748
        for (ClaimError error : errors) { // @rpg-trace: n1745
            if (option == FilterOption.KULANZ) { // @rpg-trace: n1750
                return true; // simplified - would need S3F085 lookup
            } else if (option == FilterOption.GARANTIE) { // @rpg-trace: n1757
                return true; // simplified - would need isWarrScope check
            } else { // @rpg-trace: n1762
                if (filterType.equals(error.getG73140() != null ? error.getG73140().substring(0, 1) : "")) { // @rpg-trace: n1763
                    return true;
                }
            }
        }
        return false;
    }

    private boolean applyOpenFilter(Claim claim, boolean filterOpenOnly) {
        if (!filterOpenOnly) { // @rpg-trace: n452
            return true;
        }
        boolean open = false; // @rpg-trace: n451
        if (claim.getG71170() < 20 && claim.getG71170() != ClaimStatus.MINIMUM.getCode()) { // @rpg-trace: n453
            open = true; // @rpg-trace: n454
        }
        List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n456
        if (errors.isEmpty()) { // @rpg-trace: n458
            open = true; // @rpg-trace: n459
        }
        for (ClaimError error : errors) { // @rpg-trace: n461
            if (error.getG73290() == 0) { // @rpg-trace: n463
                open = true; // @rpg-trace: n464
            }
        }
        if (!open) { // @rpg-trace: n467
            return false;
        }
        return true;
    }

    private boolean applyStatusFilter(Claim claim, String status, String compareSign) {
        if (status == null || status.isBlank()) { // @rpg-trace: n490
            return true;
        }
        String sign = (compareSign == null || compareSign.isBlank()) ? "=" : compareSign; // @rpg-trace: n488
        int statusNum; // @rpg-trace: n490
        try {
            statusNum = Integer.parseInt(status.trim());
        } catch (NumberFormatException e) {
            return true;
        }
        if (("=".equals(sign) || "*".equals(sign)) && statusNum != claim.getG71170()) { // @rpg-trace: n491
            return false;
        }
        if (">".equals(sign) && statusNum >= claim.getG71170()) { // @rpg-trace: n493
            return false;
        }
        if ("<".equals(sign) && statusNum <= claim.getG71170()) { // @rpg-trace: n495
            return false;
        }
        return true;
    }

    private boolean applySearchFilter(Claim claim, String searchString) {
        if (searchString == null || searchString.isBlank()) { // @rpg-trace: n501
            return true;
        }
        String combined = (nullSafe(claim.getG71000()) + nullSafe(claim.getG71030()) +
                          nullSafe(claim.getG71020()) + nullSafe(claim.getG71160()) +
                          nullSafe(claim.getG71050()) + nullSafe(claim.getG71010()) +
                          nullSafe(claim.getG71060()) + nullSafe(claim.getG71140()) +
                          nullSafe(claim.getG71150())).toUpperCase(); // @rpg-trace: n501
        return combined.contains(searchString.toUpperCase());
    }

    private boolean applyBranchFilter(Claim claim, String filterBranch, String mainCompany) {
        if (filterBranch == null || filterBranch.isBlank() || filterBranch.equals(mainCompany)) { // @rpg-trace: n503
            return true;
        }
        return filterBranch.equals(claim.getG71000()); // @rpg-trace: n504
    }

    private boolean applyCustomerFilter(Claim claim, String filterCustomer) {
        if (filterCustomer == null || filterCustomer.isBlank()) { // @rpg-trace: n505
            return true;
        }
        return filterCustomer.equals(claim.getG71140());
    }

    private boolean applySdeFilter(Claim claim, String filterSde) {
        if (filterSde == null || filterSde.isBlank()) { // @rpg-trace: n506
            return true;
        }
        return filterSde.equals(claim.getG71160());
    }

    private ClaimListItemDto mapToListItem(Claim claim) {
        String statusText = resolveStatusText(claim); // @rpg-trace: n476
        String demandCode = resolveDemandCode(claim); // @rpg-trace: n507
        ColorResult colorResult = determineColor(claim); // @rpg-trace: n508

        return new ClaimListItemDto(
            claim.getG71000(), // @rpg-trace: n472
            claim.getG71010(),
            formatDate(claim.getG71020()),
            claim.getG71030(),
            claim.getG71040(),
            claim.getG71050(),
            claim.getG71060(),
            claim.getG71070(),
            claim.getG71090() != null ? claim.getG71090().toString() : "",
            claim.getG71100() != null ? claim.getG71100().toString() : "",
            claim.getG71140(),
            claim.getG71150(),
            claim.getG71160(),
            claim.getG71170(),
            statusText,
            demandCode,
            colorResult.color,
            colorResult.errorCount
        ); // @rpg-trace: n509
    }

    private String resolveStatusText(Claim claim) {
        if ("00000000".equals(claim.getG71160())) { // @rpg-trace: n476
            if (claim.getG71170() == ClaimStatus.MINIMUM.getCode()) { // @rpg-trace: n478
                return "Minimumantrag"; // @rpg-trace: n479
            } else if (claim.getG71170() == ClaimStatus.APPROVED.getCode()) { // @rpg-trace: n480
                return "Minimum ausgebucht"; // @rpg-trace: n481
            } else {
                return "Minimumantrag"; // @rpg-trace: n483
            }
        }
        return String.valueOf(claim.getG71170()); // @rpg-trace: n475
    }

    private String resolveDemandCode(Claim claim) {
        List<ClaimError> errors = claimErrorRepository.findByClaimKey(claim.getG71000(), claim.getG71050()); // @rpg-trace: n1732
        if (!errors.isEmpty()) { // @rpg-trace: n1733
            return errors.get(0).getG73140(); // @rpg-trace: n1736
        }
        return ""; // @rpg-trace: n1731
    }

    private ColorResult determineColor(Claim claim) {
        boolean red = false; // @rpg-trace: n1577
        boolean yellow = false; // @rpg-trace: n1578
        boolean blue = false; // @rpg-trace: n1579
        int errorCount = 0; // @rpg-trace: n1580

        if (claim.getG71160() != null && !"00000000".equals(claim.getG71160()) && !claim.getG71160().isBlank()) { // @rpg-trace: n1581
            List<ClaimError> errors = claimErrorRepository.findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n1585

            if (errors.isEmpty() && claim.getG71170() == ClaimStatus.APPROVED.getCode()) { // @rpg-trace: n1586
                red = true; // @rpg-trace: n1588
            }

            for (ClaimError error : errors) { // @rpg-trace: n1590
                if (error.getG73290() == 16) { // @rpg-trace: n1593
                    red = true; // @rpg-trace: n1594
                }
                if (error.getG73290() == 30 || (error.getG73290() == 0 && claim.getG71160() != null && !claim.getG71160().isBlank())) { // @rpg-trace: n1596
                    red = true; // @rpg-trace: n1597
                }
                if (error.getG73290() == 11) { // @rpg-trace: n1605
                    yellow = true; // @rpg-trace: n1606
                }
                if (error.getG73290() == 3 || error.getG73290() == 11) { // @rpg-trace: n1638
                    blue = true; // @rpg-trace: n1639
                }
                errorCount++; // @rpg-trace: n1642
            }
        } else {
            // SUB160 is blank - clear it // @rpg-trace: n1647
        }

        String color = ""; // @rpg-trace: n1649
        if (red) { // @rpg-trace: n1650
            color = "ROT"; // @rpg-trace: n1651
        }
        if (yellow) { // @rpg-trace: n1653
            color = color.isEmpty() ? "GELB" : color + " GELB"; // @rpg-trace: n1654
        }
        if (blue && !yellow) { // @rpg-trace: n1656
            color = color.isEmpty() ? "BLAU" : color + " BLAU"; // @rpg-trace: n1657
        }

        return new ColorResult(color, errorCount);
    }

    /**
     * Claim header + error subfile (RPG: drill-down / claim history from list).
     */
    @Transactional(readOnly = true)
    public Optional<ClaimDetailDto> getClaimDetail(String companyCode, String claimNr) {
        return ClaimLookupSupport.findClaim(claimRepository, companyCode, claimNr)
            .map(claim -> {
                ClaimListItemDto summary = mapToListItem(claim);
                List<ClaimError> errorEntities = claimErrorRepository
                    .findByCompanyAndClaimNr(claim.getG71000(), claim.getG71050());
                List<ClaimErrorSummaryDto> errors = errorEntities.stream()
                    .map(e -> new ClaimErrorSummaryDto(
                        e.getG73060(),
                        e.getG73065(),
                        e.getG73120() != null ? e.getG73120().trim() : "",
                        e.getG73140() != null ? e.getG73140().trim() : "",
                        e.getG73290()))
                    .collect(Collectors.toList());
                List<ClaimHistoryEntryDto> history = new ArrayList<>();
                String statusTitle = summary.statusText() != null && !summary.statusText().isBlank()
                    ? summary.statusText()
                    : "Current status";
                history.add(new ClaimHistoryEntryDto(
                    ClaimHistoryEntryDto.TYPE_STATUS,
                    statusTitle,
                    "Claim " + summary.claimNr() + " · Invoice " + summary.invoiceNr() + " · Repair date " + summary.repairDate(),
                    "Status code " + summary.statusCode()
                ));
                for (ClaimError e : errorEntities) {
                    history.add(new ClaimHistoryEntryDto(
                        ClaimHistoryEntryDto.TYPE_ERROR,
                        "Error " + e.getG73060() + " / " + e.getG73065(),
                        e.getG73120() != null ? e.getG73120().trim() : "",
                        "DMC " + (e.getG73140() != null ? e.getG73140().trim() : "") + " · processing " + e.getG73290()
                    ));
                }
                return new ClaimDetailDto(summary, history, errors);
            });
    }

    @Transactional
    public boolean deleteClaim(ClaimDeleteRequestDto request) {
        Optional<Claim> claimOpt = ClaimLookupSupport.findClaim(claimRepository, request.companyCode(), request.claimNr()); // @rpg-trace: n586
        if (claimOpt.isEmpty()) {
            return false;
        }
        Claim claim = claimOpt.get();
        claimErrorRepository.deleteByCompanyAndClaimNr(claim.getG71000(), claim.getG71050()); // @rpg-trace: n593
        claimRepository.delete(claim); // physical delete from HSG71LF2
        return true;
    }

    @Transactional
    public void updateClaimStatus(ClaimStatusUpdateDto request) {
        Optional<Claim> claimOpt = ClaimLookupSupport.findClaim(claimRepository, request.companyCode(), request.claimNr()); // @rpg-trace: n653
        if (claimOpt.isPresent()) { // @rpg-trace: n654
            Claim claim = claimOpt.get();
            if (claim.getG71170() == 2) { // @rpg-trace: n657
                claim.setG71170(3); // @rpg-trace: n658
                claimRepository.save(claim); // @rpg-trace: n658
            }
        }
    }

    @Transactional
    public void bookMinimumClaim(MinimumClaimBookingDto request) {
        Optional<Claim> claimOpt = ClaimLookupSupport.findClaim(claimRepository, request.companyCode(), request.claimNr()); // @rpg-trace: n1663
        if (claimOpt.isPresent()) { // @rpg-trace: n1664
            Claim claim = claimOpt.get();
            if (claim.getG71170() == ClaimStatus.MINIMUM.getCode()) { // @rpg-trace: n1665
                claim.setG71170(ClaimStatus.APPROVED.getCode()); // @rpg-trace: n1670
                claimRepository.save(claim); // @rpg-trace: n1671
            }
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank() || isoDate.length() < 8) { // @rpg-trace: n1517
            return "";
        }
        String year = isoDate.substring(0, 4); // @rpg-trace: n1517
        String month = isoDate.substring(4, 6);
        String day = isoDate.substring(6, 8);
        if ("0000".equals(year)) { // @rpg-trace: n1517
            return "";
        }
        return day + "." + month + "." + year;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private record ColorResult(String color, int errorCount) {}
}