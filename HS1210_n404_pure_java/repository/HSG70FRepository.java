package com.scania.warranty.repository;

import com.scania.warranty.domain.HSG70F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HSG70FRepository extends JpaRepository<HSG70F, Long> {
    
    Optional<HSG70F> findByKzlAndRnrAndRdat(String kzl, String rnr, String rdat);
}