package com.energypulse.backend.ai_module.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record PredictionRequest(

        @NotNull
        LocalDate date,

        @NotNull
        LocalTime time,

        @NotBlank
        String district,

        @NotBlank
        String province,

        @Positive
        double temperatureC,

        @PositiveOrZero
        @Max(100)
        double humidityPct,

        @PositiveOrZero
        double previousConsumptionKwh,

        @Min(1)
        int householdSize,

        @Min(0)
        @Max(1)
        int acUsage,

        @Min(0)
        @Max(1)
        int fanUsage

) {
}