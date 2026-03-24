/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimReleaseRequest;
import com.scania.warranty.domain.ClaimReleaseRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimReleaseRequestRepository extends JpaRepository<ClaimReleaseRequest, ClaimReleaseRequestId> {

    Optional<ClaimReleaseRequest> findByG70KzlAndG70RnrAndG70Rdat(String g70Kzl, String g70Rnr, String g70Rdat); // @rpg-trace: n938
}