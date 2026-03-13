/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.V4CheckCriteria;
import com.scania.warranty.dto.V4ValidationRequest;
import com.scania.warranty.dto.V4ValidationResponse;
import com.scania.warranty.service.V4ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v4-validation")
public class V4ValidationController {

    private final V4ValidationService v4ValidationService;

    public V4ValidationController(V4ValidationService v4ValidationService) {
        this.v4ValidationService = v4ValidationService;
    }

    @PostMapping("/check")
    public ResponseEntity<V4ValidationResponse> checkV4(@RequestBody V4ValidationRequest request) {
        V4CheckCriteria criteria = new V4CheckCriteria(
            request.g71000(),
            request.g71010(),
            request.g71020(),
            request.g71030(),
            request.g71040(),
            request.g71190(),
            request.g71200()
        );

        boolean isValid = v4ValidationService.checkV4(criteria);
        return ResponseEntity.ok(new V4ValidationResponse(isValid));
    }
}