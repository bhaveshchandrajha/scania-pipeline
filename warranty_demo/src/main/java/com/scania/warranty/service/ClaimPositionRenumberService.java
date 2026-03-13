/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n2020}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.ClaimPositionData;
import com.scania.warranty.repository.ClaimPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for renumbering claim positions.
 */
@Service
public class ClaimPositionRenumberService {

    private final ClaimPositionRepository claimPositionRepository;

    public ClaimPositionRenumberService(ClaimPositionRepository claimPositionRepository) {
        this.claimPositionRepository = claimPositionRepository;
    }

    @Transactional
    public void reNumberPositions(String dealerId, String claimNo) {
        String aggregatedList = claimPositionRepository.findAggregatedPositionData(dealerId, claimNo);

        if (aggregatedList == null || aggregatedList.isBlank()) {
            return;
        }

        List<ClaimPositionData> positionDataList = parseAggregatedList(aggregatedList);

        int positionNew = 0;
        int positionWork = 1;
        int index = 0;

        while (index < positionDataList.size() && positionDataList.get(index) != null) {
            ClaimPositionData currentData = positionDataList.get(index);
            Integer lineNo = currentData.lineNo();
            Integer currentPosition = currentData.position();

            if (index == 0 || !currentPosition.equals(positionWork)) {
                positionWork = currentPosition;
                positionNew += 1;
            }

            claimPositionRepository.updatePosition(dealerId, claimNo, lineNo, positionNew);

            index += 1;
        }
    }

    private List<ClaimPositionData> parseAggregatedList(String aggregatedList) {
        List<ClaimPositionData> result = new ArrayList<>();

        if (aggregatedList == null || aggregatedList.length() < 6) {
            return result;
        }

        // @origin HS1210 L884-1012 (DOW)
        for (int i = 0; i + 6 <= aggregatedList.length(); i += 6) {
            String lineNoStr = aggregatedList.substring(i, i + 3);
            String positionStr = aggregatedList.substring(i + 3, i + 6);

            try {
                Integer lineNo = Integer.parseInt(lineNoStr.trim());
                Integer position = Integer.parseInt(positionStr.trim());
                result.add(new ClaimPositionData(lineNo, position));
            } catch (NumberFormatException e) {
                break;
            }
        }

        return result;
    }
}