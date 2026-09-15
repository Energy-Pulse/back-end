package com.energypulse.backend.ai_module.dto;

import java.util.Map;

public record FeatureEngineeringResponse(
        String district,
        String province,
        int engineeredFeatureCount,
        Map<String, Double> features
) {}
