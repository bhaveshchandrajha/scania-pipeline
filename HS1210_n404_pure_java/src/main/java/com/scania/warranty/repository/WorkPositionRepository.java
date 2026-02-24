package com.scania.warranty.repository;

import com.scania.warranty.domain.WorkPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkPositionRepository extends JpaRepository<WorkPosition, Long> {
    
    List<WorkPosition> findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplitt(
        String pakz, String rnr, String rdat, String anr, 
        String berei, String wt, String splitt
    );
    
    List<WorkPosition> findByPakzAndRnrAndRdatAndAnrAndBereiAndWtAndSplittOrderByPosAsc(
        String pakz, String rnr, String rdat, String anr, 
        String berei, String wt, String splitt
    );
}