package com.scania.warranty.repository;

import com.scania.warranty.domain.WarrantyRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WarrantyReleaseRepository extends JpaRepository<WarrantyRelease, Long> {
    
    @Query("SELECT w FROM WarrantyRelease w WHERE w.kzl = :kzl AND w.rNr = :rNr AND w.rDat = :rDat")
    Optional<WarrantyRelease> findByKzlAndRNrAndRDat(@Param("kzl") String kzl, @Param("rNr") String rNr, @Param("rDat") String rDat);
}