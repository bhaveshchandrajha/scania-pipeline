package com.scania.warranty.repository;

import com.scania.warranty.domain.WarrantyRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarrantyReleaseRepository extends JpaRepository<WarrantyRelease, Long> {
    
    Optional<WarrantyRelease> findByCompanyCodeAndInvoiceNumberAndInvoiceDate(
        String companyCode, String invoiceNumber, String invoiceDate);
}