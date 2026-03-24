package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimFailureRepository extends JpaRepository<ClaimFailure, String> {
}