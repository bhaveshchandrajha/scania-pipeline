package com.scania.warranty;

import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.dto.FailureCreationRequest;
import com.scania.warranty.repository.ClaimErrorRepository;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.service.ClaimFailureService;
import com.scania.warranty.service.ClaimManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 2 – Failure Assignment workflow tests.
 * A claim cannot be transmitted without at least one failure.
 * Maximum of 9 failures allowed per claim.
 * Mandatory fields: Description, Failed Part, Demand Code.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimFailureAssignmentTest {

    @Autowired
    ClaimManagementService claimManagementService;
    @Autowired
    ClaimFailureService claimFailureService;
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
    void createClaim() {
        var claim = claimManagementService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);
        claimNumber = claim.getClaimNr();
    }

    @Test
    void createFailure_withMandatoryFields_succeeds() {
        var request = new FailureCreationRequest(
                1, "0101", "PART123", false,
                List.of("Failure description"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        claimFailureService.createFailure(SEED_COMPANY, claimNumber, request);

        List<ClaimError> errors = claimErrorRepository.findByClaimNumber(SEED_COMPANY, claimNumber);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getText1()).isEqualTo("Failure description");
        assertThat(errors.get(0).getFehlerTeil()).isEqualTo("PART123");
        assertThat(errors.get(0).getHauptgruppe()).isEqualTo("01");
        assertThat(errors.get(0).getNebengruppe()).isEqualTo("01");
    }

    @Test
    void createFailure_multipleFailures_upToMaxAllowed() {
        for (int i = 1; i <= 9; i++) {
            var request = new FailureCreationRequest(
                    i, "0101", "PART" + i, false,
                    List.of("Failure " + i), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            claimFailureService.createFailure(SEED_COMPANY, claimNumber, request);
        }

        List<ClaimError> errors = claimErrorRepository.findByClaimNumber(SEED_COMPANY, claimNumber);
        assertThat(errors).hasSize(9);
    }

    @Test
    void claimWithoutFailure_hasZeroErrors() {
        List<ClaimError> errors = claimErrorRepository.findByClaimNumber(SEED_COMPANY, claimNumber);
        assertThat(errors).isEmpty();
    }
}
