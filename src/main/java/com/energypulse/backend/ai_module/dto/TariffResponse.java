package com.energypulse.backend.ai_module.dto;

public record TariffResponse(
        String currency,
        double rateLkrPerKwh,
        String note
) {}
