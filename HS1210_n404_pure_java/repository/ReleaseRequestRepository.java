/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ReleaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for release request (HSG70F).
 */
@Repository
public interface ReleaseRequestRepository extends JpaRepository<ReleaseRequest, Long> {
    
    @Query("SELECT r FROM ReleaseRequest r WHERE r.companyCode = :companyCode AND r.invoiceNumber = :invoiceNumber AND r.invoiceDate = :invoiceDate")
    Optional<ReleaseRequest> findByKey(@Param("companyCode") String companyCode, 
                                       @Param("invoiceNumber") String invoiceNumber, 
                                       @Param("invoiceDate") String invoiceDate);
}