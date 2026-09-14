package com.energypulse.backend.ai_module.controller;

import com.energypulse.backend.ai_module.dto.CostToConsumptionRequest;
import com.energypulse.backend.ai_module.dto.CostToConsumptionResponse;
import com.energypulse.backend.ai_module.dto.WattToCostRequest;
import com.energypulse.backend.ai_module.dto.WattToCostResponse;
import com.energypulse.backend.ai_module.service.ElectricityTariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/energy")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EnergyController {

    private final ElectricityTariffService tariffService;

    @PostMapping("/watt-to-cost")
    public ResponseEntity<WattToCostResponse>
    wattToCost(
            @Valid
            @RequestBody
            WattToCostRequest request
    ) {

        double kwh =
                (
                        request.watts()
                                * request.hours()
                ) / 1000.0;

        double cost =
                tariffService.calculateCost(
                        kwh
                );

        return ResponseEntity.ok(
                new WattToCostResponse(
                        request.watts(),
                        request.hours(),
                        round(kwh),
                        round(cost)
                )
        );
    }

    @PostMapping("/cost-to-consumption")
    public ResponseEntity<
            CostToConsumptionResponse>
    costToConsumption(
            @Valid
            @RequestBody
            CostToConsumptionRequest request
    ) {

        double kwh =
                tariffService
                        .calculateKwhFromAmount(
                                request.amount()
                        );

        double watts =
                (
                        kwh * 1000.0
                ) / request.usageHours();

        return ResponseEntity.ok(
                new CostToConsumptionResponse(
                        request.amount(),
                        round(kwh),
                        round(watts),
                        request.usageHours()
                )
        );
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100
        ) / 100.0;
    }
}