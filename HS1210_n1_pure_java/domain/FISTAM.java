package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "FISTAM")
public class FISTAM {

    @Id
    @Column(name = "HDLNR", length = 5, nullable = false)
    private String dealerNumber;

    @Column(name = "SA", length = 2, nullable = false)
    private String sa;

    @Column(name = "ZUS", length = 1, nullable = false)
    private String zus;

    @Column(name = "KZ-FIBU", length = 2, nullable = false)
    private String accountingIndicator;

    @Column(name = "HDL-NR.ET", length = 4, nullable = false)
    private String dealerNumberEt;

    @Column(name = "VERS", length = 1, nullable = false)
    private String version;

    @Column(name = "FIRMENBEZEICHNUNG I", length = 40, nullable = false)
    private String companyName1;

    @Column(name = "FIRMENBEZEICHNUNG II", length = 30, nullable = false)
    private String companyName2;

    @Column(name = "STRASSE", length = 30, nullable = false)
    private String street;

    @Column(name = "LKZ", length = 3, nullable = false)
    private String countryCode;

    @Column(name = "PLZ", length = 5, nullable = false)
    private String postalCode;

    @Column(name = "ORT", length = 30, nullable = false)
    private String city;

    @Column(name = "BLZ1", length = 8, nullable = false)
    private String bankCode1;

    @Column(name = "RES1", length = 2, nullable = false)
    private String reserved1;

    @Column(name = "KTO-NR1", length = 15, nullable = false)
    private String accountNumber1;

    @Column(name = "BANKENNAME 1", length = 30, nullable = false)
    private String bankName1;

    @Column(name = "BLZ2", length = 8, nullable = false)
    private String bankCode2;

    @Column(name = "RES2", length = 2, nullable = false)
    private String reserved2;

    @Column(name = "KTO-NR2", length = 15, nullable = false)
    private String accountNumber2;

    @Column(name = "BANKENNAME 2", length = 30, nullable = false)
    private String bankName2;

    @Column(name = "BLZ3", length = 8, nullable = false)
    private String bankCode3;

    @Column(name = "RES3", length = 2, nullable = false)
    private String reserved3;

    @Column(name = "KTO-NR3", length = 15, nullable = false)
    private String accountNumber3;

    @Column(name = "BANKENNAME3", length = 30, nullable = false)
    private String bankName3;

    @Column(name = "KUNDENBERATER", length = 20, nullable = false)
    private String customerAdvisor;

    @Column(name = "TEILEBERATER", length = 20, nullable = false)
    private String partsAdvisor;

    @Column(name = "UST-ID-NR/OK", length = 20, nullable = false)
    private String vatIdNumber;

    @Column(name = "KENNW1", length = 6, nullable = false)
    private String password1;

    @Column(name = "KENNW2", length = 6, nullable = false)
    private String password2;

    @Column(name = "KENNW3", length = 6, nullable = false)
    private String password3;

    @Column(name = "RES5", length = 4, nullable = false)
    private String reserved5;

    @Column(name = "LFDNR.WE", precision = 5, scale = 0, nullable = false)
    private BigDecimal sequenceNumberWe;

    @Column(name = "LFDNR.THE", precision = 5, scale = 0, nullable = false)
    private BigDecimal sequenceNumberThe;

    @Column(name = "KRE.-JOUR.", precision = 5, scale = 0, nullable = false)
    private BigDecimal creditJournal;

    @Column(name = "L.JOUR-NR BU-EING1", length = 6, nullable = false)
    private String lastJournalNumberBuEing1;

    @Column(name = "JOUR-NR", precision = 5, scale = 0, nullable = false)
    private BigDecimal journalNumber;

    @Column(name = "RES8", precision = 5, scale = 0, nullable = false)
    private BigDecimal reserved8;

    @Column(name = "FEHL-KTO", length = 6, nullable = false)
    private String errorAccount;

    @Column(name = "BU-DAT", length = 8, nullable = false)
    private String bookingDate;

    @Column(name = "L.JOUR-NR BU-EING2", length = 6, nullable = false)
    private String lastJournalNumberBuEing2;

    @Column(name = "SPAS-NR.", length = 8, nullable = false)
    private String savingsNumber;

    @Column(name = "RES11", precision = 7, scale = 0, nullable = false)
    private BigDecimal reserved11;

    @Column(name = "RES12", precision = 9, scale = 2, nullable = false)
    private BigDecimal reserved12;

    @Column(name = "RES13", length = 1, nullable = false)
    private String reserved13;

    @Column(name = "RES14", length = 1, nullable = false)
    private String reserved14;

    @Column(name = "RES15", length = 1, nullable = false)
    private String reserved15;

    // Constructors
    public FISTAM() {
    }

    // Getters and Setters
    public String getDealerNumber() {
        return dealerNumber;
    }

    public void setDealerNumber(String dealerNumber) {
        this.dealerNumber = dealerNumber;
    }

    public String getSa() {
        return sa;
    }

    public void setSa(String sa) {
        this.sa = sa;
    }

    public String getZus() {
        return zus;
    }

    public void setZus(String zus) {
        this.zus = zus;
    }

    public String getAccountingIndicator() {
        return accountingIndicator;
    }

    public void setAccountingIndicator(String accountingIndicator) {
        this.accountingIndicator = accountingIndicator;
    }

    public String getDealerNumberEt() {
        return dealerNumberEt;
    }

