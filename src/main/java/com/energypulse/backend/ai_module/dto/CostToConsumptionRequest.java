package com.energypulse.backend.ai_module.dto;

import jakarta.validation.constraints.Positive;

public record CostToConsumptionRequest(

        @Positive
        double amount,

        @Positive
        double usageHours

) {
}