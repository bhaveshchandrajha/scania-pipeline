package com.scania.warranty.repository;

import com.scania.warranty.domain.HSAHWPF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HSAHWPFRepository extends JpaRepository<HSAHWPF, Long> {
    
    List<HSAHWPF> findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
        String pakz, String rnr, String rdat, String anr, String berei, String wt, String splitt);
    
    List<HSAHWPF> findByPakzAndRnrAndRdat(String pakz, String rnr, String rdat);
}