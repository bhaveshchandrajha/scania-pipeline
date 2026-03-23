/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimCheckRequest;
import com.scania.warranty.domain.Hsgpspf;
import com.scania.warranty.dto.ClaimCheckResultDto;
import com.scania.warranty.repository.HsgpspfRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckClaimService {

    private final HsgpspfRepository hsgpspfRepository;
    private final ClaimValuesService claimValuesService;

    public CheckClaimService(HsgpspfRepository hsgpspfRepository,
                             ClaimValuesService claimValuesService) {
        this.hsgpspfRepository = hsgpspfRepository;
        this.claimValuesService = claimValuesService;
    }

    /**
     * Validates a claim before submission. Corresponds to RPG procedure CheckClaim (n1919).
     * Returns a result indicating whether the claim is valid and any validation errors found.
     *
     * @param request the claim check request containing all relevant claim fields
     * @return ClaimCheckResultDto with valid flag and list of error messages
     */
    public ClaimCheckResultDto checkClaim(ClaimCheckRequest request) {
        List<String> errors = new ArrayList<>(); // @rpg-trace: n1922

        // If customer damage code 1 is '3' (campaign) but no campaign number provided
        if ("3".equals(trimToEmpty(request.customerDamageCode1()))
                && isBlank(request.campaignNumber())) { // @rpg-trace: n1924
            errors.add("Ursache Kampagne aber keine Kampagnen-Nr. eingetragen."); // @rpg-trace: n1927
            // In RPG, G73360 is cleared and the record is updated
            // The caller is responsible for clearing customerDamageCode1 and updating HSG73LF1
            // @rpg-trace: n1928
            // @rpg-trace: n1929
        }

        // If customer damage code 1 is blank → invalid
        if (isBlank(request.customerDamageCode1())) { // @rpg-trace: n1932
            errors.add("Schadenscodierung Kunde ungültig."); // @rpg-trace: n1934
        }

        // If customer damage code 1 is '3' and code2 or code3 is not blank → invalid
        if ("3".equals(trimToEmpty(request.customerDamageCode1()))
                && (!isBlank(request.customerDamageCode2()) || !isBlank(request.customerDamageCode3()))) { // @rpg-trace: n1937
            errors.add("Schadenscodierung Kunde ungültig."); // @rpg-trace: n1940
        }

        // If workshop damage code 1 is blank → invalid
        if (isBlank(request.workshopDamageCode1())) { // @rpg-trace: n1943
            errors.add("Schadenscodierung Werkstatt ungültig."); // @rpg-trace: n1945
        }

        // If workshop damage code 1 is '97' and code2 or code3 is not blank → invalid
        if ("97".equals(trimToEmpty(request.workshopDamageCode1()))
                && (!isBlank(request.workshopDamageCode2()) || !isBlank(request.workshopDamageCode3()))) { // @rpg-trace: n1948
            errors.add("Schadenscodierung Werkstatt ungültig."); // @rpg-trace: n1951
        }

        // If damage causing part number is blank → error
        if (isBlank(request.damageCausingPartNumber())) { // @rpg-trace: n1954
            errors.add("Es ist keine schadensverursachende Teilenummer angegeben."); // @rpg-trace: n1956
        }

        // Check HSGPSPF records for SMA special costs without coding
        boolean specialCostError = checkSpecialCostsWithoutCoding(
                request.companyCode(), request.claimNumber(), request.claimSequence()); // @rpg-trace: n1958

        if (specialCostError) { // @rpg-trace: n1967
            errors.add("Es sind Sonderkosten ohne Codierung vorhanden."); // @rpg-trace: n1970
        }

        // Check if claim value is zero → cannot submit
        BigDecimal claimValue = claimValuesService.getClaimValues(
                "Requested",
                request.companyCode(),
                request.claimNumber(),
                request.claimSequence(),
                request.claimSubSequence()); // @rpg-trace: n1972

        if (claimValue.compareTo(BigDecimal.ZERO) == 0) { // @rpg-trace: n1973
            errors.add("Antrag mit Wert 0 kann nicht versendet werden."); // @rpg-trace: n1975
        }

        // If there are errors, return invalid; otherwise valid
        if (!errors.isEmpty()) { // @rpg-trace: n1978
            return new ClaimCheckResultDto(false, errors); // @rpg-trace: n1979
        }

        return new ClaimCheckResultDto(true, List.of()); // @rpg-trace: n1981
    }

    /**
     * Checks HSGPSPF records for the given key to find SMA entries
     * that are missing GPS220 or GPS240 coding.
     * Corresponds to RPG SETLL/READE loop on HSGPSPF (n1958-n1964).
     */
    private boolean checkSpecialCostsWithoutCoding(String companyCode, String claimNumber, String claimSequence) {
        List<Hsgpspf> records = hsgpspfRepository.findByGps000AndGps010AndGps020(
                companyCode, claimNumber, claimSequence); // @rpg-trace: n1958

        return records.stream() // @rpg-trace: n1959
                .anyMatch(record ->
                        "SMA".equals(trimToEmpty(record.getGps040())) // @rpg-trace: n1963
                                && (isBlank(record.getGps220()) || isBlank(record.getGps240())) // @rpg-trace: n1963
                ); // @rpg-trace: n1964
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}