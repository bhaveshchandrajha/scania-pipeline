/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.*;
import com.scania.warranty.service.ClaimSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimSearchService claimSearchService;

    public ClaimController(ClaimSearchService claimSearchService) {
        this.claimSearchService = claimSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(@RequestBody ClaimSearchRequestDto request) {
        List<ClaimListItemDto> results = claimSearchService.searchClaims(request); // @rpg-trace: n436
        return ResponseEntity.ok(results);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteClaim(@RequestBody ClaimDeleteRequestDto request) {
        claimSearchService.deleteClaim(request); // @rpg-trace: n587
        return ResponseEntity.ok().build();
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