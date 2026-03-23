/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ExtendedPartAgreement;
import com.scania.warranty.domain.InvoiceHeader;
import com.scania.warranty.domain.V4CheckCriteria;
import com.scania.warranty.dto.V4CheckResultDto;
import com.scania.warranty.repository.ExtendedPartAgreementRepository;
import com.scania.warranty.repository.InvoiceHeaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service implementing the CheckV4 RPG procedure logic.
 * 
 * RPG procedure CheckV4 returns *On (true) if a matching V4 extended part agreement
 * is found with a valid date matching the invoice header date, otherwise returns *Off (false).
 * 
 * The procedure:
 * 1. Checks if the variant code extracted from G71200 is already 'V4' → returns false (no check needed)
 * 2. Looks up the invoice header (HSAHKPF) by composite key
 * 3. If found, searches extended part agreements (HSEPAF) for matching 'V4' records
 * 4. If any EPA record's date (EPA_DATV) matches the invoice header date (AHK080), returns true
 * 5. Otherwise returns false
 */
@Service
public class V4CheckService {

    private static final Logger log = LoggerFactory.getLogger(V4CheckService.class);
    private static final String V4_VARIANT = "V4";

    private final InvoiceHeaderRepository invoiceHeaderRepository;
    private final ExtendedPartAgreementRepository extendedPartAgreementRepository;

    public V4CheckService(InvoiceHeaderRepository invoiceHeaderRepository,
                          ExtendedPartAgreementRepository extendedPartAgreementRepository) {
        this.invoiceHeaderRepository = invoiceHeaderRepository;
        this.extendedPartAgreementRepository = extendedPartAgreementRepository;
    }

    /**
     * Checks whether a V4 extended part agreement exists that matches the invoice header date.
     * 
     * Migrated from RPG procedure CheckV4 (lines 3035-3058).
     *
     * @param criteria the search criteria containing key fields
     * @return true if a matching V4 agreement is found, false otherwise
     */
    public boolean checkV4(V4CheckCriteria criteria) {
        try { // @rpg-trace: n1985
            String variantCode = criteria.variantCode(); // @rpg-trace: n1987

            // RPG: If %Subst(G71200:8:2) = 'V4'; Return *Off; EndIf;
            if (V4_VARIANT.equals(variantCode)) { // @rpg-trace: n1986
                log.debug("Variant code is already V4, returning false"); // @rpg-trace: n1989
                return false; // @rpg-trace: n1989
            }

            // RPG: Chain (G71000:G71010:G71020:' ':G71030:G71040:G71190:%Subst(G71200:8:2)) HSAHKPF
            Optional<InvoiceHeader> invoiceHeaderOpt = invoiceHeaderRepository.findByCompositeKey(
                criteria.g71000(),
                criteria.g71010(),
                criteria.g71020(),
                " ",
                criteria.g71030(),
                criteria.g71040(),
                criteria.g71190(),
                variantCode
            ); // @rpg-trace: n1991

            // RPG: If %Found(HSAHKPF)
            if (invoiceHeaderOpt.isPresent()) { // @rpg-trace: n1993
                InvoiceHeader invoiceHeader = invoiceHeaderOpt.get(); // @rpg-trace: n1992

                // RPG: SetLl (AHK000:AHK040:AHK050:AHK060:'V4') HSEPAF
                // RPG: ReadE (AHK000:AHK040:AHK050:AHK060:'V4') HSEPAF
                List<ExtendedPartAgreement> epaRecords = extendedPartAgreementRepository.findByKeyAndVariant(
                    invoiceHeader.getAhk000(),
                    invoiceHeader.getAhk040(),
                    invoiceHeader.getAhk050(),
                    invoiceHeader.getAhk060(),
                    V4_VARIANT
                ); // @rpg-trace: n1996

                // RPG: DoW Not %EoF(HSEPAF)
                for (ExtendedPartAgreement epa : epaRecords) { // @rpg-trace: n1997
                    // RPG: If EPA_DATV = AHK080; return *On; EndIf;
                    if (epa.getEpaDatv() != null && epa.getEpaDatv().equals(invoiceHeader.getAhk080())) { // @rpg-trace: n2000
                        log.debug("Found matching V4 EPA record with date {}", epa.getEpaDatv()); // @rpg-trace: n2002
                        return true; // @rpg-trace: n2002
                    }
                }
            }
        } catch (Exception e) { // @rpg-trace: n2006
            log.error("Error during V4 check: {}", e.getMessage(), e); // @rpg-trace: n2006
        }

        return false; // @rpg-trace: n2008
    }

    /**
     * Checks V4 and returns a detailed result DTO.
     *
     * @param criteria the search criteria
     * @return a DTO with the check result and additional context
     */
    public V4CheckResultDto checkV4WithDetails(V4CheckCriteria criteria) {
        String variantCode = criteria.variantCode(); // @rpg-trace: n1988
        boolean result = checkV4(criteria); // @rpg-trace: n1985

        String message = result
            ? "V4 extended part agreement found with matching date"
            : V4_VARIANT.equals(variantCode)
                ? "Variant code is already V4, no check performed"
                : "No matching V4 extended part agreement found";

        return new V4CheckResultDto(result, variantCode, message);
    }
}