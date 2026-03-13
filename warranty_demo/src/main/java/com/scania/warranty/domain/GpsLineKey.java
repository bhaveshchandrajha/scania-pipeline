/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class GpsLineKey implements Serializable { // @rpg-trace: n1784
    
    private String abbreviation; // @rpg-trace: n1784
    private String claimNumber; // @rpg-trace: n1784
    private String failureNumber; // @rpg-trace: n1784
    private String sequenceNumber; // @rpg-trace: n1784

    public GpsLineKey() { // @rpg-trace: n1784
    }

    public GpsLineKey(String abbreviation, String claimNumber, String failureNumber, String sequenceNumber) { // @rpg-trace: n1784
        this.abbreviation = abbreviation; // @rpg-trace: n1784
        this.claimNumber = claimNumber; // @rpg-trace: n1784
        this.failureNumber = failureNumber; // @rpg-trace: n1784
        this.sequenceNumber = sequenceNumber; // @rpg-trace: n1784
    }

    public String getAbbreviation() { // @rpg-trace: n1784
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) { // @rpg-trace: n1784
        this.abbreviation = abbreviation;
    }

    public String getClaimNumber() { // @rpg-trace: n1784
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) { // @rpg-trace: n1784
        this.claimNumber = claimNumber;
    }

    public String getFailureNumber() { // @rpg-trace: n1784
        return failureNumber;
    }

    public void setFailureNumber(String failureNumber) { // @rpg-trace: n1784
        this.failureNumber = failureNumber;
    }

    public String getSequenceNumber() { // @rpg-trace: n1784
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) { // @rpg-trace: n1784
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public boolean equals(Object o) { // @rpg-trace: n1784
        if (this == o) return true; // @rpg-trace: n1784
        if (o == null || getClass() != o.getClass()) return false; // @rpg-trace: n1784
        GpsLineKey that = (GpsLineKey) o; // @rpg-trace: n1784
        return Objects.equals(abbreviation, that.abbreviation) && // @rpg-trace: n1784
               Objects.equals(claimNumber, that.claimNumber) && // @rpg-trace: n1784
               Objects.equals(failureNumber, that.failureNumber) && // @rpg-trace: n1784
               Objects.equals(sequenceNumber, that.sequenceNumber); // @rpg-trace: n1784
    }

    @Override
    public int hashCode() { // @rpg-trace: n1784
        return Objects.hash(abbreviation, claimNumber, failureNumber, sequenceNumber); // @rpg-trace: n1784
    }
}