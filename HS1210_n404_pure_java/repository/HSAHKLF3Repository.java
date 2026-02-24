package com.scania.warranty.repository;

import com.scania.warranty.domain.HSAHKLF3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HSAHKLF3Repository extends JpaRepository<HSAHKLF3, Long> {
    
    List<HSAHKLF3> findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
        String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt);
    
    List<HSAHKLF3> findByPakz(String pakz);
    
    List<HSAHKLF3> findByPakzAndAdatAndAnr(String pakz, String adat, String anr);
}