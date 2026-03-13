/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ITLSMF3")
public class ItemMaster { // @rpg-trace: n1810
    
    @Id
    @Column(name = "ITEM_NUMBER", length = 18, nullable = false)
    private String itemNumber; // @rpg-trace: n1810
    
    @Column(name = "SFRAN", length = 2, nullable = false)
    private String supplierRange; // @rpg-trace: n1812

    public ItemMaster() { // @rpg-trace: n1810
    }

    public String getItemNumber() { // @rpg-trace: n1810
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) { // @rpg-trace: n1810
        this.itemNumber = itemNumber;
    }

    public String getSupplierRange() { // @rpg-trace: n1812
        return supplierRange;
    }

    public void setSupplierRange(String supplierRange) { // @rpg-trace: n1812
        this.supplierRange = supplierRange;
    }
}