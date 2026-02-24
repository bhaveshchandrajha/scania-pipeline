package com.scania.warranty.web;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.dto.ClaimCreationRequestDto;
import com.scania.warranty.dto.ClaimSearchResultDto;
import com.scania.warranty.service.ClaimCreationService;
import com.scania.warranty.service.ClaimSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    
    private final ClaimSearchService claimSearchService;
    private final ClaimCreationService claimCreationService;

    public ClaimController(ClaimSearchService claimSearchService,
                          ClaimCreationService claimCreationService) {
        this.claimSearchService = claimSearchService;
        this.claimCreationService = claimCreationService;
    }

    @PostMapping("/search")
    public ResponseEntity<ClaimSearchResultDto> searchClaims(
            @RequestBody ClaimSearchCriteria criteria,
            @RequestParam(defaultValue = "true") boolean ascending) {
        ClaimSearchResultDto result = claimSearchService.searchClaims(criteria, ascending);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<ClaimSearchResultDto> searchClaimsGet(
            @RequestParam(required = false) String companyCode,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) String statusOperator,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String customerNumber,
            @RequestParam(required = false) String claimNumberSde,
            @RequestParam(required = false) String claimType,
            @RequestParam(defaultValue = "false") boolean openClaimsOnly,
            @RequestParam(defaultValue = "false") boolean minimumOnly,
            @RequestParam(required = false) Integer claimAgeDays,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "true") boolean ascending) {
        // Build criteria from query parameters
        ClaimSearchCriteria criteria = new ClaimSearchCriteria(
            companyCode != null ? companyCode : "",
            statusFilter,
            statusOperator != null ? statusOperator : "=",
            vehicleNumber,
            customerNumber,
            claimNumberSde,
            claimType,
            openClaimsOnly,
            minimumOnly,
            claimAgeDays,
            searchText
        );
        ClaimSearchResultDto result = claimSearchService.searchClaims(criteria, ascending);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<String> getClaimsInfo() {
        return ResponseEntity.ok("Warranty Claims API\n\n" +
            "Endpoints:\n" +
            "  GET  /api/claims/search?companyCode=XXX - Search claims\n" +
            "  POST /api/claims/search - Search claims (with JSON body)\n" +
            "  POST /api/claims - Create claim\n\n" +
            "Example: GET /api/claims/search?companyCode=001");
    }

    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestBody ClaimCreationRequestDto request) {
        Claim claim = claimCreationService.createClaimFromInvoice(
            request.companyCode(),
            request.invoiceNumber(),
            request.invoiceDate(),
            request.jobNumber(),
            request.workshopType()
        );
        return ResponseEntity.ok(claim);
    }
}