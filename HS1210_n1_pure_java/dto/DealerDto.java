package com.scania.warranty.dto;

public record DealerDto(
        String dealerNumber,
        String companyName1,
        String companyName2,
        String street,
        String postalCode,
        String city,
        String countryCode
) {
}