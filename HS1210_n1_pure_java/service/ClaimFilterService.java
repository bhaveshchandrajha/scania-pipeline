package com.scania.warranty.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class ClaimFilterService {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public boolean matchesSearchCriteria(String searchTerm, String... fields) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }

        String upperSearchTerm = searchTerm.toUpperCase().trim();

        for (String field : fields) {
            if (field != null && field.toUpperCase().contains(upperSearchTerm)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesStatusFilter(String claimStatus, String filterStatus, String filterOperator) {
        if (filterStatus == null || filterStatus.isBlank()) {
            return true;
        }

        if (filterOperator == null || filterOperator.isBlank()) {
            filterOperator = "=";
        }

        try {
            int status = Integer.parseInt(claimStatus.trim());
            int filter = Integer.parseInt(filterStatus.trim());

            switch (filterOperator) {
                case "=":
                case "*":
                    return status == filter;
                case ">":
                    return status > filter;
                case "<":
                    return status < filter;
                default:
                    return status == filter;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean matchesDateFilter(String claimDate, Integer maxDays) {
        if (maxDays == null || maxDays == 0) {
            return true;
        }

        if (claimDate == null || claimDate.isBlank() || claimDate.equals("00000000")) {
            return true;
        }

        try {
            LocalDate date = LocalDate.parse(claimDate, ISO_DATE_FORMATTER);
            LocalDate currentDate = LocalDate.now();
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(date, currentDate);
            return daysDifference <= maxDays;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean matchesDealerFilter(String claimDealer, String filterDealer, String defaultDealer) {
        if (filterDealer == null || filterDealer.isBlank()) {
            return true;
        }

        if (filterDealer.equals(defaultDealer)) {
            return true;
        }

        return claimDealer != null && claimDealer.equals(filterDealer);
    }

    public boolean matchesVehicleFilter(String vehicleNumber, String filterVehicle) {
        if (filterVehicle == null || filterVehicle.isBlank()) {
            return true;
        }

        return vehicleNumber != null && vehicleNumber.equals(filterVehicle);
    }

    public boolean matchesCustomerFilter(String customerNumber, String filterCustomer) {
        if (filterCustomer == null || filterCustomer.isBlank()) {
            return true;
        }

        return customerNumber != null && customerNumber.equals(filterCustomer);
    }

    public boolean matchesSdeFilter(String sdeDate, String filterSde) {
        if (filterSde == null || filterSde.isBlank()) {
            return true;
        }

        return sdeDate != null && sdeDate.equals(filterSde);
    }

    public boolean matchesMinimumFilter(boolean isMinimumClaim, String minimumFilter) {
        if (minimumFilter == null || !minimumFilter.equals("J")) {
            return true;
        }

        return isMinimumClaim;
    }

    public boolean matchesOpenClaimsFilter(boolean hasOpenPositions, String openFilter) {
        if (openFilter == null || !openFilter.equals("J")) {
            return true;
        }

        return hasOpenPositions;
    }

    public boolean isMinimumClaim(String sdeDate, String claimStatus) {
        if (sdeDate == null || claimStatus == null) {
            return false;
        }

        boolean hasNoSdeDate = sdeDate.equals("00000000");
        
        try {
            int status = Integer.parseInt(claimStatus.trim());
            boolean isMinimumStatus = status == 5 || status == 20;
            return hasNoSdeDate && isMinimumStatus;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String determineClaimColor(String claimStatus, String sdeDate, boolean hasErrors) {
        if (claimStatus == null) {
            return "";
        }

        try {
            int status = Integer.parseInt(claimStatus.trim());

            if (hasErrors && status > 2) {
                if (status == 16 || status == 30 || (status == 0 && sdeDate != null && !sdeDate.isBlank())) {
                    return "ROT";
                }

                if (status == 11) {
                    return "GELB";
                }

                if (status == 3 || status == 11) {
                    return "BLAU";
                }
            }

            if (status == 20 && (sdeDate == null || sdeDate.equals("00000000"))) {
                return "ROT";
            }

        } catch (NumberFormatException e) {
            return "";
        }

        return "";
    }
}