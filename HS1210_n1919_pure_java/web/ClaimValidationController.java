package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimValidationResultDto;
import com.scania.warranty.service.ClaimValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims/validation")
public class ClaimValidationController {

    private final ClaimValidationService claimValidationService;

    public ClaimValidationController(ClaimValidationService claimValidationService) {
        this.claimValidationService = claimValidationService;
    }

    @PostMapping("/check")
    public ResponseEntity<ClaimValidationResultDto> checkClaim(
        @RequestParam String kuerzel,
        @RequestParam String claimNumber,
        @RequestParam String errorNumber,
        @RequestParam String sequenceNumber,
        @RequestParam(required = false) String customerCauseCode,
        @RequestParam(required = false) Long campaignNumber,
        @RequestParam(required = false) String customerMainCode,
        @RequestParam(required = false) String customerSubCode1,
        @RequestParam(required = false) String workshopCauseCode,
        @RequestParam(required = false) String workshopMainCode,
        @RequestParam(required = false) String workshopSubCode1,
        @RequestParam(required = false) String damageCausingPartNumber
    ) {
        ClaimValidationResultDto result = claimValidationService.validateClaim(
            kuerzel,
            claimNumber,
            errorNumber,
            sequenceNumber,
            customerCauseCode,
            campaignNumber,
            customerMainCode,
            customerSubCode1,
            workshopCauseCode,
            workshopMainCode,
            workshopSubCode1,
            damageCausingPartNumber
        );

        return ResponseEntity.ok(result);
    }
}