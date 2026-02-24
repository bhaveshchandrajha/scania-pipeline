package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimConfiguration;
import com.scania.warranty.domain.FISTAM;
import com.scania.warranty.repository.AttachmentRepository;
import com.scania.warranty.repository.ClaimConfigurationRepository;
import com.scania.warranty.repository.ClaimPositionRepository;
import com.scania.warranty.repository.FISTAMRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ClaimManagementService {

    private final FISTAMRepository fistamRepository;
    private final ClaimConfigurationRepository claimConfigurationRepository;
    private final AttachmentRepository attachmentRepository;
    private final ClaimPositionRepository claimPositionRepository;

    public ClaimManagementService(FISTAMRepository fistamRepository,
                                   ClaimConfigurationRepository claimConfigurationRepository,
                                   AttachmentRepository attachmentRepository,
                                   ClaimPositionRepository claimPositionRepository) {
        this.fistamRepository = fistamRepository;
        this.claimConfigurationRepository = claimConfigurationRepository;
        this.attachmentRepository = attachmentRepository;
        this.claimPositionRepository = claimPositionRepository;
    }

    public Optional<FISTAM> getFirstDealer() {
        return fistamRepository.findFirstByOrderByHdlnrAsc();
    }

    public Optional<ClaimConfiguration> getDefaultConfiguration() {
        return claimConfigurationRepository.findDefaultConfiguration();
    }

    public Integer calculateMaxDaysForClaim() {
        Optional<ClaimConfiguration> config = getDefaultConfiguration();
        if (config.isPresent()) {
            BigDecimal maxAge = config.get().getMaxAgeOfClaimMths();
            if (maxAge.compareTo(BigDecimal.valueOf(3)) > 0) {
                return maxAge.intValue();
            } else {
                return maxAge.multiply(BigDecimal.valueOf(30)).intValue();
            }
        }
        return 28;
    }

    public long calculateDaysBetween(LocalDate repairDate, LocalDate currentDate) {
        return ChronoUnit.DAYS.between(repairDate, currentDate);
    }

    public boolean isClaimAgeExceeded(LocalDate repairDate, LocalDate currentDate, Integer maxDays) {
        long daysDifference = calculateDaysBetween(repairDate, currentDate);
        return daysDifference > maxDays;
    }

    @Transactional
    public void addAttachmentsToClaim(BigDecimal workTicketId, String dealerId, String claimNo, String failNo) {
        BigDecimal sid = attachmentRepository.findSidByWorkTicketId(workTicketId);
        if (sid != null && sid.compareTo(BigDecimal.ZERO) > 0) {
            attachmentRepository.insertAttachmentsForClaim(dealerId, claimNo, failNo, sid);
        }
    }

    @Transactional
    public void reNumberClaimPositions(String dealerId, String claimNo) {
        String positionList = claimPositionRepository.findPositionListForClaim(dealerId, claimNo);
        
        if (positionList == null || positionList.isBlank()) {
            return;
        }

        int index = 0;
        int currentPos = 0;
        int newPos = 0;
        int previousPos = -1;

        while (index < positionList.length()) {
            String lineNoStr = positionList.substring(index, Math.min(index + 3, positionList.length()));
            String posNoStr = positionList.substring(Math.min(index + 3, positionList.length()),
                    Math.min(index + 6, positionList.length()));

            if (lineNoStr.isBlank() || posNoStr.isBlank()) {
                break;
            }

            try {
                int lineNo = Integer.parseInt(lineNoStr.trim());
                currentPos = Integer.parseInt(posNoStr.trim());

                if (index == 0 || currentPos != previousPos) {
                    newPos++;
                    previousPos = currentPos;
                }

                claimPositionRepository.updatePositionNumber(dealerId, claimNo, lineNo, newPos);
            } catch (NumberFormatException e) {
                break;
            }

            index += 6;
        }
    }

    public String convertToUpperCase(String input) {
        return input != null ? input.toUpperCase() : "";
    }

    public boolean isMaintenanceOperation(String description) {
        if (description == null) {
            return false;
        }
        String upperDesc = convertToUpperCase(description);
        return upperDesc.contains("WARTUNG");
    }

    public boolean isValidClaimForProcessing(String claimStatus, String sdeDate) {
        if (claimStatus == null || sdeDate == null) {
            return false;
        }
        
        int status = Integer.parseInt(claimStatus.trim());
        boolean hasValidStatus = status < 20 && status != 5;
        boolean hasSdeDate = !sdeDate.equals("00000000");
        
        return hasValidStatus && hasSdeDate;
    }

    public boolean hasOpenPositions(String dealerId, String claimNo) {
        String positionList = claimPositionRepository.findPositionListForClaim(dealerId, claimNo);
        return positionList != null && !positionList.isBlank();
    }
}