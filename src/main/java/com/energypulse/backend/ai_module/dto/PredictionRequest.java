package com.energypulse.backend.ai_module.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record PredictionRequest(
        @NotNull(message = "date is required") LocalDate date,
        @NotNull(message = "time is required") LocalTime time,
        @NotBlank(message = "district is required") String district,
        @NotBlank(message = "province is required") String province,

        @DecimalMin(value = "20.0", message = "temperatureC must be between 20 and 34 C")
        @DecimalMax(value = "34.0", message = "temperatureC must be between 20 and 34 C")
        double temperatureC,

        @DecimalMin(value = "48.0", message = "humidityPct must be between 48 and 98")
        @DecimalMax(value = "98.0", message = "humidityPct must be between 48 and 98")
        double humidityPct,

        @DecimalMin(value = "0.5", message = "previousConsumptionKwh must be between 0.5 and 3.2")
        @DecimalMax(value = "3.2", message = "previousConsumptionKwh must be between 0.5 and 3.2")
        double previousConsumptionKwh,

        @Min(value = 2, message = "householdSize must be between 2 and 7")
        @Max(value = 7, message = "householdSize must be between 2 and 7")
        int householdSize,

        @Min(0) @Max(value = 1, message = "acUsage must be 0 or 1") int acUsage,
        @Min(0) @Max(value = 1, message = "fanUsage must be 0 or 1") int fanUsage,

        @Min(0) @Max(value = 1, message = "workFromHome must be 0 or 1") Integer workFromHome
) {}
