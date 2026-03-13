/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ITLSMF3")
public class StandardMaterial {
    
    @Id
    @Column(name = "GPS050", length = 18, nullable = false)
    private String partNumber; // @rpg-trace: schema
    
    @Column(name = "SFRAN", length = 2, nullable = false)
    private String category; // @rpg-trace: schema

    public StandardMaterial() {
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}