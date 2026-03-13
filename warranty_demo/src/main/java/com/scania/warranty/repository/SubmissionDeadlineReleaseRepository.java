/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.SubmissionDeadlineRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for submission deadline releases (HSG70F).
 */
@Repository
public interface SubmissionDeadlineReleaseRepository extends JpaRepository<SubmissionDeadlineRelease, Long> {

    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT s FROM SubmissionDeadlineRelease s WHERE s.kzl = :kzl AND s.rnr = :rnr AND s.rdat = :rdat")
    Optional<SubmissionDeadlineRelease> findByKzlAndRnrAndRdat(@Param("kzl") String kzl, @Param("rnr") String rnr, @Param("rdat") String rdat);
}