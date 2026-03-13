/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.GpsLine;
import com.scania.warranty.domain.GpsLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsLineRepository extends JpaRepository<GpsLine, GpsLineId> {
    
    @Query("SELECT g FROM GpsLine g WHERE g.kuerzel = :kuerzel AND g.claimNr = :claimNr ORDER BY g.fehlerNr, g.folgeNr")
    List<GpsLine> findByKuerzelAndClaimNrOrderByFehlerNrAndFolgeNr(
        @Param("kuerzel") String kuerzel,
        @Param("claimNr") String claimNr
    ); // @rpg-trace: n1784, n1785
}