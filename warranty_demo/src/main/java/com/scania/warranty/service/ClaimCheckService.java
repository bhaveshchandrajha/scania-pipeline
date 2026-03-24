/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimCheckContext;
import com.scania.warranty.domain.Hsgpspf;
import com.scania.warranty.dto.ClaimCheckResultDto;
import com.scania.warranty.repository.HsgpspfRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimCheckService {

    private final HsgpspfRepository hsgpspfRepository;
    private final ClaimValuesService claimValuesService;

    public ClaimCheckService(HsgpspfRepository hsgpspfRepository, ClaimValuesService claimValuesService) {
        this.hsgpspfRepository = hsgpspfRepository;
        this.claimValuesService = claimValuesService;
    }

    /**
     * Validates a claim before submission. Corresponds to RPG procedure CheckClaim.
     * Returns a result indicating whether the claim is valid and any error messages.
     */
    public ClaimCheckResultDto checkClaim(ClaimCheckContext context) {
        List<String> errors = new ArrayList<>(); // @rpg-trace: n1922
        String g73360 = context.g73360(); // local mutable copy for potential update

        // Check: cause is campaign ('3') but no campaign number entered
        if ("3".equals(trimToEmpty(g73360)) && isBlank(context.g73280())) { // @rpg-trace: n1924
            errors.add("Ursache Kampagne aber keine Kampagnen-Nr. eingetragen."); // @rpg-trace: n1927
            g73360 = ""; // @rpg-trace: n1928
            // RPG: Update HSG73LR1 - the caller is responsible for persisting the cleared g73360
            // @rpg-trace: n1929
        }

        // Check: customer damage coding is blank (invalid)
        if (isBlank(g73360)) { // @rpg-trace: n1932
            errors.add("Schadenscodierung Kunde ungültig."); // @rpg-trace: n1934
        }

        // Check: customer damage coding is '3' but sub-codes are filled (invalid combination)
        if ("3".equals(trimToEmpty(g73360)) && (!isBlank(context.g73370()) || !isBlank(context.g73380()))) { // @rpg-trace: n1937
            errors.add("Schadenscodierung Kunde ungültig."); // @rpg-trace: n1940
        }

        // Check: workshop damage coding is blank (invalid)
        if (isBlank(context.g73390())) { // @rpg-trace: n1943
            errors.add("Schadenscodierung Werkstatt ungültig."); // @rpg-trace: n1945
        }

        // Check: workshop damage coding is '97' but sub-codes are filled (invalid combination)
        if ("97".equals(trimToEmpty(context.g73390())) && (!isBlank(context.g73400()) || !isBlank(context.g73410()))) { // @rpg-trace: n1948
            errors.add("Schadenscodierung Werkstatt ungültig."); // @rpg-trace: n1951
        }

        // Check: no damage-causing part number specified
        if (isBlank(context.g73070())) { // @rpg-trace: n1954
            errors.add("Es ist keine schadensverursachende Teilenummer angegeben."); // @rpg-trace: n1956
        }

        // Check HSGPSPF records for special costs without coding
        List<Hsgpspf> gpsRecords = hsgpspfRepository.findByKeyFields(
                context.g73000(), context.g73050(), context.g73060()
        ); // @rpg-trace: n1958

        boolean specialCostError = false; // @rpg-trace: n1959
        for (Hsgpspf record : gpsRecords) { // @rpg-trace: n1959
            if ("SMA".equals(trimToEmpty(record.getGps040()))
                    && (isBlank(record.getGps220()) || isBlank(record.getGps240()))) { // @rpg-trace: n1963
                specialCostError = true; // @rpg-trace: n1964
                break; // @rpg-trace: n1960
            }
        }

        if (specialCostError) { // @rpg-trace: n1967
            errors.add("Es sind Sonderkosten ohne Codierung vorhanden."); // @rpg-trace: n1970
        }

        // Check: claim value is zero - cannot send request with value 0
        BigDecimal claimValue = claimValuesService.getClaimValues(
                "Requested", context.g73000(), context.g73050(), context.g73060(), context.g73065()
        ); // @rpg-trace: n1972
        if (claimValue != null && claimValue.compareTo(BigDecimal.ZERO) == 0) { // @rpg-trace: n1973
            errors.add("Antrag mit Wert 0 kann nicht versendet werden."); // @rpg-trace: n1975
        }

        // If errors exist, return invalid result with error messages
        if (!errors.isEmpty()) { // @rpg-trace: n1978
            return new ClaimCheckResultDto(false, errors, g73360); // @rpg-trace: n1979
        }

        return new ClaimCheckResultDto(true, List.of(), g73360); // @rpg-trace: n1981
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}