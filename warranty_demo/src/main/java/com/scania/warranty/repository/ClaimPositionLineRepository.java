/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimPositionLine;
import com.scania.warranty.domain.ClaimPositionLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimPositionLineRepository extends JpaRepository<ClaimPositionLine, ClaimPositionLineId> {
    
    @Query("SELECT p FROM ClaimPositionLine p WHERE p.abbreviation = :abbreviation AND p.claimNumber = :claimNumber ORDER BY p.failureNumber, p.sequenceNumber, p.lineNumber, p.distribution")
    List<ClaimPositionLine> findByAbbreviationAndClaimNumberOrderByKeys(
        @Param("abbreviation") String abbreviation,
        @Param("claimNumber") String claimNumber
    ); // @rpg-trace: n1784, n1785
    
    @Query(value = "SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1", nativeQuery = true)
    String toUpperCase(@Param("text") String text); // @rpg-trace: SQL statement 1
}