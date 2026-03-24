/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Labor;
import com.scania.warranty.domain.LaborId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaborRepository extends JpaRepository<Labor, LaborId> {

    @Query("SELECT l FROM Labor l WHERE l.ahw000 = :ahw000 AND l.ahw010 = :ahw010 AND l.ahw020 = :ahw020 AND l.ahw040 = :ahw040 AND l.ahw060 = :ahw060 ORDER BY l.ahw080, l.ahw100, l.ahw110")
    List<Labor> findByInvoiceKey(@Param("ahw000") String ahw000, @Param("ahw010") String ahw010,
                                  @Param("ahw020") String ahw020, @Param("ahw040") String ahw040,
                                  @Param("ahw060") String ahw060); // @rpg-trace: n1213
}