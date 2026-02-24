package com.scania.warranty.web;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.dto.ClaimDto;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.dto.ClaimSearchRequestDto;
import com.scania.warranty.service.ClaimCreationService;
import com.scania.warranty.service.ClaimSearchService;
import com.scania.warranty.service.ClaimStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    
    private final ClaimSearchService claimSearchService;
    private final ClaimCreationService claimCreationService;
    private final ClaimStatusService claimStatusService;

    public ClaimController(ClaimSearchService claimSearchService,
                          ClaimCreationService claimCreationService,
                          ClaimStatusService claimStatusService) {
        this.claimSearchService = claimSearchService;
        this.claimCreationService = claimCreationService;
        this.claimStatusService = claimStatusService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(@RequestBody ClaimSearchRequestDto request) {
        ClaimSearchCriteria criteria = new ClaimSearchCriteria(
            request.companyCode(),
            request.statusFilter(),
            request.statusOperator(),
            request.vehicleNumberFilter(),
            request.customerNumberFilter(),
            request.sdeClaimNumberFilter(),
            request.claimTypeFilter(),
            request.claimAgeDays(),
            request.openClaimsOnly(),
            request.minimumRequestsOnly(),
            request.searchText(),
            request.sortAscending(),
            request.sortField()
        );
        
        List<Claim> claims = claimSearchService.searchClaims(criteria);
        
        List<ClaimListItemDto> dtos = claims.stream()
            .map(this::toListItemDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createClaim(@RequestParam String companyCode,
                                             @RequestParam String invoiceNumber,
                                             @RequestParam String invoiceDate,
                                             @RequestParam String orderNumber,
                                             @RequestParam String workshopCounter,
                                             @RequestParam String split) {
        String claimNumber = claimCreationService.createClaimFromInvoice(
            companyCode, invoiceNumber, invoiceDate, orderNumber, workshopCounter, split);
        
        return ResponseEntity.ok(claimNumber);
    }

    @PutMapping("/{companyCode}/{claimNumber}/status")
    public ResponseEntity<Void> updateClaimStatus(@PathVariable String companyCode,
                                                  @PathVariable String claimNumber,
                                                  @RequestParam int newStatusCode) {
        claimStatusService.updateClaimStatus(companyCode, claimNumber, newStatusCode);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{companyCode}/{claimNumber}")
    public ResponseEntity<Void> deleteClaim(@PathVariable String companyCode,
                                           @PathVariable String claimNumber) {
        claimStatusService.markClaimAsDeleted(companyCode, claimNumber);
        return ResponseEntity.ok().build();
    }

    private ClaimListItemDto toListItemDto(Claim claim) {
        String statusColor = claimStatusService.getClaimStatusColor(claim);
        
        return new ClaimListItemDto(
            claim.getClaimNumber(),
            claim.getInvoiceNumber(),
            claim.getInvoiceDate(),
            claim.getChassisNumber(),
            claim.getCustomerNumber(),
            claim.getCustomerName(),
            "",
            claim.getStatusCodeSde(),
            claim.getErrorCount(),
            statusColor
        );
    }
}