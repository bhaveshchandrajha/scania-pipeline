/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Hsgpspf;
import com.scania.warranty.domain.HsgpspfId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HsgpspfRepository extends JpaRepository<Hsgpspf, HsgpspfId> {

    // Translates RPG: SETLL/READE (G73000:G73050:G73060) HSGPSPF
    // Reads all records matching the partial key (GPS000, GPS010, GPS020)
    @Query("SELECT h FROM Hsgpspf h WHERE h.gps000 = :gps000 AND h.gps010 = :gps010 AND h.gps020 = :gps020")
    List<Hsgpspf> findByGps000AndGps010AndGps020(
            @Param("gps000") String gps000,
            @Param("gps010") String gps010,
            @Param("gps020") String gps020
    ); // @rpg-trace: n1958
}