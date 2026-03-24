package com.scania.warranty.repository;

import com.scania.warranty.domain.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    
    @Query("SELECT a FROM ApprovalRecord a WHERE a.companyCode = :companyCode AND a.invoiceKey = :invoiceKey AND a.approvalNumber IS NOT NULL AND a.approvalNumber <> '' AND a.approvalReleaseNumber = :releaseNumber")
    List<ApprovalRecord> findApprovalsByCompanyAndInvoiceKeyAndReleaseNumber(
        @Param("companyCode") String companyCode,
        @Param("invoiceKey") String invoiceKey,
        @Param("releaseNumber") String releaseNumber
    );
}