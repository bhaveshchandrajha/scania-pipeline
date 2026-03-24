package com.scania.warranty.web;

import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.dto.ClaimDto;
import com.scania.warranty.dto.ClaimErrorDto;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.service.ClaimService;
import com.scania.warranty.service.ClaimValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimValidationService validationService;

    public ClaimController(ClaimService claimService,
                          ClaimValidationService validationService) {
        this.claimService = claimService;
        this.validationService = validationService;
    }

    @GetMapping("/{claimNumber}")
    public ResponseEntity<ClaimDto> getClaim(@PathVariable String claimNumber) {
        Optional<ClaimDto> claim = claimService.findClaimByNumber(claimNumber);
        return claim.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(
        @RequestBody ClaimSearchCriteria criteria) {
        List<ClaimListItemDto> claims = claimService.searchClaims(criteria);
        return ResponseEntity.ok(claims);
    }

    @PostMapping
    public ResponseEntity<ClaimDto> createClaim(
        @RequestParam String dealerId,
        @RequestParam String invoiceNumber,
        @RequestParam String invoiceDate,
        @RequestParam String orderNumber,
        @RequestParam String wete,
        @RequestParam(required = false) String chassisNumber,
        @RequestParam(required = false) String registrationNumber,
        @RequestParam(required = false) Integer registrationDate,
        @RequestParam Integer repairDate,
        @RequestParam(required = false) Integer mileage,
        @RequestParam Integer productType) {
        
        if (!claimService.validateClaimData(chassisNumber, productType,
                                           registrationDate, repairDate, mileage)) {
            return ResponseEntity.badRequest().build();
        }
        
        ClaimDto claim = claimService.createClaim(dealerId, invoiceNumber, invoiceDate,
                                                 orderNumber, wete, chassisNumber,
                                                 registrationNumber, registrationDate,
                                                 repairDate, mileage, productType);
        return ResponseEntity.status(HttpStatus.CREATED).body(claim);
    }

    @PutMapping("/{claimNumber}")
    public ResponseEntity<ClaimDto> updateClaim(
        @PathVariable String claimNumber,
        @RequestParam(required = false) Integer registrationDate,
        @RequestParam(required = false) Integer repairDate,
        @RequestParam(required = false) Integer mileage,
        @RequestParam(required = false) String chassisNumber,
        @RequestParam(required = false) String registrationNumber) {
        
        ClaimDto claim = claimService.updateClaim(claimNumber, registrationDate,
                                                 repairDate, mileage, chassisNumber,
                                                 registrationNumber);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/{claimNumber}/errors")
    public ResponseEntity<List<ClaimErrorDto>> getClaimErrors(
        @PathVariable String claimNumber) {
        List<ClaimErrorDto> errors = claimService.getClaimErrors(claimNumber);
        return ResponseEntity.ok(errors);
    }

    @PostMapping("/{claimNumber}/errors")
    public ResponseEntity<ClaimErrorDto> createClaimError(
        @PathVariable String claimNumber,
        @RequestParam String errorNumber,
        @RequestParam(required = false) String damagingPart,
        @RequestParam String mainGroup,
        @RequestParam String damageCode,
        @RequestParam String controlCode,
        @RequestParam String claimType,
        @RequestParam(required = false) String description1,
        @RequestParam(required = false) String description2) {
        
        if (!validationService.validateClaimError(claimNumber, errorNumber, damagingPart,
                                                 mainGroup, damageCode, controlCode,
                                                 claimType, null, null)) {
            return ResponseEntity.badRequest().build();
        }
        
        ClaimErrorDto error = claimService.createClaimError(claimNumber, errorNumber,
                                                           damagingPart, mainGroup,
                                                           damageCode, controlCode,
                                                           claimType, description1,
                                                           description2);
        return ResponseEntity.status(HttpStatus.CREATED).body(error);
    }

    @PutMapping("/{claimNumber}/errors/{errorNumber}")
    public ResponseEntity<ClaimErrorDto> updateClaimError(
        @PathVariable String claimNumber,
        @PathVariable String errorNumber,
        @RequestParam(required = false) String damagingPart,
        @RequestParam(required = false) String mainGroup,
        @RequestParam(required = false) String damageCode,
        @RequestParam(required = false) String controlCode,
        @RequestParam(required = false) String description1,
        @RequestParam(required = false) String description2,
        @RequestParam(required = false) String description3,
        @RequestParam(required = false) String description4) {
        
        if (!validationService.checkErrorStatus(claimNumber, errorNumber, null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ClaimErrorDto error = claimService.updateClaimError(claimNumber, errorNumber,
                                                           damagingPart, mainGroup,
                                                           damageCode, controlCode,
                                                           description1, description2,
                                                           description3, description4);
        return ResponseEntity.ok(error);
    }

    @DeleteMapping("/{claimNumber}/errors/{errorNumber}")
    public ResponseEntity<Void> deleteClaimError(
        @PathVariable String claimNumber,
        @PathVariable String errorNumber) {
        
        if (!validationService.checkErrorStatus(claimNumber, errorNumber, null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        claimService.deleteClaimError(claimNumber, errorNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{targetClaimNumber}/copy-error")
    public ResponseEntity<ClaimDto> copyClaimError(
        @PathVariable String targetClaimNumber,
        @RequestParam String sourceClaimNumber,
        @RequestParam String sourceErrorNumber) {
        
        ClaimDto claim = claimService.copyClaimError(sourceClaimNumber, sourceErrorNumber,
                                                    targetClaimNumber);
        return ResponseEntity.ok(claim);
    }

    @PutMapping("/{claimNumber}/status")
    public ResponseEntity<Void> updateClaimStatus(
        @PathVariable String claimNumber,
        @RequestParam Integer status) {
        
        claimService.updateClaimStatus(claimNumber, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{claimNumber}/campaign-errors")
    public ResponseEntity<List<ClaimErrorDto>> getCampaignErrors(
        @PathVariable String claimNumber) {
        List<ClaimErrorDto> errors = claimService.getCampaignErrors(claimNumber);
        return ResponseEntity.ok(errors);
    }
}