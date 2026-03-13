/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimErrorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for claim error/failure details (HSG73PF).
 */
@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, ClaimErrorId> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT e FROM ClaimError e WHERE e.pakz = :pakz AND e.claimNr = :claimNr ORDER BY e.fehlerNr")
    // @origin HS1210 L905-907 (IF)
    List<ClaimError> findByClaimNumber(@Param("pakz") String pakz, @Param("claimNr") String claimNr);

    @Query("SELECT e FROM ClaimError e WHERE e.pakz = :pakz AND e.rechNr = :rechNr AND e.rechDatum = :rechDatum AND e.auftragsNr = :auftragsNr AND e.bereich = :bereich AND e.claimNr = :claimNr ORDER BY e.fehlerNr")
    // @origin HS1210 L2560-2562 (IF)
    List<ClaimError> findByInvoiceAndClaimKey(@Param("pakz") String pakz, @Param("rechNr") String rechNr, @Param("rechDatum") String rechDatum, @Param("auftragsNr") String auftragsNr, @Param("bereich") String bereich, @Param("claimNr") String claimNr);
}