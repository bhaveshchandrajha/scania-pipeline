package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimFailureRepository extends JpaRepository<ClaimFailure, Long> {
    
    List<ClaimFailure> findByCompanyCodeAndClaimNumber(String companyCode, String claimNumber);
    
    List<ClaimFailure> findByCompanyCodeAndClaimNumberAndStatusCode(
        String companyCode, 
        String claimNumber, 
        Integer statusCode
    );
}