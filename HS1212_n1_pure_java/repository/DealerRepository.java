package com.scania.warranty.repository;

import com.scania.warranty.domain.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, String> {

    Optional<Dealer> findByDistWarrantyCustNo(String distWarrantyCustNo);

    Optional<Dealer> findByGaCustNumber(String gaCustNumber);
}