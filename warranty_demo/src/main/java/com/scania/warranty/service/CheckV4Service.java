/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.CheckV4Request;
import com.scania.warranty.domain.ExtendedPartAgreement;
import com.scania.warranty.domain.InvoiceHeader;
import com.scania.warranty.repository.ExtendedPartAgreementRepository;
import com.scania.warranty.repository.InvoiceHeaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Migrated from RPG procedure CheckV4 (lines 3035-3058).
 *
 * This procedure checks whether a V4-type extended part agreement exists
 * that matches the invoice header's date (AHK080). It returns true if a
 * matching EPA record is found with EPA_DATV equal to AHK080, false otherwise.
 *
 * Logic:
 * 1. If the agreement type code (from G71200 position 8-9) is already 'V4', return false immediately.
 * 2. Look up the invoice header (HSAHKPF) by composite key.
 * 3. If found, search extended part agreements (HSEPAF) with type 'V4' and matching partial key.
 * 4. If any EPA record's EPA_DATV matches the invoice header's AHK080, return true.
 * 5. On any error or no match, return false.
 */
@Service
public class CheckV4Service {

    private static final Logger log = LoggerFactory.getLogger(CheckV4Service.class);
    private static final String V4_TYPE = "V4";

    private final InvoiceHeaderRepository invoiceHeaderRepository;
    private final ExtendedPartAgreementRepository extendedPartAgreementRepository;

    @Autowired
    public CheckV4Service(InvoiceHeaderRepository invoiceHeaderRepository,
                          ExtendedPartAgreementRepository extendedPartAgreementRepository) {
        this.invoiceHeaderRepository = invoiceHeaderRepository;
        this.extendedPartAgreementRepository = extendedPartAgreementRepository;
    }

    /**
     * Checks if a V4 extended part agreement exists matching the invoice header date.
     *
     * @param request the CheckV4Request containing all G71 fields
     * @return true if a matching V4 agreement is found, false otherwise
     */
    public boolean checkV4(CheckV4Request request) {
        try { // @rpg-trace: n1985
            String agreementTypeCode = request.agreementTypeCode(); // @rpg-trace: n1987

            // If the agreement type is already 'V4', return false immediately
            if (V4_TYPE.equals(agreementTypeCode)) { // @rpg-trace: n1986
                return false; // @rpg-trace: n1989
            }

            // CHAIN to HSAHKPF: look up invoice header by composite key
            // RPG key: (G71000:G71010:G71020:' ':G71030:G71040:G71190:%Subst(G71200:8:2))
            Optional<InvoiceHeader> invoiceHeaderOpt = invoiceHeaderRepository.findByCompositeKey(
                request.g71000(),   // AHK000
                request.g71010(),   // AHK010
                request.g71020(),   // AHK020
                " ",                // AHK030 = blank in RPG
                request.g71030(),   // AHK040
                request.g71040(),   // AHK050
                request.g71190(),   // AHK060 = G71190
                agreementTypeCode   // AHK070 = %Subst(G71200:8:2)
            ); // @rpg-trace: n1991

            if (invoiceHeaderOpt.isPresent()) { // @rpg-trace: n1993
                InvoiceHeader invoiceHeader = invoiceHeaderOpt.get(); // @rpg-trace: n1995

                // SETLL/READE on HSEPAF with key (AHK000:AHK040:AHK050:AHK060:'V4')
                List<ExtendedPartAgreement> epaRecords = extendedPartAgreementRepository
                    .findByKeyAndVariant(
                        invoiceHeader.getAhk000(),  // EPA000 = AHK000
                        invoiceHeader.getAhk040(),  // EPA040 = AHK040
                        invoiceHeader.getAhk050(),  // EPA050 = AHK050
                        invoiceHeader.getAhk060(),  // EPA060 = AHK060
                        V4_TYPE                      // EPA_TYPE = 'V4'
                    ); // @rpg-trace: n1996

                // DOW loop: iterate through EPA records checking EPA_DATV against AHK080
                for (ExtendedPartAgreement epa : epaRecords) { // @rpg-trace: n1997
                    if (epa.getEpaDatv() != null &&
                        epa.getEpaDatv().equals(invoiceHeader.getAhk080())) { // @rpg-trace: n2000
                        return true; // @rpg-trace: n2002
                    }
                }
            }
        } catch (Exception e) { // @rpg-trace: n2006
            // RPG: On-Error *All — silently handle all errors, fall through to return false
            log.warn("Error during CheckV4 processing: {}", e.getMessage(), e);
        }

        return false; // @rpg-trace: n2008
    }
}