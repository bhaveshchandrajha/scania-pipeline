/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimFailure;
import com.scania.warranty.repository.ClaimFailureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ClaimFailureCreationService { // @rpg-trace: n1790
    
    private final ClaimFailureRepository claimFailureRepository; // @rpg-trace: n1790

    public ClaimFailureCreationService(ClaimFailureRepository claimFailureRepository) { // @rpg-trace: n1790
        this.claimFailureRepository = claimFailureRepository; // @rpg-trace: n1790
    }

    @Transactional
    public void createFailure(Integer failureNumber, // @rpg-trace: n1790
                              String groups, // @rpg-trace: n1790
                              String partNumber, // @rpg-trace: n1790
                              boolean maintenance, // @rpg-trace: n1790
                              List<String> textLines, // @rpg-trace: n1790
                              BigDecimal valueMaterial, // @rpg-trace: n1790
                              BigDecimal valueLabor, // @rpg-trace: n1790
                              BigDecimal valueSpecial) { // @rpg-trace: n1790
        
        ClaimFailure claimFailure = new ClaimFailure(); // @rpg-trace: n1790
        claimFailure.setFehlerNr(String.format("%02d", failureNumber)); // @rpg-trace: n1790
        
        if (groups != null && groups.length() >= 4) { // @rpg-trace: n1790
            claimFailure.setHauptgruppe(groups.substring(0, 2)); // @rpg-trace: n1790
            claimFailure.setNebengruppe(groups.substring(2, 4)); // @rpg-trace: n1790
        } // @rpg-trace: n1790
        
        claimFailure.setFehlerTeil(partNumber != null ? partNumber : ""); // @rpg-trace: n1790
        claimFailure.setBeantrMat(valueMaterial); // @rpg-trace: n1790
        claimFailure.setBeantrArb(valueLabor); // @rpg-trace: n1790
        claimFailure.setBeantrSpez(valueSpecial); // @rpg-trace: n1790
        
        if (textLines != null && !textLines.isEmpty()) { // @rpg-trace: n1790
            if (textLines.size() > 0) claimFailure.setText1(textLines.get(0)); // @rpg-trace: n1790
            if (textLines.size() > 1) claimFailure.setText2(textLines.get(1)); // @rpg-trace: n1790
            if (textLines.size() > 2) claimFailure.setText3(textLines.get(2)); // @rpg-trace: n1790
            if (textLines.size() > 3) claimFailure.setText4(textLines.get(3)); // @rpg-trace: n1790
        } // @rpg-trace: n1790
        
        claimFailureRepository.save(claimFailure); // @rpg-trace: n1790
    } // @rpg-trace: n1790
}