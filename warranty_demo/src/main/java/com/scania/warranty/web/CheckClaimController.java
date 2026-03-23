/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.ClaimCheckRequest;
import com.scania.warranty.dto.ClaimCheckResultDto;
import com.scania.warranty.service.CheckClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class CheckClaimController {

    private final CheckClaimService checkClaimService;

    public CheckClaimController(CheckClaimService checkClaimService) {
        this.checkClaimService = checkClaimService;
    }

    /**
     * Validates a claim before submission.
     * POST /api/claims/check
     */
    @PostMapping("/check")
    public ResponseEntity<ClaimCheckResultDto> checkClaim(@RequestBody ClaimCheckRequest request) {
        ClaimCheckResultDto result = checkClaimService.checkClaim(request); // @rpg-trace: n1919
        return ResponseEntity.ok(result);
    }
}