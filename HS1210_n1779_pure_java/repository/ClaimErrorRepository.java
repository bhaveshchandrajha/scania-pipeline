package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, String> {
}