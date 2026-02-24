package com.scania.warranty.repository;

import com.scania.warranty.domain.ReleaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReleaseRequestRepository extends JpaRepository<ReleaseRequest, Long> {
    
    Optional<ReleaseRequest> findByKzlAndRNrAndRDat(String kzl, String rNr, String rDat);
}