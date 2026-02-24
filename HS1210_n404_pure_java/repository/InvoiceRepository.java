package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndWorkshopCounterAndSplit(
        String companyCode, String invoiceNumber, String invoiceDate, 
        String orderNumber, String workshopCounter, String split);
    
    List<Invoice> findByCompanyCodeAndOrderDateAndOrderNumber(
        String companyCode, String orderDate, String orderNumber);
    
    List<Invoice> findByCompanyCodeAndSplitOrderByInvoiceDateDesc(
        String companyCode, String split);
}