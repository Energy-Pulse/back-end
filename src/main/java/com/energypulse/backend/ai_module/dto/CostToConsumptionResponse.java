package com.energypulse.backend.ai_module.dto;

public record CostToConsumptionResponse(

        double amount,

        double estimatedConsumptionKwh,

        double estimatedAverageWatts,

        double usageHours

) {
}
