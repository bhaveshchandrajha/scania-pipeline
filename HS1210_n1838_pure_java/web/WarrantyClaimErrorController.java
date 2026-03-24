package com.scania.warranty.web;

import com.scania.warranty.domain.FailureCreationRequest;
import com.scania.warranty.domain.WarrantyClaimError;
import com.scania.warranty.dto.FailureCreationDto;
import com.scania.warranty.dto.WarrantyClaimErrorDto;
import com.scania.warranty.service.WarrantyClaimErrorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warranty/claim-errors")
public class WarrantyClaimErrorController {

    private final WarrantyClaimErrorService errorService;

    public WarrantyClaimErrorController(WarrantyClaimErrorService errorService) {
        this.errorService = errorService;
    }

    @PostMapping
    public ResponseEntity<WarrantyClaimErrorDto> createFailure(
            @RequestParam String companyCode,
            @RequestParam String claimNumber,
            @RequestParam String claimSequence,
            @RequestParam String invoiceNumber,
            @RequestParam String invoiceSequence,
            @RequestParam String claimLineNumber,
            @RequestParam String approvalReleaseNumber,
            @RequestParam String defaultPartNumber,
            @RequestParam String defaultServiceCode,
            @RequestParam String defaultControlCode,
            @RequestParam String btsCode,
            @RequestBody FailureCreationDto dto) {

        FailureCreationRequest request = new FailureCreationRequest(
            companyCode,
            claimNumber,
            claimSequence,
            invoiceNumber,
            invoiceSequence,
            claimLineNumber,
            approvalReleaseNumber,
            defaultPartNumber,
            defaultServiceCode,
            defaultControlCode,
            btsCode,
            dto.failureCode(),
            dto.groups(),
            dto.partNumber(),
            dto.maintenance(),
            dto.textLines(),
            dto.materialValue(),
            dto.laborValue(),
            dto.specialValue()
        );

        WarrantyClaimError error = errorService.createFailure(request);
        WarrantyClaimErrorDto responseDto = toDto(error);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    private WarrantyClaimErrorDto toDto(WarrantyClaimError error) {
        return new WarrantyClaimErrorDto(
            error.getCompanyCode(),
            error.getClaimNumber(),
            error.getClaimSequence(),
            error.getInvoiceNumber(),
            error.getInvoiceSequence(),
            error.getClaimLineNumber(),
            error.getFailureCode(),
            error.getPartNumber(),
            error.getMainGroup(),
            error.getSubGroup(),
            error.getServiceCode(),
            error.getTextLine1(),
            error.getTextLine2(),
            error.getControlCode(),
            error.getMaterialCostPercentage(),
            error.getLaborCostPercentage(),
            error.getSpecialCostPercentage(),
            error.getMaterialValue(),
            error.getLaborValue(),
            error.getSpecialValue(),
            error.getQuantity(),
            error.getStatusFlag(),
            error.getTextLine3(),
            error.getTextLine4()
        );
    }
}