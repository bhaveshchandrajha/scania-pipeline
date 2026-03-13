/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.domain;

import java.io.Serializable;
import java.util.Objects;

public class ClaimHeaderId implements Serializable {
    
    private String companyCode; // @rpg-trace: schema
    private String invoiceNumber; // @rpg-trace: schema
    private String invoiceDate; // @rpg-trace: schema
    private String orderNumber; // @rpg-trace: schema
    private String area; // @rpg-trace: schema
    
    public ClaimHeaderId() {
    }
    
    public ClaimHeaderId(String companyCode, String invoiceNumber, String invoiceDate, String orderNumber, String area) {
        this.companyCode = companyCode;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.orderNumber = orderNumber;
        this.area = area;
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
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimHeaderId that = (ClaimHeaderId) o;
        return Objects.equals(companyCode, that.companyCode) &&
               Objects.equals(invoiceNumber, that.invoiceNumber) &&
               Objects.equals(invoiceDate, that.invoiceDate) &&
               Objects.equals(orderNumber, that.orderNumber) &&
               Objects.equals(area, that.area);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(companyCode, invoiceNumber, invoiceDate, orderNumber, area);
    }
}