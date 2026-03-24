package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimPositionRepository extends JpaRepository<ClaimPosition, Long> {

    @Query("SELECT cp FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNumber = :claimNumber AND cp.errorNumber = :errorNumber ORDER BY cp.lineNumber")
    List<ClaimPosition> findByClaimKey(
        @Param("kuerzel") String kuerzel,
        @Param("claimNumber") String claimNumber,
        @Param("errorNumber") String errorNumber
    );

    @Query("SELECT cp FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNumber = :claimNumber AND cp.errorNumber = :errorNumber AND cp.recordType = :recordType")
    List<ClaimPosition> findByClaimKeyAndRecordType(
        @Param("kuerzel") String kuerzel,
        @Param("claimNumber") String claimNumber,
        @Param("errorNumber") String errorNumber,
        @Param("recordType") String recordType
    );
}