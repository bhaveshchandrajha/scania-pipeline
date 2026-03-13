/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.dto.ClaimCreationRequestDto;
import com.scania.warranty.service.ClaimSubfileService;
import com.scania.warranty.service.ClaimCreationService;
import com.scania.warranty.service.ClaimDeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for claim subfile operations.
 */
@RestController
@RequestMapping("/api/claims/subfile")
public class ClaimSubfileController {

    private final ClaimSubfileService claimSubfileService;
    private final ClaimCreationService claimCreationService;
    private final ClaimDeletionService claimDeletionService;

    @Autowired
    public ClaimSubfileController(ClaimSubfileService claimSubfileService, ClaimCreationService claimCreationService, ClaimDeletionService claimDeletionService) {
        this.claimSubfileService = claimSubfileService;
        this.claimCreationService = claimCreationService;
        this.claimDeletionService = claimDeletionService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<ClaimListItemDto>> listClaims(
            @RequestParam String companyCode,
            @RequestParam(required = false) Integer filterAgeDays,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String searchString,
            @RequestParam(defaultValue = "true") boolean ascending) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createClaim(@RequestBody ClaimCreationRequestDto request) {
        String claimNumber = claimCreationService.generateClaimNumber(request.companyCode(), request.invoiceNumber(), request.invoiceDate(), request.orderNumber(), request.area());
        return ResponseEntity.ok(claimNumber);
    }

    @DeleteMapping("/{companyCode}/{claimNumber}")
    public ResponseEntity<Void> deleteClaim(@PathVariable String companyCode, @PathVariable String claimNumber) {
        claimDeletionService.deleteClaim(companyCode, claimNumber);
        return ResponseEntity.noContent().build();
    }
}