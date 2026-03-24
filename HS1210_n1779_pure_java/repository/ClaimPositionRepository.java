package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimPositionRepository extends JpaRepository<ClaimPosition, String> {

    @Query("SELECT p FROM ClaimPosition p WHERE p.abbreviation = :abbreviation AND p.claimNumber = :claimNumber ORDER BY p.errorNumber, p.sequenceNumber, p.lineNumber")
    List<ClaimPosition> findByAbbreviationAndClaimNumberOrderByKeys(
            @Param("abbreviation") String abbreviation,
            @Param("claimNumber") String claimNumber);

    @Query(value = "SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1", nativeQuery = true)
    String convertToUpperCase(@Param("text") String text);
}