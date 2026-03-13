/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT i FROM Invoice i WHERE i.pakz = :pakz AND i.rnr = :rnr AND i.rdat = :rdat AND i.anr = :anr AND i.berei = :berei AND i.wt = :wt AND i.splitt = :splitt")
    // @origin HS1210 L1753-1757 (IF)
    Optional<Invoice> findByInvoiceKey(@Param("pakz") String pakz, @Param("rnr") String rnr, @Param("rdat") String rdat, @Param("anr") String anr, @Param("berei") String berei, @Param("wt") String wt, @Param("splitt") String splitt);

    // @origin HS1210 L1027-1027 (CHAIN)
    @Query("SELECT i FROM Invoice i WHERE i.pakz = :pakz AND i.adat = :adat AND i.anr = :anr AND i.berei = :berei AND i.wt = :wt AND i.splitt = :splitt ORDER BY i.rnr")
    // @origin HS1210 L1435-1440 (DOW)
    List<Invoice> findByOrderDate(@Param("pakz") String pakz, @Param("adat") String adat, @Param("anr") String anr, @Param("berei") String berei, @Param("wt") String wt, @Param("splitt") String splitt);

    // @origin HS1210 L1035-1035 (CHAIN)
    @Query("SELECT i FROM Invoice i WHERE i.pakz = :pakz AND i.splitt = '04' ORDER BY i.rnr, i.rdat")
    // @origin HS1210 L1356-1356 (ELSE)
    List<Invoice> findWarrantyInvoicesByCompany(@Param("pakz") String pakz);

    @Modifying
    @Query("DELETE FROM Invoice i WHERE i.pakz = :pakz AND i.rnr = :rnr AND i.rdat = :rdat")
    void deleteByPakzAndRnrAndRdat(@Param("pakz") String pakz, @Param("rnr") String rnr, @Param("rdat") String rdat);
}