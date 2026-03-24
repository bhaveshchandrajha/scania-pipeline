package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, Long> {

    List<ClaimError> findByClaimNumber(String claimNumber);

    List<ClaimError> findByClaimNumberAndErrorNumber(String claimNumber, String errorNumber);

    Optional<ClaimError> findByClaimNumberAndErrorNumberAndReconNumber(
        String claimNumber, String errorNumber, String reconNumber);

    @Query("SELECT e FROM ClaimError e WHERE e.claimNumber = :claimNumber " +
           "AND e.status = :status")
    List<ClaimError> findByClaimNumberAndStatus(
        @Param("claimNumber") String claimNumber,
        @Param("status") Integer status);

    @Query("SELECT e FROM ClaimError e WHERE e.claimNumber = :claimNumber " +
           "AND (:mainGroup = '' OR e.mainGroup = :mainGroup) " +
           "AND (:damageCode = '' OR e.damageCode = :damageCode) " +
           "AND (:controlCode = '' OR e.controlCode = :controlCode)")
    List<ClaimError> searchClaimErrors(
        @Param("claimNumber") String claimNumber,
        @Param("mainGroup") String mainGroup,
        @Param("damageCode") String damageCode,
        @Param("controlCode") String controlCode);

    @Query("SELECT MAX(e.errorNumber) FROM ClaimError e WHERE e.claimNumber = :claimNumber")
    Optional<String> findMaxErrorNumber(@Param("claimNumber") String claimNumber);

    @Query("SELECT e FROM ClaimError e WHERE e.claimNumber = :claimNumber " +
           "AND e.campaignNumber IS NOT NULL AND e.campaignNumber <> ''")
    List<ClaimError> findCampaignErrors(@Param("claimNumber") String claimNumber);

    void deleteByClaimNumberAndErrorNumber(String claimNumber, String errorNumber);
}