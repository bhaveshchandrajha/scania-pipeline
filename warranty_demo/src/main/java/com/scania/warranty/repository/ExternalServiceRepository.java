/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ExternalService;
import com.scania.warranty.domain.ExternalServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalService, ExternalServiceId> {

    @Query("SELECT es FROM ExternalService es WHERE es.fla000 = :fla000 AND es.fla010 = :fla010 AND es.fla020 = :fla020 AND es.fla230 > '3' ORDER BY es.fla040")
    List<ExternalService> findExternalServicesForClaim(@Param("fla000") String fla000,
                                                        @Param("fla010") String fla010,
                                                        @Param("fla020") String fla020); // @rpg-trace: n1320
}