package com.scania.warranty.domain;

import java.util.Objects;

public class ClaimKey {
    private String companyCode;
    private String invoiceNumber;
    private String invoiceDate;
    
    public ClaimKey() {
    }
    
    public ClaimKey(String companyCode, String invoiceNumber, String invoiceDate) {
        this.companyCode = companyCode;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
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
    
    public String getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimKey claimKey = (ClaimKey) o;
        return Objects.equals(companyCode, claimKey.companyCode) &&
               Objects.equals(invoiceNumber, claimKey.invoiceNumber) &&
               Objects.equals(invoiceDate, claimKey.invoiceDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(companyCode, invoiceNumber, invoiceDate);
    }
}