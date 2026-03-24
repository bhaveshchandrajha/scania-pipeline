package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimPosition;
import com.scania.warranty.repository.ClaimPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClaimValueService {

    private final ClaimPositionRepository claimPositionRepository;

    public ClaimValueService(ClaimPositionRepository claimPositionRepository) {
        this.claimPositionRepository = claimPositionRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getClaimValues(
        String valueType,
        String kuerzel,
        String claimNumber,
        String errorNumber,
        String sequenceNumber
    ) {
        List<ClaimPosition> positions = claimPositionRepository.findByClaimKey(
            kuerzel,
            claimNumber,
            errorNumber
        );

        if ("Requested".equalsIgnoreCase(valueType)) {
            return positions.stream()
                .map(ClaimPosition::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return BigDecimal.ZERO;
    }
}