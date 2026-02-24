package com.scania.warranty.repository;

import com.scania.warranty.domain.ExternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, Long> {
    
    List<ExternalService> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
        String companyCode, 
        String invoiceNumber, 
        String invoiceDate
    );
    
    List<ExternalService> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndStatusGreaterThan(
        String companyCode, 
        String invoiceNumber, 
        String invoiceDate,
        String status
    );
}