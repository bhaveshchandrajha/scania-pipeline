package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.dto.ClaimSearchCriteria;
import com.scania.warranty.service.ClaimSubfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimSubfileController {
    
    private final ClaimSubfileService claimSubfileService;
    
    @Autowired
    public ClaimSubfileController(ClaimSubfileService claimSubfileService) {
        this.claimSubfileService = claimSubfileService;
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<ClaimListItemDto>> searchClaims(@RequestBody ClaimSearchCriteria criteria) {
        List<ClaimListItemDto> claims = claimSubfileService.buildClaimSubfile(criteria);
        return ResponseEntity.ok(claims);
    }
    
    @PutMapping("/{pakz}/{claimnr}/status")
    public ResponseEntity<Void> updateClaimStatus(
            @PathVariable String pakz,
            @PathVariable String claimnr,
            @RequestParam Integer newStatus) {
        claimSubfileService.updateClaimStatus(pakz, claimnr, newStatus);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{pakz}/{claimnr}")
    public ResponseEntity<Void> deleteClaim(
            @PathVariable String pakz,
            @PathVariable String claimnr) {
        claimSubfileService.deleteClaimWithDetails(pakz, claimnr);
        return ResponseEntity.ok().build();
    }
}