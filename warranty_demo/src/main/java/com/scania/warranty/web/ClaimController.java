/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.service.ClaimCreationService;
import com.scania.warranty.service.ClaimManagementService;
import com.scania.warranty.service.ClaimSearchService;
import com.scania.warranty.service.ClaimStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for claim operations.
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimSearchService claimSearchService;
    private final ClaimCreationService claimCreationService;
    private final ClaimManagementService claimManagementService;
    private final ClaimStatusService claimStatusService;

    @Autowired
    public ClaimController(ClaimSearchService claimSearchService,
                          ClaimCreationService claimCreationService,
                          ClaimManagementService claimManagementService,
                          ClaimStatusService claimStatusService) {
        this.claimSearchService = claimSearchService;
        this.claimCreationService = claimCreationService;
        this.claimManagementService = claimManagementService;
        this.claimStatusService = claimStatusService;
    }

    @GetMapping
    public ResponseEntity<Object> getClaimsInfo() {
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Use POST /api/claims/search to search, POST /api/claims/create to create",
            "endpoints", java.util.List.of(
                "POST /api/claims/search",
                "POST /api/claims/create?companyCode=&invoiceNumber=&invoiceDate=&orderNumber=&workshopType="
            )
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(@RequestBody ClaimSearchCriteria criteria) {
        List<ClaimListItemDto> results = claimSearchService.searchClaims(criteria);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createClaim(@RequestParam String companyCode,
                                             @RequestParam String invoiceNumber,
                                             @RequestParam String invoiceDate,
                                             @RequestParam String orderNumber,
                                             @RequestParam String workshopType) {
        Claim claim = claimManagementService.createClaimFromInvoice(companyCode, invoiceNumber, invoiceDate,
                                                                         orderNumber, workshopType);
        return ResponseEntity.ok(claim.getClaimNr());
    }

    @PutMapping("/{companyCode}/{claimNumber}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String companyCode,
                                            @PathVariable String claimNumber,
                                            @RequestParam int newStatus) {
        claimStatusService.updateClaimStatus(companyCode, claimNumber, newStatus);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{companyCode}/{claimNumber}")
    public ResponseEntity<Void> deleteClaim(@PathVariable String companyCode,
                                           @PathVariable String claimNumber) {
        claimStatusService.deleteClaimAndErrors(companyCode, claimNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{companyCode}/{claimNumber}/post-minimum")
    public ResponseEntity<Void> postMinimum(@PathVariable String companyCode,
                                           @PathVariable String claimNumber) {
        claimStatusService.postMinimumClaim(companyCode, claimNumber);
        return ResponseEntity.ok().build();
    }
}