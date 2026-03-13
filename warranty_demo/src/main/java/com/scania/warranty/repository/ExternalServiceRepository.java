/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ExternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for external service line items (HSFLALF1).
 */
@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, Long> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT e FROM ExternalService e WHERE e.pkz = :pkz AND e.besDat = :besDat AND e.besNr = :besNr AND e.aufnr = :aufnr AND e.berei = :berei AND e.wt = :wt AND e.spl = :spl AND e.aufdat = :aufdat ORDER BY e.lnrFl")
    // @origin HS1210 L2148-2175 (DOW)
    List<ExternalService> findByInvoiceKey(@Param("pkz") String pkz, @Param("besDat") String besDat, @Param("besNr") String besNr, @Param("aufnr") String aufnr, @Param("berei") String berei, @Param("wt") String wt, @Param("spl") String spl, @Param("aufdat") String aufdat);
}