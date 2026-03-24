package com.scania.warranty.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FailureAggregation {
    private Integer failureNumber;
    private boolean maintenance;
    private String groups;
    private String partNumber;
    private BigDecimal partValue;
    private List<String> textLines;
    private BigDecimal valueMaterial;
    private BigDecimal valueLabor;
    private BigDecimal valueSpecial;

    public FailureAggregation() {
        this.maintenance = false;
        this.groups = "";
        this.partNumber = "";
        this.partValue = BigDecimal.ZERO;
        this.textLines = new ArrayList<>();
        this.valueMaterial = BigDecimal.ZERO;
        this.valueLabor = BigDecimal.ZERO;
        this.valueSpecial = BigDecimal.ZERO;
    }

    public void reset(Integer newFailureNumber) {
        this.failureNumber = newFailureNumber;
        this.maintenance = false;
        this.groups = "";
        this.partNumber = "";
        this.partValue = BigDecimal.ZERO;
        this.textLines.clear();
        this.valueMaterial = BigDecimal.ZERO;
        this.valueLabor = BigDecimal.ZERO;
        this.valueSpecial = BigDecimal.ZERO;
    }

    public void addTextLine(String text) {
        if (textLines.size() < 4) {
            textLines.add(text);
        }
    }

    public Integer getFailureNumber() {
        return failureNumber;
    }

    public void setFailureNumber(Integer failureNumber) {
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

    public BigDecimal getValueMaterial() {
        return valueMaterial;
    }

    public void setValueMaterial(BigDecimal valueMaterial) {
        this.valueMaterial = valueMaterial;
    }

    public BigDecimal getValueLabor() {
        return valueLabor;
    }

    public void setValueLabor(BigDecimal valueLabor) {
        this.valueLabor = valueLabor;
    }

    public BigDecimal getValueSpecial() {
        return valueSpecial;
    }

    public void setValueSpecial(BigDecimal valueSpecial) {
        this.valueSpecial = valueSpecial;
    }
}