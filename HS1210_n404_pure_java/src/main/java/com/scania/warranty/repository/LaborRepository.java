package com.scania.warranty.repository;

import com.scania.warranty.domain.Labor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborRepository extends JpaRepository<Labor, Long> {
    
    List<Labor> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
        String companyCode, 
        String invoiceNumber, 
        String invoiceDate
    );
}