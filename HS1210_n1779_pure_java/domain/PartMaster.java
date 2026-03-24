package com.scania.warranty.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ITLSMF3")
public class PartMaster {
    
    @Id
    @Column(name = "SFNR", length = 18, nullable = false)
    private String partNumber;
    
    @Column(name = "SFRAN", length = 2, nullable = false)
    private String partRange;
    
    // Constructors
    public PartMaster() {
    }
    
    // Getters and Setters
    public String getPartNumber() {
        return partNumber;
    }
    
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
    
    public String getPartRange() {
        return partRange;
    }
    
    public void setPartRange(String partRange) {
        this.partRange = partRange;
    }
}