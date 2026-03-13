/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.WarrantyRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for warranty release requests (HSG70F).
 */
@Repository
public interface WarrantyReleaseRepository extends JpaRepository<WarrantyRelease, Long> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT w FROM WarrantyRelease w WHERE w.kzl = :kzl AND w.rnr = :rnr AND w.rdat = :rdat")
    // @origin HS1210 L1687-1693 (IF)
    Optional<WarrantyRelease> findByInvoiceKey(@Param("kzl") String kzl, @Param("rnr") String rnr, @Param("rdat") String rdat);

    @Query("SELECT w FROM WarrantyRelease w WHERE w.kzl = :kzl AND w.rnr = :rnr AND w.rdat = :rdat")
    Optional<WarrantyRelease> findByKey(@Param("kzl") String kzl, @Param("rnr") String rnr, @Param("rdat") String rdat);
}