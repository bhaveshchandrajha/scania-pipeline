/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ClaimPositionLineId implements Serializable {
    
    private String abbreviation; // @rpg-trace: schema
    private String claimNumber; // @rpg-trace: schema
    private String failureNumber; // @rpg-trace: schema
    private String sequenceNumber; // @rpg-trace: schema
    private BigDecimal lineNumber; // @rpg-trace: schema
    private BigDecimal distribution; // @rpg-trace: schema
    
    public ClaimPositionLineId() {
    }
    
    public ClaimPositionLineId(String abbreviation, String claimNumber, String failureNumber, String sequenceNumber, BigDecimal lineNumber, BigDecimal distribution) {
        this.abbreviation = abbreviation;
        this.claimNumber = claimNumber;
        this.failureNumber = failureNumber;
        this.sequenceNumber = sequenceNumber;
        this.lineNumber = lineNumber;
        this.distribution = distribution;
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }
    
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    
    public String getClaimNumber() {
        return claimNumber;
    }
    
    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }
    
    public String getFailureNumber() {
        return failureNumber;
    }
    
    public void setFailureNumber(String failureNumber) {
        this.failureNumber = failureNumber;
    }
    
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public BigDecimal getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(BigDecimal lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public BigDecimal getDistribution() {
        return distribution;
    }
    
    public void setDistribution(BigDecimal distribution) {
        this.distribution = distribution;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimPositionLineId that = (ClaimPositionLineId) o;
        return Objects.equals(abbreviation, that.abbreviation) &&
               Objects.equals(claimNumber, that.claimNumber) &&
               Objects.equals(failureNumber, that.failureNumber) &&
               Objects.equals(sequenceNumber, that.sequenceNumber) &&
               Objects.equals(lineNumber, that.lineNumber) &&
               Objects.equals(distribution, that.distribution);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(abbreviation, claimNumber, failureNumber, sequenceNumber, lineNumber, distribution);
    }
}