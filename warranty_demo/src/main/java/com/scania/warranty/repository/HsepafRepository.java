/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Hsepaf;
import com.scania.warranty.domain.HsepafKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HsepafRepository extends JpaRepository<Hsepaf, HsepafKey> {

    @Query("SELECT h FROM Hsepaf h WHERE h.epaKey1 = :ahk000 AND h.epaKey2 = :ahk040 " +
           "AND h.epaKey3 = :ahk050 AND h.epaKey4 = :ahk060 AND h.epaKey5 = :v4Code " +
           "ORDER BY h.epaKey1, h.epaKey2, h.epaKey3, h.epaKey4, h.epaKey5")
    List<Hsepaf> findByCompositeKeyPrefix(
        @Param("ahk000") String ahk000,
        @Param("ahk040") String ahk040,
        @Param("ahk050") String ahk050,
        @Param("ahk060") String ahk060,
        @Param("v4Code") String v4Code
    );
}