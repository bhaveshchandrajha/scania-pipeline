/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.WarrantyRelease;
import com.scania.warranty.repository.WarrantyReleaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for warranty release request operations.
 */
@Service
@Transactional
public class WarrantyReleaseService {

    private final WarrantyReleaseRepository warrantyReleaseRepository;

    public WarrantyReleaseService(WarrantyReleaseRepository warrantyReleaseRepository) {
        this.warrantyReleaseRepository = warrantyReleaseRepository;
    }

    public void requestWarrantyRelease(String companyCode, String invoiceNr, String invoiceDate, String chassisNr, String repairDate) {
        // @origin HS1210 L941-941 (CHAIN)
        Optional<WarrantyRelease> existingOpt = warrantyReleaseRepository.findByKey(companyCode, invoiceNr, invoiceDate);
        // @origin HS1210 L830-833 (IF)
        if (existingOpt.isEmpty()) {
            WarrantyRelease release = new WarrantyRelease();
            // @origin HS1210 L887-887 (EVAL)
            release.setKzl(companyCode);
            release.setRnr(invoiceNr);
            release.setRdat(invoiceDate);
            release.setFgnr(chassisNr);
            release.setRepdat(repairDate);
            release.setStatus("");
            release.setCusNo(0);
            release.setDcNo(0);
            release.setDcFn("");
            // @origin HS1210 L860-860 (WRITE)
            warrantyReleaseRepository.save(release);
        }
    }
}