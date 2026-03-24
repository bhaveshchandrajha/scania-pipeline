/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for claim errors/failures (HSG73PF).
 */
@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, Long> {
    
    @Query("SELECT e FROM ClaimError e WHERE e.companyCode = :companyCode AND e.claimNumber = :claimNumber")
    List<ClaimError> findByClaimKey(@Param("companyCode") String companyCode, 
                                    @Param("claimNumber") String claimNumber);
    
    @Query("SELECT e FROM ClaimError e WHERE e.companyCode = :companyCode AND e.invoiceNumber = :invoiceNumber AND e.invoiceDate = :invoiceDate AND e.orderNumber = :orderNumber AND e.area = :area AND e.claimNumber = :claimNumber")
    List<ClaimError> findByFullKey(@Param("companyCode") String companyCode, 
                                   @Param("invoiceNumber") String invoiceNumber, 
                                   @Param("invoiceDate") String invoiceDate, 
                                   @Param("orderNumber") String orderNumber, 
                                   @Param("area") String area, 
                                   @Param("claimNumber") String claimNumber);
}