/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.ClaimCheckContext;
import com.scania.warranty.dto.ClaimCheckRequestDto;
import com.scania.warranty.dto.ClaimCheckResultDto;
import com.scania.warranty.service.ClaimCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ClaimCheckController {

    private final ClaimCheckService claimCheckService;

    public ClaimCheckController(ClaimCheckService claimCheckService) {
        this.claimCheckService = claimCheckService;
    }

    @PostMapping("/check-from-request")
    public ResponseEntity<ClaimCheckResultDto> checkClaim(@RequestBody ClaimCheckRequestDto request) {
        ClaimCheckContext context = new ClaimCheckContext( // @rpg-trace: n1919
                request.g73000(),
                request.g73050(),
                request.g73060(),
                request.g73065(),
                request.g73070(),
                request.g73280(),
                request.g73360(),
                request.g73370(),
                request.g73380(),
                request.g73390(),
                request.g73400(),
                request.g73410()
        );

        ClaimCheckResultDto result = claimCheckService.checkClaim(context); // @rpg-trace: n1919

        if (result.valid()) {
            return ResponseEntity.ok(result); // @rpg-trace: n1981
        } else {
            return ResponseEntity.unprocessableEntity().body(result); // @rpg-trace: n1979
        }
    }
}