package com.scania.warranty.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HSAHKLF3")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "PAKZ", length = 3)
    private String companyCode;
    
    @Column(name = "RNR", length = 5)
    private String invoiceNumber;
    
    @Column(name = "RG-NR. 10A", length = 10)
    private String invoiceNumber10;
    
    @Column(name = "RDAT", length = 8)
    private String invoiceDate;
    
    @Column(name = "KZ S", length = 1)
    private String indicatorS;
    
    @Column(name = "ANR", length = 5)
    private String orderNumber;
    
    @Column(name = "BEREI", length = 1)
    private String area;
    
    @Column(name = "W/T", length = 1)
    private String workshopCounter;
    
    @Column(name = "SPLITT", length = 2)
    private String split;
    
    @Column(name = "ADAT", length = 8)
    private String orderDate;
    
    @Column(name = "ATEXT", length = 40)
    private String orderText;
    
    @Column(name = "L.RNR", length = 5)
    private String lastInvoiceNumber;
    
    @Column(name = "STO-BEZ-RE", length = 5)
    private String cancelReferenceInvoice;
    
    @Column(name = "STO-BEZ-REDAT", length = 8)
    private String cancelReferenceInvoiceDate;
    
    @Column(name = "KOR-BEZ-RE", length = 5)
    private String correctionReferenceInvoice;
    
    @Column(name = "KOR-BEZ-REDAT", length = 8)
    private String correctionReferenceInvoiceDate;
    
    @Column(name = "BFORT", length = 1)
    private String carryForward;
    
    @Column(name = "MWST Y/N", length = 1)
    private String vatYesNo;
    
    @Column(name = "MWST %", precision = 5, scale = 2)
    private BigDecimal vatPercent;
    
    @Column(name = "MWST % R.", precision = 5, scale = 2)
    private BigDecimal vatPercentReduced;
    
    @Column(name = "BA-SCHLÜSSEL", length = 2)
    private String accountingKey;
    
    @Column(name = "KST LOHN", length = 5)
    private String costCenterLabor;
    
    @Column(name = "KST TEILE", length = 5)
    private String costCenterParts;
    
    @Column(name = "FIBU MWST", length = 6)
    private String accountingVat;
    
    @Column(name = "FIBU MWST AT", length = 6)
    private String accountingVatAustria;
    
    @Column(name = "FIBU INTERIM", length = 6)
    private String accountingInterim;
    
    @Column(name = "KTO INTAUF.", length = 6)
    private String accountInternalOrder;
    
    @Column(name = "KTR INT AUF.", length = 7)
    private String costCenterInternalOrder;
    
    @Column(name = "KST INT AUF.", length = 5)
    private String costCenterInternalOrderShort;
    
    @Column(name = "SPEZ-CODE", length = 10)
    private String specialCode;
    
    @Column(name = "BRANCH", length = 3)
    private String branch;
    
    @Column(name = "PROD-CODE", length = 10)
    private String productCode;
    
    @Column(name = "PROJEKT", length = 10)
    private String project;
    
    @Column(name = "DOKUMENTENNUMMER", length = 20)
    private String documentNumber;
    
    @Column(name = "KOSTENCODE KONZINT.", length = 3)
    private String costCodeConcernInternal;
    
    @Column(name = "KUNDEN-NR.", length = 6)
    private String customerNumber;
    
    @Column(name = "ANREDE", length = 1)
    private String salutation;
    
    @Column(name = "NAME", length = 30)
    private String name;
    
    @Column(name = "BRANCHE", length = 25)
    private String industry;
    
    @Column(name = "MATCH", length = 5)
    private String matchCode;
    
    @Column(name = "STRASSE", length = 25)
    private String street;
    
    @Column(name = "LAND", length = 3)
    private String country;
    
    @Column(name = "PLZ", length = 5)
    private String postalCode;
    
    @Column(name = "ORT", length = 20)
    private String city;
    
    @Column(name = "TELEFON", length = 17)
    private String telephone;
    
    @Column(name = "BESTELLER KUNDE", length = 20)
    private String ordererCustomer;
    
    @Column(name = "VALUTA", length = 1)
    private String currency;
    
    @Column(name = "BONITÄT", length = 1)
    private String creditworthiness;
    
    @Column(name = "ZAHLUNGSART", length = 1)
    private String paymentMethod;
    
    @Column(name = "RC", length = 3)
    private String returnCode;
    
    @Column(name = "RE KUNDEN-NR.", length = 6)
    private String invoiceCustomerNumber;
    
    @Column(name = "RE ANREDE", length = 1)
    private String invoiceSalutation;
    
    @Column(name = "RE NAME", length = 30)
    private String invoiceName;
    
    @Column(name = "RE BRANCHE", length = 25)
    private String invoiceIndustry;
    
    @Column(name = "RE MATCH", length = 5)
    private String invoiceMatchCode;
    
    @Column(name = "RE STRASSE", length = 25)
    private String invoiceStreet;
    
    @Column(name = "RE LAND", length = 3)
    private String invoiceCountry;
    
    @Column(name = "RE PLZ", length = 5)
    private String invoicePostalCode;
    
    @Column(name = "RE ORT", length = 20)
    private String invoiceCity;
    
    @Column(name = "RE TELE.", length = 17)
    private String invoiceTelephone;
    
    @Column(name = "RE VALUTA", length = 1)
    private String invoiceCurrency;
    
    @Column(name = "RE BONITÄT", length = 1)
    private String invoiceCreditworthiness;
    
    @Column(name = "RE ZART", length = 1)
    private String invoicePaymentMethod;
    
    @Column(name = "RE RC", length = 3)
    private String invoiceReturnCode;
    
    @Column(name = "UST-ID-NR/OK", length = 20)
    private String vatIdNumber;
    
    @Column(name = "FAHRG.-NR.", length = 17)
    private String vehicleNumber;
    
    @Column(name = "KZ", length = 12)
    private String licensePlate;
    
    @Column(name = "TYP", length = 15)
    private String vehicleType;
    
    @Column(name = "BJ", length = 4)
    private String buildYear;
    
    @Column(name = "ZDAT", length = 8)
    private String registrationDate;
    
    @Column(name = "WRG.", length = 3)
    private String currency3;
    
    @Column(name = "AU", length = 6)
    private String inspectionDate;
    
    @Column(name = "GA", length = 8)
    private String warrantyDate;
    
    @Column(name = "SP", length = 6)
    private String servicePackage;
    
    @Column(name = "TACHO", length = 8)
    private String odometer;
    
    @Column(name = "KM", length = 8)
    private String kilometers;
    
    @Column(name = "HU", length = 6)
    private String mainInspection;
    
    @Column(name = "AN-TAG", length = 8)
    private String acceptanceDate;
    
    @Column(name = "AN-ZEIT", length = 4)
    private String acceptanceTime;
    
    @Column(name = "FERT-TAG", length = 8)
    private String completionDate;
    
    @Column(name = "FERT-ZEIT", length = 4)
    private String completionTime;
    
    @Column(name = "BERATER", length = 20)
    private String advisor;
    
    @Column(name = "LEITZAHL", length = 3)
    private String routingNumber;
    
    @Column(name = "TX.ANF", length = 3)
    private String textStart;
    
    @Column(name = "TX.ENDE", length = 3)
    private String textEnd;
    
    @Column(name = "MOTOR-NR", length = 10)
    private String engineNumber;
    
    @Column(name = "MOTOR-TYP", length = 20)
    private String engineType;
    
    @Column(name = "USER AUFTRAG", length = 10)
    private String userOrder;
    
    @Column(name = "USER RECHNUNG", length = 10)
    private String userInvoice;
    
    @Column(name = "RGS NETTO", precision = 9, scale = 2)
    private BigDecimal invoiceNetAmount;
    
    @Column(name = "RGS BASIS AT", precision = 9, scale = 2)
    private BigDecimal invoiceBasisAustria;
    
    @Column(name = "RGS BASIS MWST", precision = 9, scale = 2)
    private BigDecimal invoiceBasisVat;
    
    @Column(name = "RGS MWST", precision = 9, scale = 2)
    private BigDecimal invoiceVat;
    
    @Column(name = "RGS MWST AT", precision = 9, scale = 2)
    private BigDecimal invoiceVatAustria;
    
    @Column(name = "RGS GES BRUTTO", precision = 9, scale = 2)
    private BigDecimal invoiceTotalGross;
    
    @Column(name = "EG-UMSATZ", length = 1)
    private String euSales;
    
    @Column(name = "STEUERFREI DRITTLAND", length = 1)
    private String taxFreeThirdCountry;
    
    @Column(name = "VERBUCHT?", length = 1)
    private String posted;
    
    @Column(name = "RESERVE", precision = 5, scale = 2)
    private BigDecimal reserve1;
    
    @Column(name = "RESERVE", precision = 9, scale = 2, insertable = false, updatable = false)
    private BigDecimal reserve2;
    
    @Column(name = "GA-ÜBERN.", length = 8)
    private String warrantyTakeover;
    
    @Column(name = "WKT-ID", precision = 9, scale = 0)
    private Integer workshopId;
    
    @Column(name = "RESERVE", precision = 2, scale = 0, insertable = false, updatable = false)
    private Integer reserve3;
    
    @Column(name = "RESERVE", precision = 2, scale = 0, insertable = false, updatable = false)
    private Integer reserve4;
    
    @Column(name = "F:V>0", precision = 3, scale = 0)
    private Integer countVGreaterZero;
    
    @Column(name = "F:B>0", precision = 3, scale = 0)
    private Integer countBGreaterZero;
    
    @Column(name = "KAMPAGNE-NR", precision = 6, scale = 0)
    private Integer campaignNumber;
    
    @Column(name = "SPO ORDER", length = 10)
    private String spoOrder;
    
    @Column(name = "KEN-AV", length = 2)
    private String identifierAv;
    
    @Column(name = "KEN-PE", length = 2)
    private String identifierPe;
    
    @Column(name = "KLR-BERECH", length = 1)
    private String costCalculation;
    
    @Column(name = "KLR-BETRAG", precision = 5, scale = 2)
    private BigDecimal costAmount;
    
    @Column(name = "ASSI-VORGANG-NR", length = 15)
    private String assistanceProcessNumber;
    
    @Column(name = "ZAGA-GUELTIG", length = 8)
    private String additionalWarrantyValid;
    
    @Column(name = "R&W FREIGABE-NR", length = 15)
    private String rwApprovalNumber;
    
    @Column(name = "KL-ERWEITERUNG", precision = 5, scale = 0)
    private Integer goodwillExtension;
    
    @Column(name = "KL-AUSNAHME IDNR", length = 3)
    private String goodwillExceptionId;
    
    @Column(name = "KL-AUSNAHME KLARTEXT", length = 40)
    private String goodwillExceptionText;
    
    @Column(name = "FAHRZEUG-ART", length = 20)
    private String vehicleCategory;
    
    @Column(name = "HERSTELLER", length = 20)
    private String manufacturer;
    
    @Column(name = "AUFBAUART", length = 20)
    private String bodyType;
    
    @Column(name = "HERSTELLER AUFBAU", length = 20)
    private String bodyManufacturer;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 1", length = 20)
    private String additionalEquipment1;
    
    @Column(name = "HERSTELLER ZUSATZ 1", length = 20)
    private String additionalEquipmentManufacturer1;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 2", length = 20)
    private String additionalEquipment2;
    
    @Column(name = "HERSTELLER ZUSATZ 2", length = 20)
    private String additionalEquipmentManufacturer2;
    
    @Column(name = "ZUSATZAUSRÜSTUNG 3", length = 20)
    private String additionalEquipment3;
    
    @Column(name = "HERSTELLER ZUSATZ 3", length = 20)
    private String additionalEquipmentManufacturer3;
    
    @Column(name = "EINSATZART", length = 20)
    private String usageType;
    
    @Column(name = "EURO-NORM", length = 10)
    private String euroStandard;
    
    @Column(name = "PARTIKELFILTER", length = 1)
    private String particleFilter;
    
    @Column(name = "IS-ART", length = 5)
    private String isType;
    
    @Column(name = "MAIL TO", length = 200)
    private String mailTo;
    
    @Column(name = "MAIL CC", length = 200)
    private String mailCc;

    public Invoice() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getInvoiceNumber10() {
        return invoiceNumber10;
    }

    public void setInvoiceNumber10(String invoiceNumber10) {
        this.invoiceNumber10 = invoiceNumber10;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getIndicatorS() {
        return indicatorS;
    }

    public void setIndicatorS(String indicatorS) {
        this.indicatorS = indicatorS;
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

    public String getWorkshopCounter() {
        return workshopCounter;
    }

    public void setWorkshopCounter(String workshopCounter) {
        this.workshopCounter = workshopCounter;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

    public String getLastInvoiceNumber() {
        return lastInvoiceNumber;
    }

    public void setLastInvoiceNumber(String lastInvoiceNumber) {
        this.lastInvoiceNumber = lastInvoiceNumber;
    }

    public String getCancelReferenceInvoice() {
        return cancelReferenceInvoice;
    }

    public void setCancelReferenceInvoice(String cancelReferenceInvoice) {
        this.cancelReferenceInvoice = cancelReferenceInvoice;
    }

    public String getCancelReferenceInvoiceDate() {
        return cancelReferenceInvoiceDate;
    }

    public void setCancelReferenceInvoiceDate(String cancelReferenceInvoiceDate) {
        this.cancelReferenceInvoiceDate = cancelReferenceInvoiceDate;
    }

    public String getCorrectionReferenceInvoice() {
        return correctionReferenceInvoice;
    }

    public void setCorrectionReferenceInvoice(String correctionReferenceInvoice) {
        this.correctionReferenceInvoice = correctionReferenceInvoice;
    }

    public String getCorrectionReferenceInvoiceDate() {
        return correctionReferenceInvoiceDate;
    }

    public void setCorrectionReferenceInvoiceDate(String correctionReferenceInvoiceDate) {
        this.correctionReferenceInvoiceDate = correctionReferenceInvoiceDate;
    }

    public String getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(String carryForward) {
        this.carryForward = carryForward;
    }

    public String getVatYesNo() {
        return vatYesNo;
    }

    public void setVatYesNo(String vatYesNo) {
        this.vatYesNo = vatYesNo;
    }

    public BigDecimal getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(BigDecimal vatPercent) {
        this.vatPercent = vatPercent;
    }

    public BigDecimal getVatPercentReduced() {
        return vatPercentReduced;
    }

    public void setVatPercentReduced(BigDecimal vatPercentReduced) {
        this.vatPercentReduced = vatPercentReduced;
    }

    public String getAccountingKey() {
        return accountingKey;
    }

    public void setAccountingKey(String accountingKey) {
        this.accountingKey = accountingKey;
    }

    public String getCostCenterLabor() {
        return costCenterLabor;
    }

    public void setCostCenterLabor(String costCenterLabor) {
        this.costCenterLabor = costCenterLabor;
    }

    public String getCostCenterParts() {
        return costCenterParts;
    }

    public void setCostCenterParts(String costCenterParts) {
        this.costCenterParts = costCenterParts;
    }

    public String getAccountingVat() {
        return accountingVat;
    }

    public void setAccountingVat(String accountingVat) {
        this.accountingVat = accountingVat;
    }

    public String getAccountingVatAustria() {
        return accountingVatAustria;
    }

    public void setAccountingVatAustria(String accountingVatAustria) {
        this.accountingVatAustria = accountingVatAustria;
    }

    public String getAccountingInterim() {
        return accountingInterim;
    }

    public void setAccountingInterim(String accountingInterim) {
        this.accountingInterim = accountingInterim;
    }

    public String getAccountInternalOrder() {
        return accountInternalOrder;
    }

    public void setAccountInternalOrder(String accountInternalOrder) {
        this.accountInternalOrder = accountInternalOrder;
    }

    public String getCostCenterInternalOrder() {
        return costCenterInternalOrder;
    }

    public void setCostCenterInternalOrder(String costCenterInternalOrder) {
        this.costCenterInternalOrder = costCenterInternalOrder;
    }

    public String getCostCenterInternalOrderShort() {
        return costCenterInternalOrderShort;
    }

    public void setCostCenterInternalOrderShort(String costCenterInternalOrderShort) {
        this.costCenterInternalOrderShort = costCenterInternalOrderShort;
    }

    public String getSpecialCode() {
        return specialCode;
    }

    public void setSpecialCode(String specialCode) {
        this.specialCode = specialCode;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getCostCodeConcernInternal() {
        return costCodeConcernInternal;
    }

    public void setCostCodeConcernInternal(String costCodeConcernInternal) {
        this.costCodeConcernInternal = costCodeConcernInternal;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getOrdererCustomer() {
        return ordererCustomer;
    }

    public void setOrdererCustomer(String ordererCustomer) {
        this.ordererCustomer = ordererCustomer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreditworthiness() {
        return creditworthiness;
    }

    public void setCreditworthiness(String creditworthiness) {
        this.creditworthiness = creditworthiness;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getInvoiceCustomerNumber() {
        return invoiceCustomerNumber;
    }

    public void setInvoiceCustomerNumber(String invoiceCustomerNumber) {
        this.invoiceCustomerNumber = invoiceCustomerNumber;
    }

    public String getInvoiceSalutation() {
        return invoiceSalutation;
    }

    public void setInvoiceSalutation(String invoiceSalutation) {
        this.invoiceSalutation = invoiceSalutation;
    }

    public String getInvoiceName() {
        return invoiceName;
    }

    public void setInvoiceName(String invoiceName) {
        this.invoiceName = invoiceName;
    }

    public String getInvoiceIndustry() {
        return invoiceIndustry;
    }

    public void setInvoiceIndustry(String invoiceIndustry) {
        this.invoiceIndustry = invoiceIndustry;
    }

    public String getInvoiceMatchCode() {
        return invoiceMatchCode;
    }

    public void setInvoiceMatchCode(String invoiceMatchCode) {
        this.invoiceMatchCode = invoiceMatchCode;
    }

    public String getInvoiceStreet() {
        return invoiceStreet;
    }

    public void setInvoiceStreet(String invoiceStreet) {
        this.invoiceStreet = invoiceStreet;
    }

    public String getInvoiceCountry() {
        return invoiceCountry;
    }

    public void setInvoiceCountry(String invoiceCountry) {
        this.invoiceCountry = invoiceCountry;
    }

    public String getInvoicePostalCode() {
        return invoicePostalCode;
    }

    public void setInvoicePostalCode(String invoicePostalCode) {
        this.invoicePostalCode = invoicePostalCode;
    }

    public String getInvoiceCity() {
        return invoiceCity;
    }

    public void setInvoiceCity(String invoiceCity) {
        this.invoiceCity = invoiceCity;
    }

    public String getInvoiceTelephone() {
        return invoiceTelephone;
    }

    public void setInvoiceTelephone(String invoiceTelephone) {
        this.invoiceTelephone = invoiceTelephone;
    }

    public String getInvoiceCurrency() {
        return invoiceCurrency;
    }

    public void setInvoiceCurrency(String invoiceCurrency) {
        this.invoiceCurrency = invoiceCurrency;
    }

    public String getInvoiceCreditworthiness() {
        return invoiceCreditworthiness;
    }

    public void setInvoiceCreditworthiness(String invoiceCreditworthiness) {
        this.invoiceCreditworthiness = invoiceCreditworthiness;
    }

    public String getInvoicePaymentMethod() {
        return invoicePaymentMethod;
    }

    public void setInvoicePaymentMethod(String invoicePaymentMethod) {
        this.invoicePaymentMethod = invoicePaymentMethod;
    }

    public String getInvoiceReturnCode() {
        return invoiceReturnCode;
    }

    public void setInvoiceReturnCode(String invoiceReturnCode) {
        this.invoiceReturnCode = invoiceReturnCode;
    }

    public String getVatIdNumber() {
        return vatIdNumber;
    }

    public void setVatIdNumber(String vatIdNumber) {
        this.vatIdNumber = vatIdNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBuildYear() {
        return buildYear;
    }

    public void setBuildYear(String buildYear) {
        this.buildYear = buildYear;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getCurrency3() {
        return currency3;
    }

    public void setCurrency3(String currency3) {
        this.currency3 = currency3;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getWarrantyDate() {
        return warrantyDate;
    }

    public void setWarrantyDate(String warrantyDate) {
        this.warrantyDate = warrantyDate;
    }

    public String getServicePackage() {
        return servicePackage;
    }

    public void setServicePackage(String servicePackage) {
        this.servicePackage = servicePackage;
    }

    public String getOdometer() {
        return odometer;
    }

    public void setOdometer(String odometer) {
        this.odometer = odometer;
    }

    public String getKilometers() {
        return kilometers;
    }

    public void setKilometers(String kilometers) {
        this.kilometers = kilometers;
    }

    public String getMainInspection() {
        return mainInspection;
    }

    public void setMainInspection(String mainInspection) {
        this.mainInspection = mainInspection;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public String getAcceptanceTime() {
        return acceptanceTime;
    }

    public void setAcceptanceTime(String acceptanceTime) {
        this.acceptanceTime = acceptanceTime;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getTextStart() {
        return textStart;
    }

    public void setTextStart(String textStart) {
        this.textStart = textStart;
    }

    public String getTextEnd() {
        return textEnd;
    }

    public void setTextEnd(String textEnd) {
        this.textEnd = textEnd;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(String userOrder) {
        this.userOrder = userOrder;
    }

    public String getUserInvoice() {
        return userInvoice;
    }

    public void setUserInvoice(String userInvoice) {
        this.userInvoice = userInvoice;
    }

    public BigDecimal getInvoiceNetAmount() {
        return invoiceNetAmount;
    }

    public void setInvoiceNetAmount(BigDecimal invoiceNetAmount) {
        this.invoiceNetAmount = invoiceNetAmount;
    }

    public BigDecimal getInvoiceBasisAustria() {
        return invoiceBasisAustria;
    }

    public void setInvoiceBasisAustria(BigDecimal invoiceBasisAustria) {
        this.invoiceBasisAustria = invoiceBasisAustria;
    }

    public BigDecimal getInvoiceBasisVat() {
        return invoiceBasisVat;
    }

    public void setInvoiceBasisVat(BigDecimal invoiceBasisVat) {
        this.invoiceBasisVat = invoiceBasisVat;
    }

    public BigDecimal getInvoiceVat() {
        return invoiceVat;
    }

    public void setInvoiceVat(BigDecimal invoiceVat) {
        this.invoiceVat = invoiceVat;
    }

    public BigDecimal getInvoiceVatAustria() {
        return invoiceVatAustria;
    }

    public void setInvoiceVatAustria(BigDecimal invoiceVatAustria) {
        this.invoiceVatAustria = invoiceVatAustria;
    }

    public BigDecimal getInvoiceTotalGross() {
        return invoiceTotalGross;
    }

    public void setInvoiceTotalGross(BigDecimal invoiceTotalGross) {
        this.invoiceTotalGross = invoiceTotalGross;
    }

    public String getEuSales() {
        return euSales;
    }

    public void setEuSales(String euSales) {
        this.euSales = euSales;
    }

    public String getTaxFreeThirdCountry() {
        return taxFreeThirdCountry;
    }

    public void setTaxFreeThirdCountry(String taxFreeThirdCountry) {
        this.taxFreeThirdCountry = taxFreeThirdCountry;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public BigDecimal getReserve1() {
        return reserve1;
    }

    public void setReserve1(BigDecimal reserve1) {
        this.reserve1 = reserve1;
    }

    public BigDecimal getReserve2() {
        return reserve2;
    }

    public void setReserve2(BigDecimal reserve2) {
        this.reserve2 = reserve2;
    }

    public String getWarrantyTakeover() {
        return warrantyTakeover;
    }

    public void setWarrantyTakeover(String warrantyTakeover) {
        this.warrantyTakeover = warrantyTakeover;
    }

    public Integer getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(Integer workshopId) {
        this.workshopId = workshopId;
    }

    public Integer getReserve3() {
        return reserve3;
    }

    public void setReserve3(Integer reserve3) {
        this.reserve3 = reserve3;
    }

    public Integer getReserve4() {
        return reserve4;
    }

    public void setReserve4(Integer reserve4) {
        this.reserve4 = reserve4;
    }

    public Integer getCountVGreaterZero() {
        return countVGreaterZero;
    }

    public void setCountVGreaterZero(Integer countVGreaterZero) {
        this.countVGreaterZero = countVGreaterZero;
    }

    public Integer getCountBGreaterZero() {
        return countBGreaterZero;
    }

    public void setCountBGreaterZero(Integer countBGreaterZero) {
        this.countBGreaterZero = countBGreaterZero;
    }

    public Integer getCampaignNumber() {
        return campaignNumber;
    }

    public void setCampaignNumber(Integer campaignNumber) {
        this.campaignNumber = campaignNumber;
    }

    public String getSpoOrder() {
        return spoOrder;
    }

    public void setSpoOrder(String spoOrder) {
        this.spoOrder = spoOrder;
    }

    public String getIdentifierAv() {
        return identifierAv;
    }

    public void setIdentifierAv(String identifierAv) {
        this.identifierAv = identifierAv;
    }

    public String getIdentifierPe() {
        return identifierPe;
    }

    public void setIdentifierPe(String identifierPe) {
        this.identifierPe = identifierPe;
    }

    public String getCostCalculation() {
        return costCalculation;
    }

    public void setCostCalculation(String costCalculation) {
        this.costCalculation = costCalculation;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }

    public String getAssistanceProcessNumber() {
        return assistanceProcessNumber;
    }

    public void setAssistanceProcessNumber(String assistanceProcessNumber) {
        this.assistanceProcessNumber = assistanceProcessNumber;
    }

    public String getAdditionalWarrantyValid() {
        return additionalWarrantyValid;
    }

    public void setAdditionalWarrantyValid(String additionalWarrantyValid) {
        this.additionalWarrantyValid = additionalWarrantyValid;
    }

    public String getRwApprovalNumber() {
        return rwApprovalNumber;
    }

    public void setRwApprovalNumber(String rwApprovalNumber) {
        this.rwApprovalNumber = rwApprovalNumber;
    }

    public Integer getGoodwillExtension() {
        return goodwillExtension;
    }

    public void setGoodwillExtension(Integer goodwillExtension) {
        this.goodwillExtension = goodwillExtension;
    }

    public String getGoodwillExceptionId() {
        return goodwillExceptionId;
    }

    public void setGoodwillExceptionId(String goodwillExceptionId) {
        this.goodwillExceptionId = goodwillExceptionId;
    }

    public String getGoodwillExceptionText() {
        return goodwillExceptionText;
    }

    public void setGoodwillExceptionText(String goodwillExceptionText) {
        this.goodwillExceptionText = goodwillExceptionText;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyManufacturer() {
        return bodyManufacturer;
    }

    public void setBodyManufacturer(String bodyManufacturer) {
        this.bodyManufacturer = bodyManufacturer;
    }

    public String getAdditionalEquipment1() {
        return additionalEquipment1;
    }

    public void setAdditionalEquipment1(String additionalEquipment1) {
        this.additionalEquipment1 = additionalEquipment1;
    }

    public String getAdditionalEquipmentManufacturer1() {
        return additionalEquipmentManufacturer1;
    }

    public void setAdditionalEquipmentManufacturer1(String additionalEquipmentManufacturer1) {
        this.additionalEquipmentManufacturer1 = additionalEquipmentManufacturer1;
    }

    public String getAdditionalEquipment2() {
        return additionalEquipment2;
    }

    public void setAdditionalEquipment2(String additionalEquipment2) {
        this.additionalEquipment2 = additionalEquipment2;
    }

    public String getAdditionalEquipmentManufacturer2() {
        return additionalEquipmentManufacturer2;
    }

    public void setAdditionalEquipmentManufacturer2(String additionalEquipmentManufacturer2) {
        this.additionalEquipmentManufacturer2 = additionalEquipmentManufacturer2;
    }

    public String getAdditionalEquipment3() {
        return additionalEquipment3;
    }

    public void setAdditionalEquipment3(String additionalEquipment3) {
        this.additionalEquipment3 = additionalEquipment3;
    }

    public String getAdditionalEquipmentManufacturer3() {
        return additionalEquipmentManufacturer3;
    }

    public void setAdditionalEquipmentManufacturer3(String additionalEquipmentManufacturer3) {
        this.additionalEquipmentManufacturer3 = additionalEquipmentManufacturer3;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getEuroStandard() {
        return euroStandard;
    }

    public void setEuroStandard(String euroStandard) {
        this.euroStandard = euroStandard;
    }

    public String getParticleFilter() {
        return particleFilter;
    }

    public void setParticleFilter(String particleFilter) {
        this.particleFilter = particleFilter;
    }

    public String getIsType() {
        return isType;
    }

    public void setIsType(String isType) {
        this.isType = isType;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailCc() {
        return mailCc;
    }

    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }
}