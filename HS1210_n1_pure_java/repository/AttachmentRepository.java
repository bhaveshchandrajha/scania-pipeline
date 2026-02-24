package com.scania.warranty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AttachmentRepository extends JpaRepository<Object, Long> {

    @Query(value = "SELECT wkt_sid FROM hswktf WHERE wkt_id = :id", nativeQuery = true)
    BigDecimal findSidByWorkTicketId(@Param("id") BigDecimal id);

    @Modifying
    @Query(value = "INSERT INTO HSG75F (G75_KZL, G75_C00001, G75_FAILNO, G75_FILE, G75_F00001) " +
            "SELECT :dealerId, :claimNo, :failNo, ATT_BLOB, ATT_NAME FROM cdattf WHERE att_dmg_id = :sid",
            nativeQuery = true)
    void insertAttachmentsForClaim(@Param("dealerId") String dealerId,
                                    @Param("claimNo") String claimNo,
                                    @Param("failNo") String failNo,
                                    @Param("sid") BigDecimal sid);
}