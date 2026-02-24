package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimConfigurationRepository extends JpaRepository<ClaimConfiguration, String> {

    @Query("SELECT c FROM ClaimConfiguration c WHERE c.key = '1'")
    Optional<ClaimConfiguration> findDefaultConfiguration();
}