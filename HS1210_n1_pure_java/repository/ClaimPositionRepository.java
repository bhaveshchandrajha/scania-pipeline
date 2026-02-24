package com.scania.warranty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimPositionRepository extends JpaRepository<Object, Long> {

    @Query(value = "SELECT LISTAGG(DIGITS(GPS030) CONCAT DIGITS(GPS150)) " +
            "WITHIN GROUP(ORDER BY GPS150) AS List " +
            "FROM HSGPSPF " +
            "WHERE GPS000 = :dealerId AND GPS010 = :claimNo",
            nativeQuery = true)
    String findPositionListForClaim(@Param("dealerId") String dealerId,
                                     @Param("claimNo") String claimNo);

    @Modifying
    @Query(value = "UPDATE HSGPSPF SET GPS150 = :posN " +
            "WHERE GPS000 = :dealerId AND GPS010 = :claimNo AND GPS030 = :line",
            nativeQuery = true)
    void updatePositionNumber(@Param("dealerId") String dealerId,
                               @Param("claimNo") String claimNo,
                               @Param("line") Integer line,
                               @Param("posN") Integer posN);
}