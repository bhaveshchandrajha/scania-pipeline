package com.scania.warranty.service;

import com.scania.warranty.domain.*;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimPositionRepository;
import com.scania.warranty.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimCreationService {

    private final ClaimRepository claimRepository;
    private final ClaimErrorRepository claimErrorRepository;
    private final ClaimPositionRepository claimPositionRepository;

    @Autowired
    public ClaimCreationService(
            ClaimRepository claimRepository,
            ClaimErrorRepository claimErrorRepository,
            ClaimPositionRepository claimPositionRepository) {
        this.claimRepository = claimRepository;
        this.claimErrorRepository = claimErrorRepository;
        this.claimPositionRepository = claimPositionRepository;
    }

    @Transactional
    public void createClaim(String companyCode, String invoiceNumber, String invoiceDate, String claimNumber) {
        List<ClaimPosition> positions = claimPositionRepository.findByAbbreviationAndClaimNumberOrderByKeys(
                companyCode, claimNumber);

        if (positions.isEmpty()) {
            return;
        }

        FailureAggregation currentFailure = new FailureAggregation();
        currentFailure.setFailureNumber(1);
        boolean hasCreatedFailures = false;

        for (ClaimPosition position : positions) {
            Integer positionFailureNumber = parseFailureNumber(position.getErrorNumber());

            if (positionFailureNumber > currentFailure.getFailureNumber()) {
                createFailure(companyCode, invoiceNumber, invoiceDate, claimNumber, currentFailure);
                hasCreatedFailures = true;
                currentFailure.reset(positionFailureNumber);
            }

            if (positionFailureNumber > 0) {
                hasCreatedFailures = true;
                processPosition(position, currentFailure);
            }
        }

        if (hasCreatedFailures) {
            createFailure(companyCode, invoiceNumber, invoiceDate, claimNumber, currentFailure);
            updateClaimStatus(companyCode, invoiceNumber, invoiceDate);
        }
    }

    private void processPosition(ClaimPosition position, FailureAggregation aggregation) {
        PositionType positionType = PositionType.fromCode(position.getRecordType());

        switch (positionType) {
            case MATERIAL:
                processMaterialPosition(position, aggregation);
                break;
            case LABOR:
                processLaborPosition(position, aggregation);
                break;
            case TEXT:
                processTextPosition(position, aggregation);
                break;
            default:
                processSpecialPosition(position, aggregation);
                break;
        }
    }

    private void processMaterialPosition(ClaimPosition position, FailureAggregation aggregation) {
        BigDecimal lineValue = position.getValue().multiply(new BigDecimal(position.getQuantity()));
        aggregation.setValueMaterial(aggregation.getValueMaterial().add(lineValue));

        if (lineValue.compareTo(aggregation.getPartValue()) > 0) {
            aggregation.setPartNumber(position.getNumber());
            aggregation.setPartValue(lineValue);
        }
    }

    private void processLaborPosition(ClaimPosition position, FailureAggregation aggregation) {
        BigDecimal lineValue = position.getValue().multiply(new BigDecimal(position.getQuantity()));
        aggregation.setValueLabor(aggregation.getValueLabor().add(lineValue));

        String textUpper = claimPositionRepository.convertToUpperCase(position.getText());
        if (textUpper != null && textUpper.contains("WARTUNG")) {
            aggregation.setMaintenance(true);
            if (aggregation.getGroups().isEmpty() && position.getNumber().length() >= 4) {
                aggregation.setGroups(position.getNumber().substring(0, 4));
            }
        }

        if (aggregation.getGroups().isEmpty() && position.getNumber().length() >= 4) {
            aggregation.setGroups(position.getNumber().substring(0, 4));
        }
    }

    private void processTextPosition(ClaimPosition position, FailureAggregation aggregation) {
        String text = position.getText();
        if (text != null && !text.isEmpty() && !text.startsWith("+")) {
            aggregation.addTextLine(text);
        }
    }

    private void processSpecialPosition(ClaimPosition position, FailureAggregation aggregation) {
        aggregation.setValueSpecial(aggregation.getValueSpecial().add(position.getValue()));
    }

    private void createFailure(String companyCode, String invoiceNumber, String invoiceDate,
                                String claimNumber, FailureAggregation aggregation) {
        ClaimError error = new ClaimError();
        error.setCompanyCode(companyCode);
        error.setInvoiceNumber(invoiceNumber);
        error.setInvoiceDate(invoiceDate);
        error.setClaimNumber(claimNumber);

        String errorNumberStr = String.format("%02d", aggregation.getFailureNumber());
        error.setErrorNumber(errorNumberStr);
        error.setSequenceNumber("00");

        if (aggregation.getGroups().length() >= 2) {
            error.setMainGroup(aggregation.getGroups().substring(0, 2));
        } else {
            error.setMainGroup("");
        }

        if (aggregation.getGroups().length() >= 4) {
            error.setSubGroup(aggregation.getGroups().substring(2, 4));
        } else {
            error.setSubGroup("");
        }

        error.setErrorPart(aggregation.getPartNumber());

        List<String> textLines = aggregation.getTextLines();
        error.setText1(textLines.size() > 0 ? textLines.get(0) : "");
        error.setText2(textLines.size() > 1 ? textLines.get(1) : "");
        error.setText3(textLines.size() > 2 ? textLines.get(2) : "");
        error.setText4(textLines.size() > 3 ? textLines.get(3) : "");

        error.setRequestedMaterial(aggregation.getValueMaterial());
        error.setRequestedLabor(aggregation.getValueLabor());
        error.setRequestedSpecial(aggregation.getValueSpecial());

        error.setOrderNumber("");
        error.setScope("");
        error.setDamageCode1("");
        error.setDamageCode2("");
        error.setTaxCode("");
        error.setEvaluationCode1("");
        error.setEvaluationCode2(0);
        error.setEvaluationDate(0);
        error.setCompensationMaterial(0);
        error.setCompensationLabor(0);
        error.setCompensationSpecial(0);
        error.setClaimType(0);
        error.setPreviousRepairDate(0);
        error.setPreviousMileage(0);
        error.setFieldTestNumber(0);
        error.setCampaignNumber("");
        error.setEps("");
        error.setStatusCode(0);
        error.setVariantCode(0);
        error.setActionCode(0);
        error.setErrorNumberSde("");
        error.setAttachment("");
        error.setSource("");
        error.setComplain("");
        error.setSymptom("");
        error.setFailure("");
        error.setLocation("");
        error.setRepair("");
        error.setResultCode("");
        error.setResult1("");
        error.setResult2("");
        error.setFault1("");
        error.setFault2("");
        error.setReply1("");
        error.setReply2("");
        error.setExplanation1("");
        error.setExplanation2("");

        claimErrorRepository.save(error);
    }

    private void updateClaimStatus(String companyCode, String invoiceNumber, String invoiceDate) {
        Optional<Claim> claimOpt = claimRepository.findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
                companyCode, invoiceNumber, invoiceDate);

        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            claim.setStatusCodeSde(ClaimStatus.SUBMITTED.getCode());
            claimRepository.save(claim);
        }
    }

    private Integer parseFailureNumber(String errorNumber) {
        if (errorNumber == null || errorNumber.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(errorNumber.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}