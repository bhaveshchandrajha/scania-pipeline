/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.ItemMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, String> { // @rpg-trace: n1810
    
    @Query(value = "SELECT SFRAN FROM ITLSMF3 WHERE SFNR = :partNumber", nativeQuery = true) // @rpg-trace: n1810
    String findSupplierRangeByPartNumber(@Param("partNumber") String partNumber);
}