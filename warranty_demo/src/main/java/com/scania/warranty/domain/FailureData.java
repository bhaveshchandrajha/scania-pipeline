/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FailureData { // @rpg-trace: n1788
    
    private Integer failureNumber; // @rpg-trace: n1783
    private String groups; // @rpg-trace: n1796
    private String partNumber; // @rpg-trace: n1816
    private boolean maintenance; // @rpg-trace: n1791
    private List<String> textLines; // @rpg-trace: n1792
    private BigDecimal valueMaterial; // @rpg-trace: n1793
    private BigDecimal valueLabor; // @rpg-trace: n1794
    private BigDecimal valueSpecial; // @rpg-trace: n1795

    public FailureData() { // @rpg-trace: n1788
        this.textLines = new ArrayList<>(); // @rpg-trace: n1792
        this.valueMaterial = BigDecimal.ZERO; // @rpg-trace: n1793
        this.valueLabor = BigDecimal.ZERO; // @rpg-trace: n1794
        this.valueSpecial = BigDecimal.ZERO; // @rpg-trace: n1795
        this.maintenance = false; // @rpg-trace: n1791
        this.groups = ""; // @rpg-trace: n1796
        this.partNumber = ""; // @rpg-trace: n1816
    }

    public Integer getFailureNumber() { // @rpg-trace: n1783
        return failureNumber;
    }

    public void setFailureNumber(Integer failureNumber) { // @rpg-trace: n1783
        this.failureNumber = failureNumber;
    }

    public String getGroups() { // @rpg-trace: n1796
        return groups;
    }

    public void setGroups(String groups) { // @rpg-trace: n1796
        this.groups = groups;
    }

    public String getPartNumber() { // @rpg-trace: n1816
        return partNumber;
    }

    public void setPartNumber(String partNumber) { // @rpg-trace: n1816
        this.partNumber = partNumber;
    }

    public boolean isMaintenance() { // @rpg-trace: n1791
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) { // @rpg-trace: n1791
        this.maintenance = maintenance;
    }

    public List<String> getTextLines() { // @rpg-trace: n1792
        return textLines;
    }

    public void setTextLines(List<String> textLines) { // @rpg-trace: n1792
        this.textLines = textLines;
    }

    public BigDecimal getValueMaterial() { // @rpg-trace: n1793
        return valueMaterial;
    }

    public void setValueMaterial(BigDecimal valueMaterial) { // @rpg-trace: n1793
        this.valueMaterial = valueMaterial;
    }

    public BigDecimal getValueLabor() { // @rpg-trace: n1794
        return valueLabor;
    }

    public void setValueLabor(BigDecimal valueLabor) { // @rpg-trace: n1794
        this.valueLabor = valueLabor;
    }

    public BigDecimal getValueSpecial() { // @rpg-trace: n1795
        return valueSpecial;
    }

    public void setValueSpecial(BigDecimal valueSpecial) { // @rpg-trace: n1795
        this.valueSpecial = valueSpecial;
    }
}