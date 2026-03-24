/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, ClaimId> {

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 AND c.g71170 <> 99 ORDER BY c.g71050 ASC")
    List<Claim> findActiveClaimsByCompanyAsc(@Param("g71000") String g71000);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 AND c.g71170 <> 99 ORDER BY c.g71050 DESC")
    List<Claim> findActiveClaimsByCompanyDesc(@Param("g71000") String g71000);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 AND c.g71050 = :g71050")
    Optional<Claim> findByCompanyAndClaimNr(@Param("g71000") String g71000, @Param("g71050") String g71050);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 ORDER BY c.g71050 ASC")
    List<Claim> findAllByCompanyAsc(@Param("g71000") String g71000);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 ORDER BY c.g71050 DESC")
    List<Claim> findAllByCompanyDesc(@Param("g71000") String g71000);

    @Query("SELECT MAX(c.g71050) FROM Claim c WHERE c.g71000 = :g71000")
    Optional<String> findMaxClaimNrByCompany(@Param("g71000") String g71000);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 AND c.g71010 = :g71010 AND c.g71020 = :g71020 AND c.g71030 = :g71030 AND c.g71040 = :g71040")
    List<Claim> findByInvoiceKey(@Param("g71000") String g71000, @Param("g71010") String g71010,
                                  @Param("g71020") String g71020, @Param("g71030") String g71030,
                                  @Param("g71040") String g71040);

    @Query("SELECT c FROM Claim c WHERE c.g71000 = :g71000 AND c.g71010 = :g71010 AND c.g71020 = :g71020 AND c.g71030 = :g71030")
    List<Claim> findByInvoiceKeyPartial(@Param("g71000") String g71000, @Param("g71010") String g71010,
                                         @Param("g71020") String g71020, @Param("g71030") String g71030);
}