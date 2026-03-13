/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimCreationRequest;
import com.scania.warranty.service.ClaimCreationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimCreationController {
    
    private final ClaimCreationService claimCreationService;
    
    public ClaimCreationController(ClaimCreationService claimCreationService) {
        this.claimCreationService = claimCreationService;
    }
    
    @PostMapping("/create-from-positions")
    public ResponseEntity<Void> createClaim(@RequestBody ClaimCreationRequest request) { // @rpg-trace: n1779
        claimCreationService.createClaim(
            request.kuerzel(),
            request.claimNr(),
            request.pakz(),
            request.rechNr(),
            request.rechDatum(),
            request.auftragsNr(),
            request.bereich()
        ); // @rpg-trace: n1779
        
        return ResponseEntity.ok().build(); // @rpg-trace: n1836
    }
}