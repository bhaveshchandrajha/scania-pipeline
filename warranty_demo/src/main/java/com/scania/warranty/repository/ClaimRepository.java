/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.Claim;
import com.scania.warranty.domain.ClaimId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, ClaimId> {
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz AND c.rechNr = :rechNr AND c.rechDatum = :rechDatum AND c.auftragsNr = :auftragsNr AND c.bereich = :bereich")
    Optional<Claim> findByKey(@Param("pakz") String pakz, 
                               @Param("rechNr") String rechNr, 
                               @Param("rechDatum") String rechDatum, 
                               @Param("auftragsNr") String auftragsNr, 
                               @Param("bereich") String bereich); // @rpg-trace: n1824
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz AND c.claimNr = :claimNr")
    Optional<Claim> findByPakzAndClaimNr(@Param("pakz") String pakz, 
                                          @Param("claimNr") String claimNr);
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz ORDER BY c.claimNr ASC")
    List<Claim> findByPakzOrderByClaimNrAsc(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz ORDER BY c.claimNr DESC")
    List<Claim> findByPakzOrderByClaimNrDesc(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz ORDER BY c.claimNr ASC")
    List<Claim> findAllByPakzOrderByClaimNrAsc(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz ORDER BY c.claimNr DESC")
    List<Claim> findByPakzOrderByClaimNrDescAll(@Param("pakz") String pakz);
    
    @Query("SELECT c FROM Claim c WHERE c.pakz = :pakz AND c.rechNr = :rechNr AND c.rechDatum = :rechDatum AND c.auftragsNr = :auftragsNr")
    Optional<Claim> findByInvoiceKey(@Param("pakz") String pakz, 
                                      @Param("rechNr") String rechNr, 
                                      @Param("rechDatum") String rechDatum, 
                                      @Param("auftragsNr") String auftragsNr);
}