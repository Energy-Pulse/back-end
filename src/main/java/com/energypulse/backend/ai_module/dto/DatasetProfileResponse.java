package com.energypulse.backend.ai_module.dto;

import java.util.List;

public record DatasetProfileResponse(
        String source,
        String file,
        int records,
        int rawFeatureCount,
        int engineeredFeatureCount,
        String targetVariable,
        String dateFrom,
        String dateTo,
        long missingCells,
        long duplicateRows,
        List<String> districts,
        List<String> provinces
) {}
