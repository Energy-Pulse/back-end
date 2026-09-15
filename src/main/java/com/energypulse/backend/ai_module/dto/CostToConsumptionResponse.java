package com.energypulse.backend.ai_module.dto;

public record CostToConsumptionResponse(
        double amountLkr,
        double estimatedConsumptionKwh,
        double estimatedAverageWatts,
        double usageHours,
        double tariffRateLkrPerKwh,
        String currency
) {}
