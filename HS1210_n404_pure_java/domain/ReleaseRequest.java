/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import jakarta.persistence.*;

/**
 * JPA entity for release request (HSG70F).
 */
@Entity
@Table(name = "HSG70F")
public class ReleaseRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "KZL", length = 3)
    private String companyCode;
    
    @Column(name = "R.NR.", length = 5)
    private String invoiceNumber;
    
    @Column(name = "R.DAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "FGNR.", length = 17)
    private String chassisNumber;
    
    @Column(name = "REP.DAT.", length = 8)
    private String repairDate;
    
    @Column(name = "STATUS", length = 1)
    private String status;
    
    @Column(name = "CUS.NO.", precision = 5, scale = 0)
    private Integer customerNumber;
    
    @Column(name = "D.C.NO.", precision = 8, scale = 0)
    private Integer dcNumber;
    
    @Column(name = "D.C.FN.", length = 5)
    private String dcFn;

    // Constructors
    public ReleaseRequest() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) { this.chassisNumber = chassisNumber; }

    public String getRepairDate() { return repairDate; }
    public void setRepairDate(String repairDate) { this.repairDate = repairDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(Integer customerNumber) { this.customerNumber = customerNumber; }

    public Integer getDcNumber() { return dcNumber; }
    public void setDcNumber(Integer dcNumber) { this.dcNumber = dcNumber; }

    public String getDcFn() { return dcFn; }
    public void setDcFn(String dcFn) { this.dcFn = dcFn; }
}