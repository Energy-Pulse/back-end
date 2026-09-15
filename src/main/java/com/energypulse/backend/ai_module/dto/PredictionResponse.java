package com.energypulse.backend.ai_module.dto;

public record PredictionResponse(
        double predictedConsumptionKwh,
        double estimatedCostLkr,
        double tariffRateLkrPerKwh,
        String selectedModel,
        double confidenceR2,
        String costNote,
        double monthlyPredictedConsumptionKwhAmount
) {
    /** Backward-compatible alias used by the earlier frontend. */
    public double estimatedCost() {
        return estimatedCostLkr;
    }
}
