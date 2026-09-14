package com.energypulse.backend.ai_module.dto;

public record PredictionResponse(

        double predictedConsumptionKwh,

        double estimatedCost,

        String selectedModel,

        double confidenceR2

) {
}