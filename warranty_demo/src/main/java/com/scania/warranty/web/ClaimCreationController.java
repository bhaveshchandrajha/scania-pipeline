/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimCreationRequestDto;
import com.scania.warranty.dto.ClaimCreationResponseDto;
import com.scania.warranty.service.ClaimCreationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimCreationController {

    private final ClaimCreationService claimCreationService; // @rpg-trace: n950

    public ClaimCreationController(ClaimCreationService claimCreationService) {
        this.claimCreationService = claimCreationService;
    }

    /**
     * Create claim from invoice - matches Angular ClaimService.create() API.
     * Accepts query params: companyCode, invoiceNumber, invoiceDate, orderNumber, workshopType.
     */
    @PostMapping(value = "/create", produces = "text/plain")
    public ResponseEntity<String> createClaim(
            @RequestParam String companyCode,
            @RequestParam String invoiceNumber,
            @RequestParam String invoiceDate,
            @RequestParam String orderNumber,
            @RequestParam String workshopType) {
        try {
            // Map workshopType "1" to "A" for demo compatibility with seeded invoices
            String serviceType = "1".equals(workshopType) ? "A" : workshopType;
            String claimNumber = claimCreationService.createClaimFromInvoice(
                    companyCode, invoiceNumber, invoiceDate, orderNumber, serviceType);
            return ResponseEntity.ok(claimNumber);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-from-invoice")
    public ResponseEntity<ClaimCreationResponseDto> createClaimFromInvoice(@RequestBody ClaimCreationRequestDto request) {
        try {
            String claimNumber = claimCreationService.createClaimFromInvoice( // @rpg-trace: n950
                    request.companyCode(),
                    request.invoiceNumber(),
                    request.invoiceDate(),
                    request.workshopCode(),
                    request.serviceType()
            );
            return ResponseEntity.ok(new ClaimCreationResponseDto(claimNumber, "Claim created successfully")); // @rpg-trace: n1114
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ClaimCreationResponseDto(null, e.getMessage())); // @rpg-trace: n985
        }
    }
}