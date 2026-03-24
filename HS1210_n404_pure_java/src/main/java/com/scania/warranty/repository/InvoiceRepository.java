package com.scania.warranty.repository;

import com.scania.warranty.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByPakzOrderByRnrAscRdatAscAnrAscBereiAscWtAscSplittAsc(String pakz);
    
    Optional<Invoice> findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
        String pakz, String rnr, String rdat, String anr, 
        String berei, String wt, String splitt
    );
    
    List<Invoice> findByPakzAndAdatAndAnr(String pakz, String adat, String anr);
    
    List<Invoice> findByPakzAndSplittIn(String pakz, List<String> splittValues);
}