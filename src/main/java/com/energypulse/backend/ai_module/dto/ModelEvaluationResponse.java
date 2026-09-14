package com.energypulse.backend.ai_module.dto;

public record ModelEvaluationResponse(

        String model,

        double mae,

        double mse,

        double rmse,

        double r2

) {
}