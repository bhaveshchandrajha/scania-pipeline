package com.scania.warranty.dto;

import com.scania.warranty.domain.ClaimSearchCriteria;
import java.util.List;

public record ClaimSearchResultDto(
    List<ClaimListItemDto> claims,
    int totalCount,
    ClaimSearchCriteria criteria
) {}