package com.scania.warranty.repository;

import com.scania.warranty.domain.FISTAM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FISTAMRepository extends JpaRepository<FISTAM, String> {

    Optional<FISTAM> findFirstByOrderByDealerNumberAsc();
}