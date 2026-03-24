package com.scania.warranty.repository;

import com.scania.warranty.domain.CostPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostPercentageRepository extends JpaRepository<CostPercentage, String> {
    
    Optional<CostPercentage> findByControlCode(String controlCode);
}