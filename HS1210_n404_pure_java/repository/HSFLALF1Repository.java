package com.scania.warranty.repository;

import com.scania.warranty.domain.HSFLALF1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HSFLALF1Repository extends JpaRepository<HSFLALF1, Long> {
    
    List<HSFLALF1> findByPkzAndBesdatAndBesnrAndBereiAndWtAndSpl(
        String pkz, String besdat, String besnr, String berei, String wt, String spl);
    
    List<HSFLALF1> findByPkzAndBesdatAndBesnr(String pkz, String besdat, String besnr);
    
    List<HSFLALF1> findByStatusGreaterThan(String status);
}