package com.scania.warranty.domain;

public record ClaimSearchCriteria(
    String dealerId,
    String invoiceNumber,
    String invoiceDate,
    String orderNumber,
    String claimType,
    String chassisNumber,
    String registrationNumber,
    String mainGroup,
    String damageCode,
    String controlCode,
    Integer statusFrom,
    Integer statusTo
) {
    public ClaimSearchCriteria {
        if (dealerId == null) {
            dealerId = "";
        }
        if (invoiceNumber == null) {
            invoiceNumber = "";
        }
        if (invoiceDate == null) {
            invoiceDate = "";
        }
        if (orderNumber == null) {
            orderNumber = "";
        }
        if (claimType == null) {
            claimType = "";
        }
        if (chassisNumber == null) {
            chassisNumber = "";
        }
        if (registrationNumber == null) {
            registrationNumber = "";
        }
        if (mainGroup == null) {
            mainGroup = "";
        }
        if (damageCode == null) {
            damageCode = "";
        }
        if (controlCode == null) {
            controlCode = "";
        }
        if (statusFrom == null) {
            statusFrom = 0;
        }
        if (statusTo == null) {
            statusTo = 99;
        }
    }
}