package com.scania.warranty.service;

import com.scania.warranty.domain.FailedClaim;
import com.scania.warranty.dto.FailedClaimDto;
import com.scania.warranty.repository.FailedClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FailedClaimService {

    private final FailedClaimRepository failedClaimRepository;

    public FailedClaimService(FailedClaimRepository failedClaimRepository) {
        this.failedClaimRepository = failedClaimRepository;
    }

    @Transactional
    public void recordFailure(String companyCode, String invoiceNr, String invoiceDate, String workshopCode,
                              String failureReason, Integer repairAgeDays) {
        FailedClaim row = new FailedClaim(
            ClaimLookupSupport.normalizeCompanyCode(companyCode),
            invoiceNr != null ? invoiceNr : "",
            invoiceDate != null ? invoiceDate : "",
            workshopCode != null ? workshopCode : "",
            failureReason,
            repairAgeDays,
            Instant.now()
        );
        failedClaimRepository.save(row);
    }

    @Transactional(readOnly = true)
    public List<FailedClaimDto> listByCompany(String companyCode) {
        String cc = ClaimLookupSupport.normalizeCompanyCode(companyCode);
        return failedClaimRepository.findByCompanyCodeOrderByFailedAtDesc(cc).stream()
            .map(f -> new FailedClaimDto(
                f.getId(),
                f.getCompanyCode(),
                f.getInvoiceNr(),
                f.getInvoiceDate(),
                f.getWorkshopCode(),
                f.getFailureReason(),
                f.getRepairAgeDays(),
                f.getFailedAt()
            ))
            .collect(Collectors.toList());
    }
}
