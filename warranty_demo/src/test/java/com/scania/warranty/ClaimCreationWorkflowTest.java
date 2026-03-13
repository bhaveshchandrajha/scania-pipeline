package com.scania.warranty;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimStatus;
import com.scania.warranty.service.ClaimManagementService;
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
    ClaimManagementService claimManagementService;

    private static final String SEED_COMPANY = "001";
    private static final String SEED_INVOICE = "12345";
    private static final String SEED_DATE = "20240115";
    private static final String SEED_ORDER = "001";
    private static final String SEED_WORKSHOP = "1"; // matches Invoice.wt (length 1)

    @Test
    void createClaim_withValidWorkorder_createsClaimWithOpenStatus() {
        Claim claim = claimManagementService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);

        assertThat(claim).isNotNull();
        assertThat(claim.getClaimNr()).isNotBlank();
        assertThat(claim.getStatusCodeSde()).isEqualTo(ClaimStatus.PENDING.getCode());
        assertThat(claim.getPakz()).isEqualTo(SEED_COMPANY);
        assertThat(claim.getRechNr()).isEqualTo(SEED_INVOICE);
    }

    @Test
    void createClaim_whenInvoiceNotFound_throwsException() {
        assertThatThrownBy(() ->
                claimManagementService.createClaimFromInvoice(
                        SEED_COMPANY, "55555", SEED_DATE, SEED_ORDER, SEED_WORKSHOP))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invoice not found");
    }

    @Test
    void createClaim_whenClaimAlreadyExists_throwsDuplicateException() {
        claimManagementService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);

        assertThatThrownBy(() ->
                claimManagementService.createClaimFromInvoice(
                        SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Claim already exists");
    }

    @Test
    void createClaim_workorderStructure_matchesInvoiceOrderNumberAreaType() {
        Claim claim = claimManagementService.createClaimFromInvoice(
                SEED_COMPANY, SEED_INVOICE, SEED_DATE, SEED_ORDER, SEED_WORKSHOP);

        assertThat(claim.getRechNr()).isEqualTo(SEED_INVOICE);
        assertThat(claim.getRechDatum()).isEqualTo(SEED_DATE);
        assertThat(claim.getAuftragsNr()).isEqualTo(SEED_ORDER);
    }
}
