package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    List<Claim> findByCompanyCodeOrderByClaimNumberAsc(String companyCode);
    
    List<Claim> findByCompanyCodeOrderByClaimNumberDesc(String companyCode);
    
    Optional<Claim> findByCompanyCodeAndClaimNumber(String companyCode, String claimNumber);
    
    List<Claim> findByCompanyCodeAndStatusCodeSde(String companyCode, Integer statusCode);
    
    @Query("SELECT c FROM Claim c WHERE c.companyCode = :companyCode " +
           "AND (:statusCode IS NULL OR c.statusCodeSde = :statusCode) " +
           "AND (:vehicleNumber IS NULL OR c.chassisNumber LIKE %:vehicleNumber%) " +
           "AND (:customerNumber IS NULL OR c.customerNumber = :customerNumber)")
    List<Claim> searchClaims(
        @Param("companyCode") String companyCode,
        @Param("statusCode") Integer statusCode,
        @Param("vehicleNumber") String vehicleNumber,
        @Param("customerNumber") String customerNumber
    );
}