/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.*;
import com.scania.warranty.service.ClaimSearchService;
import com.scania.warranty.service.FailedClaimService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimSearchService claimSearchService;
    private final FailedClaimService failedClaimService;

    public ClaimController(ClaimSearchService claimSearchService, FailedClaimService failedClaimService) {
        this.claimSearchService = claimSearchService;
        this.failedClaimService = failedClaimService;
    }

    /** Failed validation attempts (e.g. repair date &gt; 19 days), newest first. */
    @GetMapping("/failed")
    public List<FailedClaimDto> listFailedClaims(@RequestParam String companyCode) {
        return failedClaimService.listByCompany(companyCode);
    }

    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(@RequestBody ClaimSearchRequestDto request) {
        List<ClaimListItemDto> results = claimSearchService.searchClaims(request); // @rpg-trace: n436
        return ResponseEntity.ok(results);
    }

    /** Claim + ordered history (current status first, then error subfile). */
    @GetMapping("/{companyCode}/{claimNumber}")
    public ResponseEntity<ClaimDetailDto> getClaimDetail(
            @PathVariable String companyCode,
            @PathVariable String claimNumber) {
        Optional<ClaimDetailDto> detail = claimSearchService.getClaimDetail(companyCode, claimNumber);
        return detail.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** REST path delete (Angular UI); body-based POST /delete also supported. */
    @DeleteMapping("/{companyCode}/{claimNumber}")
    public ResponseEntity<Void> deleteClaimByPath(
            @PathVariable String companyCode,
            @PathVariable String claimNumber) {
        boolean deleted = claimSearchService.deleteClaim(new ClaimDeleteRequestDto(companyCode, claimNumber));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteClaim(@RequestBody ClaimDeleteRequestDto request) {
        boolean deleted = claimSearchService.deleteClaim(request); // @rpg-trace: n587
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/update-status")
    public ResponseEntity<Void> updateClaimStatus(@RequestBody ClaimStatusUpdateDto request) {
        claimSearchService.updateClaimStatus(request); // @rpg-trace: n654
        return ResponseEntity.ok().build();
    }

    @PostMapping("/book-minimum")
    public ResponseEntity<Void> bookMinimumClaim(@RequestBody MinimumClaimBookingDto request) {
        claimSearchService.bookMinimumClaim(request); // @rpg-trace: n1664
        return ResponseEntity.ok().build();
    }
}