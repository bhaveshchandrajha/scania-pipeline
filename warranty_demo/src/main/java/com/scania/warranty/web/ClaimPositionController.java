/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n2020}.
 */

package com.scania.warranty.web;

import com.scania.warranty.service.ClaimPositionRenumberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for claim position operations.
 */
@RestController
@RequestMapping("/api/claim-positions")
public class ClaimPositionController {

    private final ClaimPositionRenumberService claimPositionRenumberService;

    public ClaimPositionController(ClaimPositionRenumberService claimPositionRenumberService) {
        this.claimPositionRenumberService = claimPositionRenumberService;
    }

    @PostMapping("/renumber")
    public ResponseEntity<Void> renumberPositions(
            @RequestParam String dealerId,
            @RequestParam String claimNo) {
        claimPositionRenumberService.reNumberPositions(dealerId, claimNo);
        return ResponseEntity.ok().build();
    }
}