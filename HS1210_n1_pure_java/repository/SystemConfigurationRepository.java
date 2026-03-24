package com.scania.warranty.repository;

import com.scania.warranty.domain.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {

    @Query("SELECT s FROM SystemConfiguration s WHERE s.key = '1'")
    Optional<SystemConfiguration> findDefaultConfiguration();

    @Query("SELECT s.maxAgeOfClaimMonths FROM SystemConfiguration s WHERE s.key = '1'")
    Optional<BigDecimal> findMaxAgeOfClaimMonths();

    @Modifying
    @Query(value = "UPDATE S3F002 SET \"SSS Claim Value\" = UPPER(:value) WHERE \"Key\" = :key", nativeQuery = true)
    void updateUpperCaseValue(@Param("key") String key, @Param("value") String value);
}