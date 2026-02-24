package com.scania.warranty.repository;

import com.scania.warranty.domain.ExternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, Long> {
    
    List<ExternalService> findByCompanyCodeAndOrderDateAndOrderNumberAndAreaAndWorkshopCounterAndSplitAndJobDate(
        String companyCode, String orderDate, String orderNumber, String area, 
        String workshopCounter, String split, String jobDate);
    
    List<ExternalService> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndJobNumberAndAreaAndWorkshopCounterAndSplit(
        String companyCode, String invoiceNumber, String invoiceDate, String jobNumber, 
        String area, String workshopCounter, String split);
}