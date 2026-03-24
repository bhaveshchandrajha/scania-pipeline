/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for claim header (HSG71LF2).
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode AND c.statusCodeSde <> 99 ORDER BY c.claimNumber ASC")
    List<Claim> findActiveClaimsByCompanyCodeAscending(@Param("companyCode") String companyCode);
    
    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode AND c.statusCodeSde <> 99 ORDER BY c.claimNumber DESC")
    List<Claim> findActiveClaimsByCompanyCodeDescending(@Param("companyCode") String companyCode);
    
    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode AND c.invoiceNumber = :invoiceNumber AND c.invoiceDate = :invoiceDate AND c.orderNumber = :orderNumber AND c.workshopTheke = :workshopTheke")
    List<Claim> findByInvoiceKey(@Param("companyCode") String companyCode, 
                                  @Param("invoiceNumber") String invoiceNumber, 
                                  @Param("invoiceDate") String invoiceDate, 
                                  @Param("orderNumber") String orderNumber, 
                                  @Param("workshopTheke") String workshopTheke);
    
    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode AND c.claimNumber = :claimNumber")
    Optional<Claim> findByCompanyAndClaimNumber(@Param("companyCode") String companyCode, 
                                                 @Param("claimNumber") String claimNumber);
}