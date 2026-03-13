/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimPosition;
import com.scania.warranty.repository.ClaimPositionRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClaimCreationService {
    
    private final ClaimPositionRepository claimPositionRepository;
    private final ClaimRepository claimRepository;
    
    public ClaimCreationService(ClaimPositionRepository claimPositionRepository, 
                                ClaimRepository claimRepository) {
        this.claimPositionRepository = claimPositionRepository;
        this.claimRepository = claimRepository;
    }
    
    public String generateClaimNumber(String companyCode, String invoiceNumber, String invoiceDate, String orderNumber, String area) {
        List<Claim> existingClaims = claimRepository.findByPakzOrderByClaimNrAsc(companyCode);
        int maxNumber = 0;
        for (Claim claim : existingClaims) {
            try {
                int claimNum = Integer.parseInt(claim.getClaimNr());
                if (claimNum > maxNumber) {
                    maxNumber = claimNum;
                }
            } catch (NumberFormatException e) {
                // Skip invalid claim numbers
            }
        }
        return String.format("%08d", maxNumber + 1);
    }
    
    @Transactional
    public void createClaim(String kuerzel, String claimNr, String pakz, String rechNr, 
                            String rechDatum, String auftragsNr, String bereich) {
        
        Integer failureNo = 0; // @rpg-trace: n1783
        
        List<ClaimPosition> positions = claimPositionRepository.findByKuerzelAndClaimNrOrderByKeys(kuerzel, claimNr); // @rpg-trace: n1784
        
        boolean create = false; // @rpg-trace: n1779
        boolean maintenance = false; // @rpg-trace: n1779
        String groups = ""; // @rpg-trace: n1779
        String partNo = ""; // @rpg-trace: n1779
        BigDecimal partValue = BigDecimal.ZERO; // @rpg-trace: n1779
        Integer lineNo = 0; // @rpg-trace: n1779
        BigDecimal valueMat = BigDecimal.ZERO; // @rpg-trace: n1779
        BigDecimal valueLab = BigDecimal.ZERO; // @rpg-trace: n1779
        BigDecimal valueSpe = BigDecimal.ZERO; // @rpg-trace: n1779
        
        for (ClaimPosition position : positions) { // @rpg-trace: n1785
            
            if (position.getZeile() != null && !position.getZeile().equals(failureNo)) { // @rpg-trace: n1789
                
                if (maintenance && groups != null && !groups.isEmpty() && partNo != null && !partNo.isEmpty()) { // @rpg-trace: n1788
                    // Process accumulated maintenance data
                    // This would be the logic to save/update maintenance records
                }
                
                failureNo = position.getZeile(); // @rpg-trace: n1790
                maintenance = false; // @rpg-trace: n1791
                lineNo = 0; // @rpg-trace: n1792
                valueMat = BigDecimal.ZERO; // @rpg-trace: n1793
                valueLab = BigDecimal.ZERO; // @rpg-trace: n1794
                valueSpe = BigDecimal.ZERO; // @rpg-trace: n1795
                groups = ""; // @rpg-trace: n1796
                partNo = ""; // @rpg-trace: n1797
                partValue = BigDecimal.ZERO; // @rpg-trace: n1798
            }
            
            if (position.getZeile() != null && position.getZeile().equals(failureNo)) { // @rpg-trace: n1801
                create = true; // @rpg-trace: n1802
                String fehlerNr = position.getFehlerNr() != null ? position.getFehlerNr().trim() : ""; // @rpg-trace: n1803
                String folgeNr = position.getFolgeNr() != null ? position.getFolgeNr().trim() : ""; // @rpg-trace: n1806
                
                if (position.getSatzart() != null && !position.getSatzart().isEmpty()) { // @rpg-trace: n1809
                    // Chain to ITLSMF3 - would need additional repository/logic
                    // For now, we'll handle the basic logic
                    
                    if (position.getWert() != null && position.getWert().compareTo(BigDecimal.ZERO) > 0) { // @rpg-trace: n1815
                        partNo = position.getNr() != null ? position.getNr() : ""; // @rpg-trace: n1816
                        partValue = position.getWert(); // @rpg-trace: n1817
                    }
                }
            }
        }
        
        if (create) { // @rpg-trace: n1823
            Claim claim = claimRepository.findByKey(pakz, rechNr, rechDatum, auftragsNr, bereich).orElse(null); // @rpg-trace: n1824
            
            if (claim != null) { // @rpg-trace: n1826
                claim.setAnzFehler(failureNo); // @rpg-trace: n1828
                claimRepository.save(claim); // @rpg-trace: n1829
            } else { // @rpg-trace: n1832
                // Handle case where claim doesn't exist - could throw exception or create new
                throw new IllegalArgumentException("Claim not found for key: " + pakz + "/" + rechNr + "/" + rechDatum + "/" + auftragsNr + "/" + bereich); // @rpg-trace: n1832
            }
        }
        
        if (maintenance && groups != null && !groups.isEmpty() && partNo != null && !partNo.isEmpty()) { // @rpg-trace: n1822
            // Final processing of accumulated maintenance data
            // This would be the logic to save/update the last maintenance record
        }
    } // @rpg-trace: n1836
}