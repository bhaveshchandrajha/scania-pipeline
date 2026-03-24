/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.InvoiceHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for HSAHKPF (InvoiceHeader).
 * Supports the CHAIN operation from CheckV4 procedure.
 */
@Repository
public interface InvoiceHeaderRepository extends JpaRepository<InvoiceHeader, String> {

    /**
     * RPG: Chain (G71000:G71010:G71020:' ':G71030:G71040:G71190:%Subst(G71200:8:2)) HSAHKPF
     * Finds an InvoiceHeader by composite key fields.
     */
    @Query("SELECT h FROM InvoiceHeader h WHERE h.ahk000 = :key0 AND h.ahk010 = :key1 " +
           "AND h.ahk020 = :key2 AND h.ahk025 = :blank " +
           "AND h.ahk030 = :key3 AND h.ahk040 = :key4 " +
           "AND h.ahk190 = :key5 AND h.ahk200 = :variantCode")
    Optional<InvoiceHeader> findByCompositeKey(
        @Param("key0") String key0,
        @Param("key1") String key1,
        @Param("key2") String key2,
        @Param("blank") String blank,
        @Param("key3") String key3,
        @Param("key4") String key4,
        @Param("key5") String key5,
        @Param("variantCode") String variantCode
    ); // @rpg-trace: n1991
}