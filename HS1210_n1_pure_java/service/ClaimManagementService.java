package com.scania.warranty.service;

import com.scania.warranty.domain.FISTAM;
import com.scania.warranty.domain.SystemConfiguration;
import com.scania.warranty.repository.FISTAMRepository;
import com.scania.warranty.repository.SystemConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class ClaimManagementService {

    private final FISTAMRepository fistamRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    public ClaimManagementService(
            FISTAMRepository fistamRepository,
            SystemConfigurationRepository systemConfigurationRepository) {
        this.fistamRepository = fistamRepository;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    public Optional<String> getDealerNumber() {
        return fistamRepository.findFirstByOrderByDealerNumberAsc()
                .map(FISTAM::getDealerNumber);
    }

    public int getMaxClaimAgeDays() {
        Optional<BigDecimal> maxAgeMonths = systemConfigurationRepository.findMaxAgeOfClaimMonths();
        if (maxAgeMonths.isPresent() && maxAgeMonths.get().compareTo(BigDecimal.valueOf(3)) > 0) {
            return maxAgeMonths.get().intValue();
        } else if (maxAgeMonths.isPresent()) {
            return maxAgeMonths.get().multiply(BigDecimal.valueOf(30)).intValue();
        }
        return 28;
    }

    public int getFilterDays() {
        int maxDays = getMaxClaimAgeDays();
        return maxDays - 6;
    }

    public boolean isClaimTooOld(LocalDate repairDate, LocalDate currentDate) {
        int maxDays = getMaxClaimAgeDays();
        long daysBetween = ChronoUnit.DAYS.between(repairDate, currentDate);
        return daysBetween > maxDays;
    }

    public boolean isClaimWithinSubmissionPeriod(LocalDate repairDate, LocalDate currentDate, int maxDays) {
        long daysBetween = ChronoUnit.DAYS.between(repairDate, currentDate);
        return daysBetween <= maxDays;
    }

    public String convertToUpperCase(String input) {
        if (input == null) {
            return null;
        }
        return input.toUpperCase();
    }

    public void updateSystemConfigurationValue(String key, String value) {
        systemConfigurationRepository.updateUpperCaseValue(key, value.toUpperCase());
    }
}