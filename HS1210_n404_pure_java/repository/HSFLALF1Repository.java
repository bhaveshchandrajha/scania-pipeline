/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.HSFLALF1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for external service line items (HSFLALF1).
 */
@Repository
public interface HSFLALF1Repository extends JpaRepository<HSFLALF1, Long> {
    
    @Query("SELECT f FROM HSFLALF1 f WHERE f.pkz = :pkz AND f.besNr = :besNr AND f.besDat = :besDat AND f.aufnr = :aufnr AND f.spl = :spl AND f.pos > :pos")
    List<HSFLALF1> findByOrderKey(@Param("pkz") String pkz, 
                                   @Param("besNr") String besNr, 
                                   @Param("besDat") String besDat, 
                                   @Param("aufnr") String aufnr, 
                                   @Param("spl") String spl, 
                                   @Param("pos") Integer pos);
}