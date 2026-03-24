package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimPositionId implements Serializable {
    
    private String abbreviation;
    private String claimNumber;
    private String errorNumber;
    private String sequenceNumber;
    
    public ClaimPositionId() {
    }
    
    public ClaimPositionId(String abbreviation, String claimNumber, String errorNumber, String sequenceNumber) {
        this.abbreviation = abbreviation;
        this.claimNumber = claimNumber;
        this.errorNumber = errorNumber;
        this.sequenceNumber = sequenceNumber;
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
    
    public String getErrorNumber() {
        return errorNumber;
    }
    
    public void setErrorNumber(String errorNumber) {
        this.errorNumber = errorNumber;
    }
    
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimPositionId that = (ClaimPositionId) o;
        return Objects.equals(abbreviation, that.abbreviation) &&
               Objects.equals(claimNumber, that.claimNumber) &&
               Objects.equals(errorNumber, that.errorNumber) &&
               Objects.equals(sequenceNumber, that.sequenceNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(abbreviation, claimNumber, errorNumber, sequenceNumber);
    }
}