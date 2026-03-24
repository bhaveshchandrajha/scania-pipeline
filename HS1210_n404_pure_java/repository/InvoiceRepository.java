/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for invoice header (HSAHKLF3).
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    @Query("SELECT i FROM Invoice i WHERE i.companyCode = :companyCode AND i.invoiceNumber = :invoiceNumber AND i.invoiceDate = :invoiceDate AND i.orderNumber = :orderNumber AND i.workshopTheke = :workshopTheke")
    Optional<Invoice> findByInvoiceKey(@Param("companyCode") String companyCode, 
                                       @Param("invoiceNumber") String invoiceNumber, 
                                       @Param("invoiceDate") String invoiceDate, 
                                       @Param("orderNumber") String orderNumber, 
                                       @Param("workshopTheke") String workshopTheke);
    
    @Query("SELECT i FROM Invoice i WHERE i.companyCode = :companyCode AND i.orderDate = :orderDate")
    List<Invoice> findByCompanyAndOrderDate(@Param("companyCode") String companyCode, 
                                            @Param("orderDate") String orderDate);
}