package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "S3F085")
public class CostPercentage {

    @Id
    @Column(name = "CONTROL_CODE")
    private String controlCode;

    @Column(name = "DMCPWF")
    private BigDecimal materialCostPercentage;

    @Column(name = "DLCPWF")
    private BigDecimal laborCostPercentage;

    @Column(name = "DSCPWF")
    private BigDecimal specialCostPercentage;

    // Constructors
    public CostPercentage() {
    }

    // Getters and Setters
    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public BigDecimal getMaterialCostPercentage() {
        return materialCostPercentage;
    }

    public void setMaterialCostPercentage(BigDecimal materialCostPercentage) {
        this.materialCostPercentage = materialCostPercentage;
    }

    public BigDecimal getLaborCostPercentage() {
        return laborCostPercentage;
    }

    public void setLaborCostPercentage(BigDecimal laborCostPercentage) {
        this.laborCostPercentage = laborCostPercentage;
    }

    public BigDecimal getSpecialCostPercentage() {
        return specialCostPercentage;
    }

    public void setSpecialCostPercentage(BigDecimal specialCostPercentage) {
        this.specialCostPercentage = specialCostPercentage;
    }
}