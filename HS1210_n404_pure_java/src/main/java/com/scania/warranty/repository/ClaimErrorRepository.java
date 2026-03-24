package com.scania.warranty.repository;

import com.scania.warranty.domain.ClaimError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimErrorRepository extends JpaRepository<ClaimError, Long> {
    
    List<ClaimError> findByPakzAndClaimNr(String pakz, String claimNr);
    
    List<ClaimError> findByPakzAndClaimNrOrderByFehlerNrAsc(String pakz, String claimNr);
    
    List<ClaimError> findByPakzAndRechNrAndRechDatumAndAuftragsNrAndBereich(
        String pakz, String rechNr, String rechDatum, String auftragsNr, String bereich
    );
    
    void deleteByPakzAndClaimNr(String pakz, String claimNr);
}