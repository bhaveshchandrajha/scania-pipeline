/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.WorkPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for work positions (HSAHWPF).
 */
@Repository
public interface WorkPositionRepository extends JpaRepository<WorkPosition, Long> {
    
    @Query("SELECT w FROM WorkPosition w WHERE w.companyCode = :companyCode AND w.invoiceNumber = :invoiceNumber AND w.invoiceDate = :invoiceDate AND w.orderNumber = :orderNumber AND w.workshopTheke = :workshopTheke")
    List<WorkPosition> findByInvoiceKey(@Param("companyCode") String companyCode, 
                                        @Param("invoiceNumber") String invoiceNumber, 
                                        @Param("invoiceDate") String invoiceDate, 
                                        @Param("orderNumber") String orderNumber, 
                                        @Param("workshopTheke") String workshopTheke);
}