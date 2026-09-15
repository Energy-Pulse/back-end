package com.energypulse.backend.ai_module.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElectricityTariffServiceTest {
    @Test
    void calculatesConfiguredLkrRate() {
        var service = new ElectricityTariffService(12.0);
        assertEquals(24.0, service.calculateCost(2.0));
        assertEquals(2.0, service.calculateKwhFromAmount(24.0));
    }
}
