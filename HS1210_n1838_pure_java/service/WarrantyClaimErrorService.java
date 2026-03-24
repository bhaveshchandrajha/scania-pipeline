package com.scania.warranty.service;

import com.scania.warranty.domain.ApprovalRecord;
import com.scania.warranty.domain.CostPercentage;
import com.scania.warranty.domain.FailureCreationRequest;
import com.scania.warranty.domain.WarrantyClaimError;
import com.scania.warranty.repository.ApprovalRecordRepository;
import com.scania.warranty.repository.CostPercentageRepository;
import com.scania.warranty.repository.WarrantyClaimErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
public class WarrantyClaimErrorService {

    private final WarrantyClaimErrorRepository errorRepository;
    private final CostPercentageRepository costPercentageRepository;
    private final ApprovalRecordRepository approvalRecordRepository;
    private final ClaimActionService claimActionService;

    public WarrantyClaimErrorService(
            WarrantyClaimErrorRepository errorRepository,
            CostPercentageRepository costPercentageRepository,
            ApprovalRecordRepository approvalRecordRepository,
            ClaimActionService claimActionService) {
        this.errorRepository = errorRepository;
        this.costPercentageRepository = costPercentageRepository;
        this.approvalRecordRepository = approvalRecordRepository;
        this.claimActionService = claimActionService;
    }

    @Transactional
    public WarrantyClaimError createFailure(FailureCreationRequest request) {
        WarrantyClaimError error = new WarrantyClaimError();

        error.setCompanyCode(request.companyCode());
        error.setClaimNumber(request.claimNumber());
        error.setClaimSequence(request.claimSequence());
        error.setInvoiceNumber(request.invoiceNumber());
        error.setInvoiceSequence(request.invoiceSequence());
        error.setClaimLineNumber(request.claimLineNumber());

        String formattedFailureCode = formatFailureCode(request.failureCode());
        error.setFailureCode(formattedFailureCode);

        String effectivePartNumber = determinePartNumber(request.partNumber(), request.defaultPartNumber());
        error.setPartNumber(effectivePartNumber);

        String mainGroup = extractMainGroup(request.groups());
        error.setMainGroup(mainGroup);

        String subGroup = extractSubGroup(request.groups());
        error.setSubGroup(subGroup);

        error.setServiceCode(request.defaultServiceCode());

        error.setTextLine1(request.textLines().get(0));
        error.setTextLine2(request.textLines().get(1));

        String controlCode = determineControlCode(request.defaultControlCode(), request.maintenance(), request.btsCode());
        error.setControlCode(controlCode);

        applyCostPercentages(error, controlCode);

        error.setMaterialValue(request.materialValue());
        error.setLaborValue(request.laborValue());
        error.setSpecialValue(request.specialValue());

        error.setQuantity(1);
        error.setStatusFlag(0);

        error.setTextLine3(request.textLines().get(2));
        error.setTextLine4(request.textLines().get(3));

        String approvalNumber = retrieveApprovalNumber(
            request.companyCode(),
            request.invoiceNumber(),
            request.invoiceSequence(),
            request.approvalReleaseNumber(),
            controlCode
        );
        if (approvalNumber != null && !approvalNumber.isBlank()) {
            String textLine4 = error.getTextLine4();
            if (textLine4 != null && textLine4.length() >= 47) {
                String updatedTextLine4 = textLine4.substring(0, 46) + approvalNumber;
                if (updatedTextLine4.length() > 65) {
                    updatedTextLine4 = updatedTextLine4.substring(0, 65);
                }
                error.setTextLine4(updatedTextLine4);
            }
        }

        error.setFailureCodeCopy(formattedFailureCode);

        WarrantyClaimError savedError = errorRepository.save(error);

        claimActionService.executeClaimAction(
            request.companyCode(),
            request.claimNumber(),
            request.claimSequence(),
            request.invoiceNumber(),
            request.invoiceSequence(),
            request.claimLineNumber(),
            formattedFailureCode,
            "J"
        );

        return savedError;
    }

    private String formatFailureCode(Integer failureCode) {
        if (failureCode == null) {
            return "00";
        }
        DecimalFormat formatter = new DecimalFormat("00");
        return formatter.format(failureCode);
    }

    private String determinePartNumber(String partNumber, String defaultPartNumber) {
        if (partNumber == null || partNumber.isBlank()) {
            return defaultPartNumber != null ? defaultPartNumber : "";
        }
        return partNumber;
    }

    private String extractMainGroup(String groups) {
        if (groups == null || groups.length() < 2) {
            return "00";
        }
        String mainGroup = groups.substring(0, 2);
        if (mainGroup.isBlank()) {
            return "00";
        }
        return mainGroup;
    }

    private String extractSubGroup(String groups) {
        if (groups == null || groups.length() < 4) {
            return "00";
        }
        String subGroup = groups.substring(2, 4);
        if (subGroup.isBlank()) {
            return "00";
        }
        return subGroup;
    }

    private String determineControlCode(String defaultControlCode, boolean maintenance, String btsCode) {
        String controlCode = defaultControlCode != null ? defaultControlCode : "";
        
        if (maintenance && controlCode.length() >= 1) {
            controlCode = "W" + controlCode.substring(1);
        }

        if ("SAT".equals(btsCode) && controlCode.length() >= 2 && "2".equals(controlCode.substring(1, 2))) {
            controlCode = controlCode.substring(0, 1) + "8";
        }

        return controlCode;
    }

    private void applyCostPercentages(WarrantyClaimError error, String controlCode) {
        Optional<CostPercentage> costPercentageOpt = costPercentageRepository.findByControlCode(controlCode);
        
        if (costPercentageOpt.isPresent()) {
            CostPercentage costPercentage = costPercentageOpt.get();
            error.setMaterialCostPercentage(costPercentage.getMaterialCostPercentage());
            error.setLaborCostPercentage(costPercentage.getLaborCostPercentage());
            error.setSpecialCostPercentage(costPercentage.getSpecialCostPercentage());
        } else {
            error.setMaterialCostPercentage(BigDecimal.ZERO);
            error.setLaborCostPercentage(BigDecimal.ZERO);
            error.setSpecialCostPercentage(BigDecimal.ZERO);
        }
    }

    private String retrieveApprovalNumber(
            String companyCode,
            String invoiceNumber,
            String invoiceSequence,
            String approvalReleaseNumber,
            String controlCode) {
        
        String scope = getScope(controlCode);
        if (scope == null || scope.isEmpty() || !scope.substring(0, 1).equals("R")) {
            return null;
        }

        String invoiceKey = invoiceNumber + invoiceSequence;
        List<ApprovalRecord> approvals = approvalRecordRepository.findApprovalsByCompanyAndInvoiceKeyAndReleaseNumber(
            companyCode,
            invoiceKey,
            approvalReleaseNumber
        );

        if (!approvals.isEmpty()) {
            return approvals.get(0).getApprovalNumber();
        }

        return null;
    }

    private String getScope(String controlCode) {
        if (controlCode == null || controlCode.isEmpty()) {
            return "";
        }
        return controlCode.substring(0, 1);
    }
}