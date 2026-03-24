package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimErrorId implements Serializable {
    
    private String companyCode;
    private String invoiceNumber;
    private String errorNumber;
    
    public ClaimErrorId() {
    }
    
    public ClaimErrorId(String companyCode, String invoiceNumber, String errorNumber) {
        this.companyCode = companyCode;
        this.invoiceNumber = invoiceNumber;
        this.errorNumber = errorNumber;
    }
    
    public String getCompanyCode() {
        return companyCode;
    }
    
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getErrorNumber() {
        return errorNumber;
    }
    
    public void setErrorNumber(String errorNumber) {
        this.errorNumber = errorNumber;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimErrorId that = (ClaimErrorId) o;
        return Objects.equals(companyCode, that.companyCode) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(errorNumber, that.errorNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(companyCode, invoiceNumber, errorNumber);
    }
}