/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimPosition;
import com.scania.warranty.domain.ClaimPositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ClaimPositionRepository extends JpaRepository<ClaimPosition, ClaimPositionId> {
    
    @Query("SELECT cp FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNr = :claimNr ORDER BY cp.fehlerNr, cp.folgeNr, cp.zeile, cp.aufteilung")
    List<ClaimPosition> findByKuerzelAndClaimNrOrderByKeys(@Param("kuerzel") String kuerzel, 
                                                             @Param("claimNr") String claimNr); // @rpg-trace: n1784
    
    @Query("SELECT SUM(cp.wert) FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNr = :claimNr AND cp.fehlerNr = :fehlerNr AND cp.folgeNr = :folgeNr")
    BigDecimal calculateTotalValueByClaimKey(@Param("kuerzel") String kuerzel, 
                                              @Param("claimNr") String claimNr, 
                                              @Param("fehlerNr") String fehlerNr, 
                                              @Param("folgeNr") String folgeNr);
    
    @Query("SELECT cp FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNr = :claimNr AND cp.fehlerNr = :fehlerNr")
    List<ClaimPosition> findByCompanyCodeAndClaimNumberAndErrorNumber(@Param("kuerzel") String kuerzel, 
                                                                        @Param("claimNr") String claimNr, 
                                                                        @Param("fehlerNr") String fehlerNr);
    
    @Query("SELECT CONCAT(LPAD(CAST(cp.zeile AS string), 3, '0'), LPAD(CAST(cp.posNr AS string), 3, '0')) FROM ClaimPosition cp WHERE cp.kuerzel = :kuerzel AND cp.claimNr = :claimNr ORDER BY cp.posNr")
    String findAggregatedPositionData(@Param("kuerzel") String kuerzel, 
                                       @Param("claimNr") String claimNr);
    
    @Modifying
    @Query("UPDATE ClaimPosition cp SET cp.posNr = :newPosition WHERE cp.kuerzel = :kuerzel AND cp.claimNr = :claimNr AND cp.zeile = :lineNo")
    void updatePosition(@Param("kuerzel") String kuerzel, 
                        @Param("claimNr") String claimNr, 
                        @Param("lineNo") Integer lineNo, 
                        @Param("newPosition") int newPosition);
}