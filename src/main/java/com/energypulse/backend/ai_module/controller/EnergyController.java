package com.energypulse.backend.ai_module.controller;

import com.energypulse.backend.ai_module.dto.*;
import com.energypulse.backend.ai_module.service.ElectricityTariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/energy")
@RequiredArgsConstructor
public class EnergyController {
    private final ElectricityTariffService tariffService;

    @GetMapping("/tariff")
    public ResponseEntity<TariffResponse> tariff() {
        return ResponseEntity.ok(new TariffResponse(
                "LKR", tariffService.getRateLkrPerKwh(),
                "Configurable application planning rate. It is not an official monthly utility bill tariff."
        ));
    }

    @PostMapping("/watt-to-cost")
    public ResponseEntity<WattToCostResponse> wattToCost(@Valid @RequestBody WattToCostRequest request) {
        double kwh = request.watts() * request.hours() / 1000.0;
        double cost = tariffService.calculateCost(kwh);
        return ResponseEntity.ok(new WattToCostResponse(
                request.watts(), request.hours(), round(kwh),
                tariffService.getRateLkrPerKwh(), round(cost), "LKR"
        ));
    }

    @PostMapping("/cost-to-consumption")
    public ResponseEntity<CostToConsumptionResponse> costToConsumption(@Valid @RequestBody CostToConsumptionRequest request) {
        double kwh = tariffService.calculateKwhFromAmount(request.amount());
        double watts = kwh * 1000.0 / request.usageHours();
        return ResponseEntity.ok(new CostToConsumptionResponse(
                request.amount(), round(kwh), round(watts), request.usageHours(),
                tariffService.getRateLkrPerKwh(), "LKR"
        ));
    }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
