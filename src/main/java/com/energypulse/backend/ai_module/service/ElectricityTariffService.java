package com.energypulse.backend.ai_module.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Energy-cost estimator used by the application.
 * This is intentionally a configurable planning rate rather than an official
 * monthly electricity bill calculator because utility tariffs can change and
 * real bills depend on billing-period slab rules and fixed charges.
 */
@Service
public class ElectricityTariffService {

    private final double rateLkrPerKwh;

    public ElectricityTariffService(
            @Value("${energy.tariff.rate-lkr-per-kwh:12.00}") double rateLkrPerKwh) {
        if (rateLkrPerKwh <= 0) throw new IllegalArgumentException("Tariff rate must be positive");
        this.rateLkrPerKwh = rateLkrPerKwh;
    }

    public double calculateCost(double kwh) {
        if (kwh <= 0) return 0;
        return round(kwh * rateLkrPerKwh);
    }

    public double calculateKwhFromAmount(double amount) {
        if (amount <= 0) return 0;
        return round(amount / rateLkrPerKwh);
    }

    public double getRateLkrPerKwh() { return rateLkrPerKwh; }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
