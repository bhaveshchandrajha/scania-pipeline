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
    
    List<Claim> findByPakzOrderByClaimNrAsc(String pakz);
    
    List<Claim> findByPakzOrderByClaimNrDesc(String pakz);
    
    List<Claim> findByPakzOrderByRechNrAsc(String pakz);
    
    List<Claim> findByPakzOrderByRechNrDesc(String pakz);
    
    Optional<Claim> findByPakzAndClaimNr(String pakz, String claimNr);
    
    Optional<Claim> findByPakzAndRechNrAndRechDatumAndAuftragsNrAndWete(
        String pakz, String rechNr, String rechDatum, String auftragsNr, String wete
    );
    
    List<Claim> findByPakzAndRechNrAndRechDatum(String pakz, String rechNr, String rechDatum);
    
    // @origin HS1210 L941-941 (CHAIN)
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz AND c.statusCodeSde <> 99")
    List<Claim> findActiveClaimsByPakz(@Param("pakz") String pakz);
    
    @Query("SELECT MAX(c.claimNr) FROM Claim c WHERE c.pakz = :pakz")
    Optional<String> findMaxClaimNrByPakz(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM Claim c ORDER BY c.claimNr ASC")
    List<Claim> findAllOrderByClaimNrAsc();
    
    @Query("SELECT c FROM Claim c ORDER BY c.claimNr DESC")
    List<Claim> findAllOrderByClaimNrDesc();
}