/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import com.scania.warranty.domain.ClaimErrorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, ClaimErrorId> {

    @Query("SELECT ce FROM ClaimError ce WHERE ce.g73000 = :g73000 AND ce.g73050 = :g73050")
    List<ClaimError> findByCompanyAndClaimNr(@Param("g73000") String g73000, @Param("g73050") String g73050); // @rpg-trace: n456

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ClaimError ce WHERE ce.g73000 = :g73000 AND ce.g73050 = :g73050")
    int deleteByCompanyAndClaimNr(@Param("g73000") String g73000, @Param("g73050") String g73050); // @rpg-trace: n593

    @Query("SELECT ce FROM ClaimError ce WHERE ce.g73000 = :g73000 AND ce.g73050 = :g73050 ORDER BY ce.g73060 ASC")
    List<ClaimError> findByClaimKey(@Param("g73000") String g73000, @Param("g73050") String g73050); // @rpg-trace: n1732
}