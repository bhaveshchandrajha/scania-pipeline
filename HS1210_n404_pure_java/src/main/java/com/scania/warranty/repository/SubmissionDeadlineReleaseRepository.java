package com.scania.warranty.repository;

import com.scania.warranty.domain.SubmissionDeadlineRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubmissionDeadlineReleaseRepository extends JpaRepository<SubmissionDeadlineRelease, Long> {
    
    Optional<SubmissionDeadlineRelease> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
        String companyCode, 
        String invoiceNumber, 
        String invoiceDate
    );
}