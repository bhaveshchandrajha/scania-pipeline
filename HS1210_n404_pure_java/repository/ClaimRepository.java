package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {
    
    Optional<Claim> findByCompanyCodeAndClaimNumber(String companyCode, String claimNumber);
    
    List<Claim> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndWorkshopCounter(
        String companyCode, String invoiceNumber, String invoiceDate, 
        String orderNumber, String workshopCounter);
    
    Optional<Claim> findFirstByCompanyCodeOrderByClaimNumberDesc(String companyCode);
    
    List<Claim> findByCompanyCodeOrderByClaimNumberAsc(String companyCode);
    
    List<Claim> findByCompanyCodeOrderByInvoiceNumberAsc(String companyCode);
}