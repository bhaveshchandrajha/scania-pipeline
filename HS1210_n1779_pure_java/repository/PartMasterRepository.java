package com.scania.warranty.repository;

import com.scania.warranty.domain.PartMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartMasterRepository extends JpaRepository<PartMaster, String> {
    
    Optional<PartMaster> findByPartNumber(String partNumber);
}