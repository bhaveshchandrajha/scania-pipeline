/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.InvoiceHeader;
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
public interface InvoiceHeaderRepository extends JpaRepository<InvoiceHeader, Long> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT i FROM InvoiceHeader i WHERE i.pakz = :pakz AND i.rnr = :rnr AND i.rdat = :rdat AND i.anr = :anr AND i.berei = :berei AND i.wt = :wt AND i.splitt = :splitt")
    Optional<InvoiceHeader> findByKey(@Param("pakz") String pakz, @Param("rnr") String rnr, @Param("rdat") String rdat, @Param("anr") String anr, @Param("berei") String berei, @Param("wt") String wt, @Param("splitt") String splitt);

    // @origin HS1210 L1027-1027 (CHAIN)
    @Query("SELECT i FROM InvoiceHeader i WHERE i.pakz = :pakz AND i.adat = :adat AND i.anr = :anr AND i.berei = :berei AND i.wt = :wt")
    List<InvoiceHeader> findByOrderKey(@Param("pakz") String pakz, @Param("adat") String adat, @Param("anr") String anr, @Param("berei") String berei, @Param("wt") String wt);
}