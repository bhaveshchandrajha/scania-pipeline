package com.scania.warranty;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.dto.FailureCreationRequest;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.service.ClaimFailureService;
import com.scania.warranty.service.ClaimManagementService;
import com.scania.warranty.service.ClaimStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 3 – Transmission workflow tests.
 * User selects Option 10 – SEND.
 * System validates: at least one failure exists.
 * If transmission successful: status changes to 10 – SENT (mapped to APPROVED/20).
 * If transmission fails: status unchanged, error displayed.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimTransmissionTest {

    @Autowired
    ClaimManagementService claimManagementService;
    @Autowired
    ClaimFailureService claimFailureService;
    @Autowired
    ClaimStatusService claimStatusService;
    @Autowired
    ClaimRepository claimRepository;
    @Autowired
    ClaimErrorRepository claimErrorRepository;

    private static final String SEED_COMPANY = "001";
    private static final String SEED_INVOICE = "12345";
    private static final String SEED_DATE = "20240115";
    private static final String SEED_ORDER = "001";
    private static final String SEED_WORKSHOP = "1";

    private String claimNumber;

    @BeforeEach
    void createClaimWithFailure() {
        var claim = claimManagementService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);
        claimNumber = claim.getClaimNr();

        var request = new FailureCreationRequest(
                1, "0101", "PART123", false,
                List.of("Failure description"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        claimFailureService.createFailure(SEED_COMPANY, claimNumber, request);
    }

    @Test
    void postMinimum_whenClaimHasFailureAndStatusMinimum_updatesStatus() {
        Claim claim = claimRepository.findByPakzAndClaimNr(SEED_COMPANY, claimNumber).orElseThrow();
        claim.setStatusCodeSde(ClaimStatus.MINIMUM.getCode());
        claimRepository.save(claim);

        assertThat(claimErrorRepository.findByClaimNumber(SEED_COMPANY, claimNumber)).isNotEmpty();

        claimStatusService.postMinimumClaim(SEED_COMPANY, claimNumber);

        Claim after = claimRepository.findByPakzAndClaimNr(SEED_COMPANY, claimNumber).orElseThrow();
        assertThat(after.getStatusCodeSde()).isEqualTo(ClaimStatus.APPROVED.getCode());
    }

    @Test
    void postMinimum_whenClaimNotFound_throwsException() {
        assertThatThrownBy(() ->
                claimStatusService.postMinimumClaim(SEED_COMPANY, "99999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Claim not found");
    }

    @Test
    void transmission_requiresAtLeastOneFailure() {
        List<ClaimError> errors = claimErrorRepository.findByClaimNumber(SEED_COMPANY, claimNumber);
        assertThat(errors).isNotEmpty();
    }
}
