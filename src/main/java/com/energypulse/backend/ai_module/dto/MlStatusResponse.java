package com.energypulse.backend.ai_module.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MlStatusResponse(
        boolean trained,
        LocalDateTime trainedAt,
        String bestModel,
        int datasetRows,
        int trainingRows,
        int testRows,
        int engineeredFeatureCount,
        List<String> models
) {}
