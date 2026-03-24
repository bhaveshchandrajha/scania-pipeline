package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.FailureAggregation;
import com.scania.warranty.repository.ClaimErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ClaimFailureCreationService {
    
    private final ClaimErrorRepository claimErrorRepository;
    
    public ClaimFailureCreationService(ClaimErrorRepository claimErrorRepository) {
        this.claimErrorRepository = claimErrorRepository;
    }
    
    @Transactional
    public void createFailure(
        String companyCode,
        String invoiceNumber,
        String invoiceDate,
        String claimNumber,
        FailureAggregation aggregation
    ) {
        if (aggregation.getFailureNumber() == null || aggregation.getFailureNumber() == 0) {
            return;
        }
        
        ClaimError error = new ClaimError();
        
        error.setCompanyCode(companyCode);
        error.setInvoiceNumber(invoiceNumber);
        error.setInvoiceDate(invoiceDate);
        error.setClaimNumber(claimNumber);
        
        String errorNumberStr = String.format("%02d", aggregation.getFailureNumber());
        error.setErrorNumber(errorNumberStr);
        error.setSequenceNumber("00");
        
        error.setErrorPart(aggregation.getPartNumber() != null ? aggregation.getPartNumber() : "");
        
        String groups = aggregation.getGroups() != null ? aggregation.getGroups() : "";
        if (groups.length() >= 2) {
            error.setMainGroup(groups.substring(0, 2));
            if (groups.length() >= 4) {
                error.setSubGroup(groups.substring(2, 4));
            } else {
                error.setSubGroup("");
            }
        } else {
            error.setMainGroup("");
            error.setSubGroup("");
        }
        
        error.setText1(getTextLine(aggregation, 0));
        error.setText2(getTextLine(aggregation, 1));
        error.setText3(getTextLine(aggregation, 2));
        error.setText4(getTextLine(aggregation, 3));
        
        error.setRequestedMaterial(aggregation.getValueMaterial());
        error.setRequestedLabor(aggregation.getValueLabor());
        error.setRequestedSpecial(aggregation.getValueSpecial());
        
        error.setOrderNumber("");
        error.setArea("");
        error.setDamageCode1("");
        error.setDamageCode2("");
        error.setTaxCode("");
        error.setAssessmentCode1("");
        error.setAssessmentCode2(BigDecimal.ZERO);
        error.setAssessmentDate(BigDecimal.ZERO);
        error.setCompensationMaterial(BigDecimal.ZERO);
        error.setCompensationLabor(BigDecimal.ZERO);
        error.setCompensationSpecial(BigDecimal.ZERO);
        error.setClaimType(BigDecimal.ZERO);
        error.setPreviousRepairDate(BigDecimal.ZERO);
        error.setPreviousMileage(BigDecimal.ZERO);
        error.setFieldTestNumber(BigDecimal.ZERO);
        error.setCampaignNumber("");
        error.setEps("");
        error.setStatusCode(BigDecimal.ZERO);
        error.setVariantCode(BigDecimal.ZERO);
        error.setActionCode(BigDecimal.ZERO);
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
    
    private String getTextLine(FailureAggregation aggregation, int index) {
        if (aggregation.getTextLines() != null && index < aggregation.getTextLines().size()) {
            return aggregation.getTextLines().get(index);
        }
        return "";
    }
}