package com.scania.warranty.repository;

import com.scania.warranty.domain.WorkPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkPositionRepository extends JpaRepository<WorkPosition, Long> {
    
    List<WorkPosition> findByCompanyCodeAndInvoiceNumberAndInvoiceDateAndOrderNumberAndAreaAndWorkshopCounterAndSplit(
        String companyCode, String invoiceNumber, String invoiceDate, String orderNumber, 
        String area, String workshopCounter, String split);
}