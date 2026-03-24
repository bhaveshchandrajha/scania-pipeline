package com.scania.warranty;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.repository.ClaimRepository;
import com.scania.warranty.service.ClaimCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 1 – Claim Creation workflow tests.
 * Based on Warranty Claim Processing End-to-End Workflow.
 * <p>
 * Business preconditions: Valid workorder, workorder structure match, repair date rule (≤19 days),
 * duplicate prevention. Initial status: 00 – OPEN.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimCreationWorkflowTest {

    @Autowired
    ClaimCreationService claimCreationService;

    @Autowired
    ClaimRepository claimRepository;

    private static final String SEED_COMPANY = "001";
    private static final String SEED_INVOICE = "12345";
    private static final String SEED_DATE = "20240115";
    private static final String SEED_ORDER = "001";
    private static final String SEED_WORKSHOP = "1"; // matches Invoice ahk050 (length 1)

    @Test
    void createClaim_withValidWorkorder_createsClaimWithOpenStatus() {
        String claimNumber = claimCreationService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);

        assertThat(claimNumber).isNotBlank();
        Claim claim = claimRepository.findByCompanyAndClaimNr(SEED_COMPANY, claimNumber).orElseThrow();
        assertThat(claim.getG71050()).isNotBlank();
        assertThat(claim.getG71170()).isEqualTo(ClaimStatus.PENDING.getCode());
        assertThat(claim.getG71000()).isEqualTo(SEED_COMPANY);
        assertThat(claim.getG71010()).isEqualTo(SEED_INVOICE);
    }

    @Test
    void createClaim_whenInvoiceNotFound_throwsException() {
        // Invoice lookup uses (company, order, workshop, date) - use non-existent date
        assertThatThrownBy(() ->
                claimCreationService.createClaimFromInvoice(
                        SEED_COMPANY, SEED_INVOICE, "19990101", SEED_ORDER, SEED_WORKSHOP))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void createClaim_whenClaimAlreadyExists_throwsDuplicateException() {
        claimCreationService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);

        assertThatThrownBy(() ->
                claimCreationService.createClaimFromInvoice(
                        SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Claim already exists");
    }

    @Test
    void createClaim_workorderStructure_matchesInvoiceOrderNumberAreaType() {
        String claimNumber = claimCreationService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);
        Claim claim = claimRepository.findByCompanyAndClaimNr(SEED_COMPANY, claimNumber).orElseThrow();

        assertThat(claim.getG71010()).isEqualTo(SEED_INVOICE);
        assertThat(claim.getG71020()).isEqualTo(SEED_DATE);
        assertThat(claim.getG71030()).isEqualTo(SEED_ORDER);
    }
}
