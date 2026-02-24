package com.scania.warranty.repository;

import com.scania.warranty.domain.HSG71LF2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HSG71LF2Repository extends JpaRepository<HSG71LF2, Long> {
    
    List<HSG71LF2> findByPakz(String pakz);
    
    List<HSG71LF2> findByPakzOrderByClaimnrAsc(String pakz);
    
    List<HSG71LF2> findByPakzOrderByClaimnrDesc(String pakz);
    
    Optional<HSG71LF2> findByPakzAndClaimnr(String pakz, String claimnr);
    
    @Query("SELECT c FROM HSG71LF2 c WHERE c.pakz = :pakz AND c.statuscodesde <> 99")
    List<HSG71LF2> findActiveClaimsByPakz(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM HSG71LF2 c WHERE c.pakz = :pakz AND c.statuscodesde = :status")
    List<HSG71LF2> findByPakzAndStatus(@Param("pakz") String pakz, @Param("status") Integer status);
}