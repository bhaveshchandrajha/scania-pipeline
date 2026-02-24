package com.scania.warranty.web;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimSearchCriteria;
import com.scania.warranty.dto.ClaimDto;
import com.scania.warranty.dto.ClaimCreationRequestDto;
import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.service.ClaimCreationService;
import com.scania.warranty.service.ClaimSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {
    
    private final ClaimSearchService claimSearchService;
    private final ClaimCreationService claimCreationService;
    
    @Autowired
    public ClaimController(ClaimSearchService claimSearchService, 
                          ClaimCreationService claimCreationService) {
        this.claimSearchService = claimSearchService;
        this.claimCreationService = claimCreationService;
    }
    
    @GetMapping
    public ResponseEntity<List<ClaimListItemDto>> getAllClaims(
            @RequestParam(required = false) String pakz,
            @RequestParam(name = "companyCode", required = false) String companyCode,
            @RequestParam(defaultValue = "true") boolean ascending) {
        String pakzFilter = firstNonBlank(pakz, companyCode);
        ClaimSearchCriteria criteria = new ClaimSearchCriteria(
            pakzFilter, // Can be null to get all claims
            null, // status
            null, // filterOperator
            null, // vehicleNumber
            null, // customerNumber
            null, // claimNumberSde
            null, // claimType
            false, // openClaimsOnly
            false, // minimumOnly
            null  // filterDays
        );
        
        List<ClaimListItemDto> results = claimSearchService.searchClaims(criteria, ascending);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaimsGet(
            @RequestParam(required = false) String pakz,
            @RequestParam(name = "companyCode", required = false) String companyCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String filterOperator,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String customerNumber,
            @RequestParam(required = false) String claimNumberSde,
            @RequestParam(required = false) String claimType,
            @RequestParam(defaultValue = "false") boolean openClaimsOnly,
            @RequestParam(defaultValue = "false") boolean minimumOnly,
            @RequestParam(required = false) Integer filterDays,
            @RequestParam(defaultValue = "true") boolean ascending) {
        String pakzFilter = firstNonBlank(pakz, companyCode);
        ClaimSearchCriteria criteria = new ClaimSearchCriteria(
            pakzFilter,
            status,
            filterOperator,
            vehicleNumber,
            customerNumber,
            claimNumberSde,
            claimType,
            openClaimsOnly,
            minimumOnly,
            filterDays
        );
        
        List<ClaimListItemDto> results = claimSearchService.searchClaims(criteria, ascending);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(
            @RequestBody ClaimSearchCriteria criteria,
            @RequestParam(defaultValue = "true") boolean ascending) {
        
        List<ClaimListItemDto> results = claimSearchService.searchClaims(criteria, ascending);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping
    public ResponseEntity<ClaimDto> createClaimFromBody(
            @RequestBody ClaimCreationRequestDto request) {
        
        String pakz = request.companyCode();
        Claim claim = claimCreationService.createClaimFromInvoice(
                pakz,
                request.invoiceNumber(),
                request.invoiceDate(),
                request.jobNumber(),
                request.workshopType()
        );
        
        return ResponseEntity.ok(toDto(claim));
    }
    
    @GetMapping("/{pakz}/{claimNumber}")
    public ResponseEntity<ClaimDto> getClaimByNumber(
            @PathVariable String pakz,
            @PathVariable String claimNumber) {
        
        Optional<Claim> claimOpt = claimSearchService.findClaimByNumber(pakz, claimNumber);
        
        return claimOpt.map(claim -> ResponseEntity.ok(toDto(claim)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/create")
    public ResponseEntity<ClaimDto> createClaim(
            @RequestParam String pakz,
            @RequestParam String invoiceNumber,
            @RequestParam String invoiceDate,
            @RequestParam String orderNumber,
            @RequestParam String area) {
        
        Claim claim = claimCreationService.createClaimFromInvoice(
                pakz, invoiceNumber, invoiceDate, orderNumber, area);
        
        return ResponseEntity.ok(toDto(claim));
    }
    
    @DeleteMapping("/{pakz}/{claimNumber}")
    public ResponseEntity<Void> deleteClaim(
            @PathVariable String pakz,
            @PathVariable String claimNumber) {
        
        claimSearchService.deleteClaim(pakz, claimNumber);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{pakz}/{claimNumber}/status")
    public ResponseEntity<Void> updateClaimStatus(
            @PathVariable String pakz,
            @PathVariable String claimNumber,
            @RequestParam int newStatus) {
        
        claimCreationService.updateClaimStatus(pakz, claimNumber, newStatus);
        return ResponseEntity.ok().build();
    }
    
    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private ClaimDto toDto(Claim claim) {
        return new ClaimDto(
                claim.getPakz(),
                claim.getRechNr(),
                claim.getRechDatum(),
                claim.getAuftragsNr(),
                claim.getWete(),
                claim.getClaimNr(),
                claim.getChassisNr(),
                claim.getKennzeichen(),
                claim.getZulDatum(),
                claim.getRepDatum(),
                claim.getKmStand(),
                claim.getProduktTyp(),
                claim.getAnhang(),
                claim.getAuslaender(),
                claim.getKdNr(),
                claim.getKdName(),
                claim.getClaimNrSde(),
                claim.getStatusCodeSde(),
                claim.getAnzFehler(),
                claim.getBereich(),
                claim.getAufNr()
        );
    }
}