    public void setDealerNumberEt(String dealerNumberEt) {
        this.dealerNumberEt = dealerNumberEt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCompanyName1() {
        return companyName1;
    }

    public void setCompanyName1(String companyName1) {
        this.companyName1 = companyName1;
    }

    public String getCompanyName2() {
        return companyName2;
    }

    public void setCompanyName2(String companyName2) {
        this.companyName2 = companyName2;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBankCode1() {
        return bankCode1;
    }

    public void setBankCode1(String bankCode1) {
        this.bankCode1 = bankCode1;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getAccountNumber1() {
        return accountNumber1;
    }

    public void setAccountNumber1(String accountNumber1) {
        this.accountNumber1 = accountNumber1;
    }

    public String getBankName1() {
        return bankName1;
    }

    public void setBankName1(String bankName1) {
        this.bankName1 = bankName1;
    }

    public String getBankCode2() {
        return bankCode2;
    }

    public void setBankCode2(String bankCode2) {
        this.bankCode2 = bankCode2;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public String getAccountNumber2() {
        return accountNumber2;
    }

    public void setAccountNumber2(String accountNumber2) {
        this.accountNumber2 = accountNumber2;
    }

    public String getBankName2() {
        return bankName2;
    }

    public void setBankName2(String bankName2) {
        this.bankName2 = bankName2;
    }

    public String getBankCode3() {
        return bankCode3;
    }

    public void setBankCode3(String bankCode3) {
        this.bankCode3 = bankCode3;
    }

    public String getReserved3() {
        return reserved3;
    }

    public void setReserved3(String reserved3) {
        this.reserved3 = reserved3;
    }

    public String getAccountNumber3() {
        return accountNumber3;
    }

    public void setAccountNumber3(String accountNumber3) {
        this.accountNumber3 = accountNumber3;
    }

    public String getBankName3() {
        return bankName3;
    }

    public void setBankName3(String bankName3) {
        this.bankName3 = bankName3;
    }

    public String getCustomerAdvisor() {
        return customerAdvisor;
    }

    public void setCustomerAdvisor(String customerAdvisor) {
        this.customerAdvisor = customerAdvisor;
    }

    public String getPartsAdvisor() {
        return partsAdvisor;
    }

    public void setPartsAdvisor(String partsAdvisor) {
        this.partsAdvisor = partsAdvisor;
    }

    public String getVatIdNumber() {
        return vatIdNumber;
    }

    public void setVatIdNumber(String vatIdNumber) {
        this.vatIdNumber = vatIdNumber;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getPassword3() {
        return password3;
    }

    public void setPassword3(String password3) {
        this.password3 = password3;
    }

    public String getReserved5() {
        return reserved5;
    }

    public void setReserved5(String reserved5) {
        this.reserved5 = reserved5;
    }

    public BigDecimal getSequenceNumberWe() {
        return sequenceNumberWe;
    }

    public void setSequenceNumberWe(BigDecimal sequenceNumberWe) {
        this.sequenceNumberWe = sequenceNumberWe;
    }

    public BigDecimal getSequenceNumberThe() {
        return sequenceNumberThe;
    }

    public void setSequenceNumberThe(BigDecimal sequenceNumberThe) {
        this.sequenceNumberThe = sequenceNumberThe;
    }

    public BigDecimal getCreditJournal() {
        return creditJournal;
    }

    public void setCreditJournal(BigDecimal creditJournal) {
        this.creditJournal = creditJournal;
    }

    public String getLastJournalNumberBuEing1() {
        return lastJournalNumberBuEing1;
    }

    public void setLastJournalNumberBuEing1(String lastJournalNumberBuEing1) {
        this.lastJournalNumberBuEing1 = lastJournalNumberBuEing1;
    }

    public BigDecimal getJournalNumber() {
        return journalNumber;
    }

    public void setJournalNumber(BigDecimal journalNumber) {
        this.journalNumber = journalNumber;
    }

    public BigDecimal getReserved8() {
        return reserved8;
    }

    public void setReserved8(BigDecimal reserved8) {
        this.reserved8 = reserved8;
    }

    public String getErrorAccount() {
        return errorAccount;
    }

    public void setErrorAccount(String errorAccount) {
        this.errorAccount = errorAccount;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getLastJournalNumberBuEing2() {
        return lastJournalNumberBuEing2;
    }

    public void setLastJournalNumberBuEing2(String lastJournalNumberBuEing2) {
        this.lastJournalNumberBuEing2 = lastJournalNumberBuEing2;
    }

    public String getSavingsNumber() {
        return savingsNumber;
    }

    public void setSavingsNumber(String savingsNumber) {
        this.savingsNumber = savingsNumber;
    }

    public BigDecimal getReserved11() {
        return reserved11;
    }

    public void setReserved11(BigDecimal reserved11) {
        this.reserved11 = reserved11;
    }

    public BigDecimal getReserved12() {
        return reserved12;
    }

    public void setReserved12(BigDecimal reserved12) {
        this.reserved12 = reserved12;
    }

    public String getReserved13() {
        return reserved13;
    }

    public void setReserved13(String reserved13) {
        this.reserved13 = reserved13;
    }

    public String getReserved14() {
        return reserved14;
    }

    public void setReserved14(String reserved14) {
        this.reserved14 = reserved14;
    }

    public String getReserved15() {
        return reserved15;
    }

    public void setReserved15(String reserved15) {
        this.reserved15 = reserved15;
    }
}