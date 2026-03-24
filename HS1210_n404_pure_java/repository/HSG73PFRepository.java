package com.scania.warranty.repository;

import com.scania.warranty.domain.HSG73PF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HSG73PFRepository extends JpaRepository<HSG73PF, Long> {
    
    List<HSG73PF> findByPakzAndRechnrAndRechdatumAndAuftragsnrAndBereichAndClaimnr(
        String pakz, String rechnr, String rechdatum, String auftragsnr, String bereich, String claimnr);
    
    List<HSG73PF> findByPakzAndClaimnr(String pakz, String claimnr);
    
    List<HSG73PF> findByPakzAndClaimnrAndStatuscode(String pakz, String claimnr, Integer statuscode);
}