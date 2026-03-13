/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ITLSMF3")
public class ItemMasterRecord { // @rpg-trace: n1810
    
    @Id
    @Column(name = "SFNR", length = 18, nullable = false)
    private String partNumber; // @rpg-trace: n1810
    
    @Column(name = "SFRAN", length = 2, nullable = false)
    private String supplierRange; // @rpg-trace: n1810

    public ItemMasterRecord() {
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getSupplierRange() {
        return supplierRange;
    }

    public void setSupplierRange(String supplierRange) {
        this.supplierRange = supplierRange;
    }
}