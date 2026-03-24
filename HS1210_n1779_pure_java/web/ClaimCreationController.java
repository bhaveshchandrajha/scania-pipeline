package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimCreationRequest;
import com.scania.warranty.dto.ClaimCreationResponse;
import com.scania.warranty.service.ClaimCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimCreationController {

    private final ClaimCreationService claimCreationService;

    @Autowired
    public ClaimCreationController(ClaimCreationService claimCreationService) {
        this.claimCreationService = claimCreationService;
    }

    @PostMapping("/create")
    public ResponseEntity<ClaimCreationResponse> createClaim(@RequestBody ClaimCreationRequest request) {
        try {
            claimCreationService.createClaim(
                    request.companyCode(),
                    request.invoiceNumber(),
                    request.invoiceDate(),
                    request.claimNumber()
            );

            ClaimCreationResponse response = new ClaimCreationResponse(
                    true,
                    "Claim created successfully",
                    request.claimNumber()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ClaimCreationResponse response = new ClaimCreationResponse(
                    false,
                    "Failed to create claim: " + e.getMessage(),
                    request.claimNumber()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}