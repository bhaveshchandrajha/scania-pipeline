/**
 * Spring Data JPA repository for warranty claim data access.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.repository;

import com.scania.warranty.domain.StandardMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StandardMaterialRepository extends JpaRepository<StandardMaterial, String> {
    
    Optional<StandardMaterial> findByPartNumber(String partNumber); // @rpg-trace: n1810
}