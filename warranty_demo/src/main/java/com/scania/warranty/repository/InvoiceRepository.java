/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import com.scania.warranty.domain.InvoiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, InvoiceId> {

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 AND i.ahk080 = :ahk080 ORDER BY i.ahk010")
    List<Invoice> findByCompanyAndOrderDate(@Param("ahk000") String ahk000, @Param("ahk080") String ahk080); // @rpg-trace: n794

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 AND i.ahk010 = :ahk010 AND i.ahk020 = :ahk020 AND i.ahk040 = :ahk040 AND i.ahk060 = :ahk060")
    Optional<Invoice> findByKeyAhk(@Param("ahk000") String ahk000, @Param("ahk010") String ahk010,
                                    @Param("ahk020") String ahk020, @Param("ahk040") String ahk040,
                                    @Param("ahk060") String ahk060); // @rpg-trace: n918

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 AND i.ahk030 = 'S' AND i.ahk010 = :ahk010 AND i.ahk080 = :ahk080")
    List<Invoice> findStornoInvoices(@Param("ahk000") String ahk000, @Param("ahk010") String ahk010,
                                      @Param("ahk080") String ahk080); // @rpg-trace: n975

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 AND i.ahk010 = :ahk010 AND i.ahk020 = :ahk020 AND i.ahk040 = :ahk040")
    Optional<Invoice> findByKey(@Param("ahk000") String ahk000, @Param("ahk010") String ahk010,
                                 @Param("ahk020") String ahk020, @Param("ahk040") String ahk040);

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 ORDER BY i.ahk010")
    List<Invoice> findByCompany(@Param("ahk000") String ahk000);

    @Query("SELECT i FROM Invoice i WHERE i.ahk000 = :ahk000 AND i.ahk030 = 'S' AND i.ahk080 = :ahk080 AND i.ahk010 = :ahk010")
    List<Invoice> findStornoByKey(@Param("ahk000") String ahk000, @Param("ahk080") String ahk080,
                                   @Param("ahk010") String ahk010);
}