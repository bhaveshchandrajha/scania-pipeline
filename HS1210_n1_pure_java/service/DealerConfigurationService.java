package com.scania.warranty.service;

import com.scania.warranty.domain.FISTAM;
import com.scania.warranty.domain.SystemConfiguration;
import com.scania.warranty.repository.FISTAMRepository;
import com.scania.warranty.repository.SystemConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DealerConfigurationService {

    private final FISTAMRepository fistamRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    public DealerConfigurationService(FISTAMRepository fistamRepository,
                                      SystemConfigurationRepository systemConfigurationRepository) {
        this.fistamRepository = fistamRepository;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    public Optional<String> getDefaultDealerId() {
        return fistamRepository.findFirstByOrderByDealerIdAsc()
                .map(FISTAM::getDealerId);
    }

    public int getMaxClaimAgeDays() {
        return systemConfigurationRepository.findDefaultConfiguration()
                .map(config -> {
                    BigDecimal maxAgeMonths = config.getMaxAgeOfClaimMonths();
                    if (maxAgeMonths.compareTo(BigDecimal.valueOf(3)) > 0) {
                        return maxAgeMonths.intValue();
                    } else {
                        return maxAgeMonths.multiply(BigDecimal.valueOf(30)).intValue();
                    }
                })
                .orElse(28);
    }

    public int getClaimFilterDays() {
        int maxDays = getMaxClaimAgeDays();
        return maxDays - 6;
    }

    public String toUpperCase(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return systemConfigurationRepository.toUpperCase(text);
    }

    public Optional<Long> findWorkTicketSid(Long workTicketId) {
        if (workTicketId == null || workTicketId == 0) {
            return Optional.empty();
        }
        Long sid = systemConfigurationRepository.findWorkTicketSidById(workTicketId);
        return Optional.ofNullable(sid);
    }

    public Optional<String> findAggregatedPositions(String dealerId, String claimNo) {
        if (dealerId == null || claimNo == null) {
            return Optional.empty();
        }
        String result = systemConfigurationRepository.findAggregatedPositionsByDealerAndClaim(dealerId, claimNo);
        return Optional.ofNullable(result);
    }
}