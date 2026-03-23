/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ExtendedPartAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for HSEPAF (ExtendedPartAgreement).
 * Supports the SETLL/READE operations from CheckV4 procedure.
 */
@Repository
public interface ExtendedPartAgreementRepository extends JpaRepository<ExtendedPartAgreement, String> {

    /**
     * RPG: SetLl (AHK000:AHK040:AHK050:AHK060:'V4') HSEPAF
     *      ReadE (AHK000:AHK040:AHK050:AHK060:'V4') HSEPAF
     * Finds all ExtendedPartAgreement records matching the key with variant 'V4',
     * ordered to replicate SETLL/READE sequential access.
     */
    @Query("SELECT e FROM ExtendedPartAgreement e WHERE e.epa000 = :ahk000 " +
           "AND e.epa040 = :ahk040 AND e.epa050 = :ahk050 " +
           "AND e.epa060 = :ahk060 AND e.epaType = :variant " +
           "ORDER BY e.epa000, e.epa040, e.epa050, e.epa060, e.epaType")
    List<ExtendedPartAgreement> findByKeyAndVariant(
        @Param("ahk000") String ahk000,
        @Param("ahk040") String ahk040,
        @Param("ahk050") String ahk050,
        @Param("ahk060") String ahk060,
        @Param("variant") String variant
    ); // @rpg-trace: n1996
}