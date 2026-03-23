/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.V4CheckCriteria;
import com.scania.warranty.dto.V4CheckResultDto;
import com.scania.warranty.service.V4CheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for V4 check operations.
 * Exposes the CheckV4 procedure as a REST endpoint.
 */
@RestController
@RequestMapping("/api/v4-check")
public class V4CheckController {

    private final V4CheckService v4CheckService;

    public V4CheckController(V4CheckService v4CheckService) {
        this.v4CheckService = v4CheckService;
    }

    /**
     * Performs the V4 check and returns a detailed result.
     *
     * @param g71000 key field 0
     * @param g71010 key field 1
     * @param g71020 key field 2
     * @param g71030 key field 3
     * @param g71040 key field 4
     * @param g71190 key field 5
     * @param g71200 full field from which variant code is extracted
     * @return V4 check result with details
     */
    @GetMapping("/verify")
    public ResponseEntity<V4CheckResultDto> checkV4(
            @RequestParam String g71000,
            @RequestParam String g71010,
            @RequestParam String g71020,
            @RequestParam String g71030,
            @RequestParam String g71040,
            @RequestParam String g71190,
            @RequestParam String g71200) {

        V4CheckCriteria criteria = new V4CheckCriteria(
            g71000, g71010, g71020, g71030, g71040, g71190, g71200
        );

        V4CheckResultDto result = v4CheckService.checkV4WithDetails(criteria); // @rpg-trace: n1985
        return ResponseEntity.ok(result);
    }

    /**
     * Performs the V4 check via POST with a request body.
     *
     * @param criteria the V4 check criteria
     * @return V4 check result with details
     */
    @PostMapping("/verify")
    public ResponseEntity<V4CheckResultDto> checkV4Post(@RequestBody V4CheckCriteria criteria) {
        V4CheckResultDto result = v4CheckService.checkV4WithDetails(criteria); // @rpg-trace: n1985
        return ResponseEntity.ok(result);
    }
}