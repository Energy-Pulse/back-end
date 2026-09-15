package com.energypulse.backend.ai_module.dto;

public record WattToCostResponse(
        double watts,
        double hours,
        double consumptionKwh,
        double tariffRateLkrPerKwh,
        double estimatedCostLkr,
        String currency
) {}
