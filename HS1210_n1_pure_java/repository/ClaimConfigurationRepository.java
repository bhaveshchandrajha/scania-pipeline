package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ClaimConfigurationRepository extends JpaRepository<ClaimConfiguration, String> {
    
    Optional<ClaimConfiguration> findFirstBy();
    
    @Query("SELECT c.maxAgeOfClaimMonths FROM ClaimConfiguration c WHERE c.key = '1'")
    Optional<BigDecimal> findMaxAgeOfClaimMonths();
}