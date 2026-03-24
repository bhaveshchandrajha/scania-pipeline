/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.web;

import com.scania.warranty.dto.ClaimListItemDto;
import com.scania.warranty.service.ClaimSubfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims/subfile")
public class ClaimSubfileController {

    private final ClaimSubfileService claimSubfileService; // @rpg-trace: n422

    public ClaimSubfileController(ClaimSubfileService claimSubfileService) {
        this.claimSubfileService = claimSubfileService;
    }

    @GetMapping
    public ResponseEntity<List<ClaimListItemDto>> getClaimSubfile(
            @RequestParam String companyCode,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam(required = false) Integer filterAgeDays,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String filterCompany,
            @RequestParam(required = false) String filterChassis,
            @RequestParam(required = false) String filterCustomer,
            @RequestParam(required = false) String filterSdeNumber,
            @RequestParam(defaultValue = "false") boolean filterOpenOnly,
            @RequestParam(required = false) String searchString
    ) {
        List<ClaimListItemDto> claims = claimSubfileService.buildSubfileList( // @rpg-trace: n428
                companyCode,
                ascending,
                filterAgeDays,
                filterType,
                filterStatus,
                filterCompany,
                filterChassis,
                filterCustomer,
                filterSdeNumber,
                filterOpenOnly,
                searchString
        );
        return ResponseEntity.ok(claims); // @rpg-trace: n541
    }
}