/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.dto.FailureCreationRequest;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for creating claim failures/errors.
 */
@Service
public class ClaimFailureService {

    private final ClaimErrorRepository claimErrorRepository;
    private final ClaimRepository claimRepository;

    public ClaimFailureService(ClaimErrorRepository claimErrorRepository, ClaimRepository claimRepository) {
        this.claimErrorRepository = claimErrorRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional
    public void createFailure(String companyCode, String claimNumber, FailureCreationRequest request) {
        Claim claim = claimRepository.findByPakzAndClaimNr(companyCode, claimNumber)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + companyCode + "/" + claimNumber));

        ClaimError error = new ClaimError();

        // @origin HS1210 L887-887 (EVAL) - all composite key fields must be set
        error.setPakz(companyCode);
        error.setRechNr(claim.getRechNr() != null ? claim.getRechNr() : "");
        error.setRechDatum(claim.getRechDatum() != null ? claim.getRechDatum() : "");
        error.setAuftragsNr(claim.getAuftragsNr() != null ? claim.getAuftragsNr() : "");
        error.setBereich(claim.getBereich() != null ? claim.getBereich() : "");
        error.setClaimNr(claimNumber);
        error.setFehlerNr(formatErrorNumber(request.failureNumber()));
        error.setFolgeNr("00");

        String mainGroup = extractMainGroup(request.groups());
        String subGroup = extractSubGroup(request.groups());
        // @origin HS1210 L906-906 (EVAL)
        error.setHauptgruppe(mainGroup);
        error.setNebengruppe(subGroup);

        error.setFehlerTeil(request.partNumber() != null ? request.partNumber() : "");

        List<String> textLines = request.textLines();
        error.setText1(textLines.size() > 0 ? textLines.get(0) : "");
        error.setText2(textLines.size() > 1 ? textLines.get(1) : "");
        error.setText3(textLines.size() > 2 ? textLines.get(2) : "");
        error.setText4(textLines.size() > 3 ? textLines.get(3) : "");

        error.setBeantrMat(request.valueMaterial() != null ? request.valueMaterial() : BigDecimal.ZERO);
        error.setBeantrgArb(request.valueLabor() != null ? request.valueLabor() : BigDecimal.ZERO);
        error.setBeantrgSpez(request.valueSpecial() != null ? request.valueSpecial() : BigDecimal.ZERO);

        error.setSchadC1("");
        error.setSchadC2("");
        error.setSteuerCode("");
        error.setBewCode1("");
        error.setBewCode2(0);
        error.setBewDatum(0);
        error.setVergMat(0);
        error.setVergArb(0);
        error.setVergSpez(0);
        error.setClaimArt(0);
        error.setvRepDatum(0);
        error.setvKmStand(0);
        error.setFeldtestNr(0);
        error.setKampagnenNr("");
        error.setEps("");
        error.setStatusCode(0);
        error.setVariantCode(0);
        error.setActionCode(0);
        error.setFehlerNrSde("");
        error.setAnhang("");
        error.setSource("");
        error.setComplain("");
        error.setSymptom("");
        error.setFailure("");
        error.setLocation("");
        error.setRepair("");
        error.setErgCode("");
        error.setResult1("");
        error.setResult2("");
        error.setFault1("");
        error.setFault2("");
        error.setReply1("");
        error.setReply2("");
        error.setExplanation1("");
        error.setExplanation2("");

        // @origin HS1210 L860-860 (WRITE)
        claimErrorRepository.save(error);
    }

    private String formatErrorNumber(Integer failureNumber) {
        // @origin HS1210 L830-833 (IF)
        if (failureNumber == null || failureNumber == 0) {
            return "00";
        }
        return String.format("%02d", failureNumber);
    }

    private String extractMainGroup(String groups) {
        if (groups == null || groups.length() < 2) {
            return "";
        }
        return groups.substring(0, 2);
    }

    private String extractSubGroup(String groups) {
        if (groups == null || groups.length() < 4) {
            return "";
        }
        return groups.substring(2, 4);
    }
}