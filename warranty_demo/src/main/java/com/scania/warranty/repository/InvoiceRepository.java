package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
        String companyCode, 
        String invoiceNumber, 
        String invoiceDate
    );
    
    List<Invoice> findByCompanyCodeAndSplit(String companyCode, String split);
}