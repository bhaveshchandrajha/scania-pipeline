/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClaimSubfileService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ClaimSubfileService(ClaimRepository claimRepository, ClaimErrorRepository claimErrorRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
    }

    public void resetMarkersForSubfile() { // @rpg-trace: n405
        // SETOFF 50 51 52 53 54 55 56 57 58
        // Indicators reset - no state in stateless service
    }

    public void swapMarkers(String mark12, String mark11) { // @rpg-trace: n408
        if (mark12 == null || mark12.isBlank()) {
            mark12 = mark11;
            mark11 = " ";
        }
    }

    public void swapMarkersForSelection(String mark12, String mark11, String mark22, String mark21) { // @rpg-trace: n414
        if (mark12 == null || mark12.isBlank()) {
            mark12 = mark11;
            mark11 = " ";
        }
        if (mark22 == null || mark22.isBlank()) { // @rpg-trace: n416
            mark22 = mark21;
            mark21 = " ";
        }
    }

    public List<Claim> buildSubfileList(String pakz, boolean ascending, Integer filterAgeDays, String filterType, String filterOpen, String searchString, String statusFilter, String zeichen, String filPkz, String filtFg, String filtKd, String filSde, LocalDate currentDate) { // @rpg-trace: n422
        List<Claim> claims;
        if (ascending) { // @rpg-trace: n428
            claims = claimRepository.findByPakzOrderByClaimNrAsc(pakz);
        } else { // @rpg-trace: n431
            claims = claimRepository.findByPakzOrderByClaimNrDesc(pakz);
        }

        return claims.stream() // @rpg-trace: n436
                .filter(claim -> filterByAge(claim, filterAgeDays, currentDate)) // @rpg-trace: n439
                .filter(claim -> filterByType(claim, filterType)) // @rpg-trace: n447
                .filter(claim -> filterByOpenStatus(claim, filterOpen)) // @rpg-trace: n452
                .filter(claim -> claim.getStatusCodeSde() != null && claim.getStatusCodeSde() != 99) // @rpg-trace: n471
                .filter(claim -> filterByStatus(claim, statusFilter, zeichen)) // @rpg-trace: n490
                .filter(claim -> matchesSearchString(claim, searchString)) // @rpg-trace: n501
                .filter(claim -> filterByCompany(claim, filPkz)) // @rpg-trace: n504
                .filter(claim -> filterByVehicle(claim, filtFg)) // @rpg-trace: n505
                .filter(claim -> filterByCustomer(claim, filtKd)) // @rpg-trace: n506
                .filter(claim -> filterBySde(claim, filSde)) // @rpg-trace: n507
                .limit(9999) // @rpg-trace: n436
                .collect(Collectors.toList());
    }

    private boolean filterByAge(Claim claim, Integer filterAgeDays, LocalDate currentDate) { // @rpg-trace: n439
        if (filterAgeDays == null || filterAgeDays == 0 || claim.getStatusCodeSde() == 99) {
            return true;
        }
        try {
            LocalDate repairDate = LocalDate.parse(String.valueOf(claim.getRepDatum()), ISO_DATE_FORMATTER); // @rpg-trace: n441
            long daysDiff = ChronoUnit.DAYS.between(repairDate, currentDate); // @rpg-trace: n442
            if (daysDiff > filterAgeDays) { // @rpg-trace: n443
                return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private boolean filterByType(Claim claim, String filterType) { // @rpg-trace: n447
        if (filterType == null || filterType.isBlank() || claim.getStatusCodeSde() == 99) {
            return true;
        }
        List<ClaimError> failures = claimErrorRepository.findByClaimNumber(claim.getPakz(), claim.getClaimNr()); // @rpg-trace: n1745
        return failures.stream().anyMatch(f -> matchesFilterType(f, filterType)); // @rpg-trace: n1748
    }

    private boolean matchesFilterType(ClaimError failure, String filterType) { // @rpg-trace: n1750
        // Simplified logic - actual implementation would check S3F085 and warranty scope
        return true;
    }

    private boolean filterByOpenStatus(Claim claim, String filterOpen) { // @rpg-trace: n452
        if (!"J".equals(filterOpen)) {
            return true;
        }
        boolean open = false;
        if (claim.getStatusCodeSde() < 20 && claim.getStatusCodeSde() != 5) { // @rpg-trace: n454
            open = true;
        }
        List<ClaimError> failures = claimErrorRepository.findByClaimNumber(claim.getPakz(), claim.getClaimNr()); // @rpg-trace: n457
        if (failures.isEmpty()) { // @rpg-trace: n459
            open = true;
        }
        for (ClaimError failure : failures) { // @rpg-trace: n463
            if (failure.getStatusCode() == 0) { // @rpg-trace: n464
                open = true;
            }
        }
        return open;
    }

    private boolean filterByStatus(Claim claim, String statusFilter, String zeichen) { // @rpg-trace: n490
        if (statusFilter == null || statusFilter.isBlank()) {
            return true;
        }
        try {
            int statusCode = Integer.parseInt(statusFilter);
            if (zeichen == null || zeichen.isBlank()) {
                zeichen = "=";
            }
            if ("=".equals(zeichen) || "*".equals(zeichen)) { // @rpg-trace: n491
                return statusCode == claim.getStatusCodeSde();
            }
            if (">".equals(zeichen)) { // @rpg-trace: n493
                return statusCode < claim.getStatusCodeSde();
            }
            if ("<".equals(zeichen)) { // @rpg-trace: n495
                return statusCode > claim.getStatusCodeSde();
            }
        } catch (NumberFormatException e) {
            return true;
        }
        return true;
    }

    private boolean matchesSearchString(Claim claim, String searchString) { // @rpg-trace: n501
        if (searchString == null || searchString.isBlank()) {
            return true;
        }
        String combined = claim.getPakz() + claim.getAuftragsNr() + claim.getRechDatum() + claim.getClaimNrSde() + claim.getClaimNr() + claim.getRechNr() + claim.getChassisNr() + claim.getKdNr() + claim.getKdName();
        return combined.toUpperCase().contains(searchString.toUpperCase());
    }

    private boolean filterByCompany(Claim claim, String filPkz) { // @rpg-trace: n504
        if (filPkz == null || filPkz.isBlank()) {
            return true;
        }
        return filPkz.equals(claim.getPakz());
    }

    private boolean filterByVehicle(Claim claim, String filtFg) { // @rpg-trace: n505
        if (filtFg == null || filtFg.isBlank()) {
            return true;
        }
        return filtFg.equals(claim.getChassisNr());
    }

    private boolean filterByCustomer(Claim claim, String filtKd) { // @rpg-trace: n506
        if (filtKd == null || filtKd.isBlank()) {
            return true;
        }
        return filtKd.equals(claim.getKdNr());
    }

    private boolean filterBySde(Claim claim, String filSde) { // @rpg-trace: n507
        if (filSde == null || filSde.isBlank()) {
            return true;
        }
        return filSde.equals(claim.getClaimNrSde());
    }

    public void positionToNewClaim(List<Claim> claims, String newClaimNr) { // @rpg-trace: n529
        if (newClaimNr == null || newClaimNr.isBlank()) {
            return;
        }
        for (int i = 0; i < claims.size(); i++) { // @rpg-trace: n532
            if (claims.get(i).getClaimNr().compareTo(newClaimNr) >= 0) {
                // Position found - in UI context would set REC1/PAG1
                return;
            }
        }
    }

    public void deleteSelectedClaims(List<String> selectedClaimIds) { // @rpg-trace: n553
        for (String claimId : selectedClaimIds) { // @rpg-trace: n557
            String[] parts = claimId.split("\\|");
            if (parts.length >= 6) {
                String pakz = parts[0];
                String rechNr = parts[1];
                String rechDatum = parts[2];
                String auftragsNr = parts[3];
                String wete = parts[4];
                String claimNr = parts[5];
                
                ClaimId id = new ClaimId(pakz, rechNr, rechDatum, auftragsNr, wete);
                claimRepository.findById(id).ifPresent(claim -> { // @rpg-trace: n587
                    claim.setStatusCodeSde(99); // @rpg-trace: n589
                    claimRepository.save(claim);
                    
                    List<ClaimError> failures = claimErrorRepository.findByClaimNumber(pakz, claimNr); // @rpg-trace: n592
                    claimErrorRepository.deleteAll(failures);
                });
            }
        }
    }

    public void updateClaimStatus(String pakz, String claimNr, int newStatus) { // @rpg-trace: n654
        claimRepository.findByPakzAndClaimNr(pakz, claimNr).ifPresent(claim -> { // @rpg-trace: n654
            if (claim.getStatusCodeSde() == 2) { // @rpg-trace: n657
                claim.setStatusCodeSde(3); // @rpg-trace: n658
                claimRepository.save(claim);
            }
        });
    }

    public String formatDate(String isoDate) { // @rpg-trace: n1517
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
}