package com.energypulse.backend.ai_module.dto;

import jakarta.validation.constraints.Positive;

public record WattToCostRequest(

        @Positive
        double watts,

        @Positive
        double hours

) {
}