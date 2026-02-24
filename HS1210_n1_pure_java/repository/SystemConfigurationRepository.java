package com.scania.warranty.repository;

import com.scania.warranty.domain.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {

    @Query("SELECT s FROM SystemConfiguration s WHERE s.key = '1'")
    Optional<SystemConfiguration> findDefaultConfiguration();

    @Query(value = "SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1", nativeQuery = true)
    String toUpperCase(String text);

    @Query(value = "SELECT wkt_sid FROM hswktf WHERE wkt_id = :id", nativeQuery = true)
    Long findWorkTicketSidById(Long id);

    @Query(value = "SELECT LISTAGG(DIGITS(GPS030) CONCAT DIGITS(GPS150)) WITHIN GROUP(ORDER BY GPS150) " +
            "FROM HSGPSPF WHERE GPS000 = :dealerId AND GPS010 = :claimNo", nativeQuery = true)
    String findAggregatedPositionsByDealerAndClaim(String dealerId, String claimNo);
}