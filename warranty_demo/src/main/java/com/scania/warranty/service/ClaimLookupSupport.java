package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.repository.ClaimRepository;

import java.util.Optional;

/**
 * Resolves API/UI claim keys (trim, company padding, 8-digit claim numbers) to persisted G71000/G71050.
 */
final class ClaimLookupSupport {

    private ClaimLookupSupport() {}

    static Optional<Claim> findClaim(ClaimRepository claimRepository, String companyCode, String claimNr) {
        String cc = normalizeCompanyCode(companyCode);
        String raw = claimNr == null ? "" : claimNr.trim();
        if (cc.isEmpty() || raw.isEmpty()) {
            return Optional.empty();
        }
        Optional<Claim> found = claimRepository.findByCompanyAndClaimNr(cc, raw);
        if (found.isPresent()) {
            return found;
        }
        String padded = padToEightDigitClaimNr(raw);
        if (padded != null && !padded.equals(raw)) {
            return claimRepository.findByCompanyAndClaimNr(cc, padded);
        }
        return Optional.empty();
    }

    static String normalizeCompanyCode(String companyCode) {
        if (companyCode == null) {
            return "";
        }
        String t = companyCode.trim();
        if (t.isEmpty()) {
            return "";
        }
        if (t.matches("\\d{1,4}")) {
            try {
                return String.format("%03d", Integer.parseInt(t));
            } catch (NumberFormatException e) {
                return t;
            }
        }
        return t;
    }

    private static String padToEightDigitClaimNr(String raw) {
        if (!raw.matches("\\d{1,8}")) {
            return null;
        }
        try {
            long n = Long.parseLong(raw);
            if (n < 0 || n > 99_999_999L) {
                return null;
            }
            return String.format("%08d", n);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
