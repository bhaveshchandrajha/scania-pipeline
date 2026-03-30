package com.scania.warranty.repository;

import com.scania.warranty.domain.FailedClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedClaimRepository extends JpaRepository<FailedClaim, Long> {

    List<FailedClaim> findByCompanyCodeOrderByFailedAtDesc(String companyCode);
}
