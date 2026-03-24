/**
 * REST controller for warranty claim APIs.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.web;

import com.scania.warranty.domain.CheckV4Request;
import com.scania.warranty.dto.CheckV4RequestDto;
import com.scania.warranty.dto.CheckV4ResultDto;
import com.scania.warranty.service.CheckV4Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class CheckV4Controller {

    private final CheckV4Service checkV4Service;

    @Autowired
    public CheckV4Controller(CheckV4Service checkV4Service) {
        this.checkV4Service = checkV4Service;
    }

    /**
     * Checks whether a V4 extended part agreement exists for the given claim fields.
     */
    @PostMapping("/check-v4")
    public ResponseEntity<CheckV4ResultDto> checkV4(@RequestBody CheckV4RequestDto requestDto) {
        CheckV4Request request = new CheckV4Request( // @rpg-trace: n1985
            requestDto.g71000(),
            requestDto.g71010(),
            requestDto.g71020(),
            requestDto.g71030(),
            requestDto.g71040(),
            requestDto.g71190(),
            requestDto.g71200()
        );

        boolean result = checkV4Service.checkV4(request); // @rpg-trace: n1985

        return ResponseEntity.ok(new CheckV4ResultDto(result));
    }
}