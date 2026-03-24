/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1919}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class DefaultClaimErrorUpdateService implements ClaimErrorUpdateService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void clearDamageCodeCustomer(String g73000, String g73050, String g73060) {
        // Corresponds to RPG: G73360 = *Blanks; Update HSG73LR1;
        // HSG73LR1 is a logical file over HSG73PF (ClaimError entity)
        // This updates the damage code customer field to blank for the matching record
        Query query = entityManager.createQuery(
                "UPDATE ClaimError c SET c.g73360 = '' " +
                "WHERE c.g73000 = :g73000 AND c.g73050 = :g73050 AND c.g73060 = :g73060"); // @rpg-trace: n1929
        query.setParameter("g73000", g73000);
        query.setParameter("g73050", g73050);
        query.setParameter("g73060", g73060);
        query.executeUpdate(); // @rpg-trace: n1929
    }
}