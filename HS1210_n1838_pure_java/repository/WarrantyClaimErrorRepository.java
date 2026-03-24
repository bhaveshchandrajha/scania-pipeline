package com.scania.warranty.repository;

import com.scania.warranty.domain.WarrantyClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyClaimErrorRepository extends JpaRepository<WarrantyClaimError, Long> {
    
    List<WarrantyClaimError> findByCompanyCodeAndClaimNumberAndClaimSequence(
        String companyCode, 
        String claimNumber, 
        String claimSequence
    );
    
    List<WarrantyClaimError> findByCompanyCodeAndInvoiceNumberAndInvoiceSequence(
        String companyCode, 
        String invoiceNumber, 
        String invoiceSequence
    );
}