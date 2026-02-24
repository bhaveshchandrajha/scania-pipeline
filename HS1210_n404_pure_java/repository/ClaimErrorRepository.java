package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, Long> {
    
    List<ClaimError> findByCompanyCodeAndClaimNumber(String companyCode, String claimNumber);
    
    List<ClaimError> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndArea(
        String companyCode, String invoiceNumber, String invoiceDate, 
        String orderNumber, String area);
}