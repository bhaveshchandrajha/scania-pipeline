/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FailureCreationContext { // @rpg-trace: n1779
    private int failureNumber; // @rpg-trace: n1783
    private boolean maintenance; // @rpg-trace: n1791
    private String groups; // @rpg-trace: n1796
    private String partNumber; // @rpg-trace: n1797
    private BigDecimal partValue; // @rpg-trace: n1798
    private List<String> textLines; // @rpg-trace: n1792
    private int lineCount; // @rpg-trace: n1792
    private BigDecimal valueMaterial; // @rpg-trace: n1793
    private BigDecimal valueLabor; // @rpg-trace: n1794
    private BigDecimal valueSpecial; // @rpg-trace: n1795

    public FailureCreationContext() { // @rpg-trace: n1779
        this.failureNumber = 0; // @rpg-trace: n1783
        this.maintenance = false; // @rpg-trace: n1791
        this.groups = ""; // @rpg-trace: n1796
        this.partNumber = ""; // @rpg-trace: n1797
        this.partValue = BigDecimal.ZERO; // @rpg-trace: n1798
        this.textLines = new ArrayList<>(); // @rpg-trace: n1792
        this.lineCount = 0; // @rpg-trace: n1792
        this.valueMaterial = BigDecimal.ZERO; // @rpg-trace: n1793
        this.valueLabor = BigDecimal.ZERO; // @rpg-trace: n1794
        this.valueSpecial = BigDecimal.ZERO; // @rpg-trace: n1795
    }

    public void resetForNewFailure(int newFailureNumber) { // @rpg-trace: n1788
        this.failureNumber = newFailureNumber; // @rpg-trace: n1790
        this.maintenance = false; // @rpg-trace: n1791
        this.textLines.clear(); // @rpg-trace: n1792
        this.lineCount = 0; // @rpg-trace: n1792
        this.valueMaterial = BigDecimal.ZERO; // @rpg-trace: n1793
        this.valueLabor = BigDecimal.ZERO; // @rpg-trace: n1794
        this.valueSpecial = BigDecimal.ZERO; // @rpg-trace: n1795
        this.groups = ""; // @rpg-trace: n1796
        this.partNumber = ""; // @rpg-trace: n1797
        this.partValue = BigDecimal.ZERO; // @rpg-trace: n1798
    }

    public int getFailureNumber() {
        return failureNumber;
    }

    public void setFailureNumber(int failureNumber) {
        this.failureNumber = failureNumber;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public BigDecimal getPartValue() {
        return partValue;
    }

    public void setPartValue(BigDecimal partValue) {
        this.partValue = partValue;
    }

    public List<String> getTextLines() {
        return textLines;
    }

    public void addTextLine(String text) { // @rpg-trace: n1808
        if (lineCount < 4) { // @rpg-trace: n1808
            this.textLines.add(text); // @rpg-trace: n1808
            this.lineCount++; // @rpg-trace: n1808
        }
    }

    public int getLineCount() {
        return lineCount;
    }

    public BigDecimal getValueMaterial() {
        return valueMaterial;
    }

    public void setValueMaterial(BigDecimal valueMaterial) {
        this.valueMaterial = valueMaterial;
    }

    public void addValueMaterial(BigDecimal amount) { // @rpg-trace: n1800
        this.valueMaterial = this.valueMaterial.add(amount); // @rpg-trace: n1800
    }

    public BigDecimal getValueLabor() {
        return valueLabor;
    }

    public void setValueLabor(BigDecimal valueLabor) {
        this.valueLabor = valueLabor;
    }

    public void addValueLabor(BigDecimal amount) { // @rpg-trace: n1800
        this.valueLabor = this.valueLabor.add(amount); // @rpg-trace: n1800
    }

    public BigDecimal getValueSpecial() {
        return valueSpecial;
    }

    public void setValueSpecial(BigDecimal valueSpecial) {
        this.valueSpecial = valueSpecial;
    }

    public void addValueSpecial(BigDecimal amount) { // @rpg-trace: n1800
        this.valueSpecial = this.valueSpecial.add(amount); // @rpg-trace: n1800
    }
}