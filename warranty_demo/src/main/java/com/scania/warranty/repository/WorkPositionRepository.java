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
 * Repository for work position line items (HSAHWPF).
 */
@Repository
public interface WorkPositionRepository extends JpaRepository<WorkPosition, Long> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT w FROM WorkPosition w WHERE w.pakz = :pakz AND w.rnr = :rnr AND w.rdat = :rdat AND w.anr = :anr AND w.berei = :berei AND w.wt = :wt AND w.splitt = :splitt ORDER BY w.pos")
    // @origin HS1210 L2022-2144 (DOW)
    List<WorkPosition> findByInvoiceKey(@Param("pakz") String pakz, @Param("rnr") String rnr, @Param("rdat") String rdat, @Param("anr") String anr, @Param("berei") String berei, @Param("wt") String wt, @Param("splitt") String splitt);
}