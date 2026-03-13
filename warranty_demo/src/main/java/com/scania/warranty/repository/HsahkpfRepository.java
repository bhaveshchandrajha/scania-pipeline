/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Hsahkpf;
import com.scania.warranty.domain.HsahkpfKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HsahkpfRepository extends JpaRepository<Hsahkpf, HsahkpfKey> {

    @Query("SELECT h FROM Hsahkpf h WHERE h.ahk000 = :g71000 AND h.ahk010 = :g71010 " +
           "AND h.ahk020 = :g71020 AND h.ahk030 = :spacer AND h.ahk040 = :g71030 " +
           "AND h.ahk050 = :g71040 AND h.ahk060 = :g71190 AND h.ahk070 = :v4Code")
    Optional<Hsahkpf> findByCompositeKey(
        @Param("g71000") String g71000,
        @Param("g71010") String g71010,
        @Param("g71020") String g71020,
        @Param("spacer") String spacer,
        @Param("g71030") String g71030,
        @Param("g71040") String g71040,
        @Param("g71190") String g71190,
        @Param("v4Code") String v4Code
    );
}