package com.energypulse.backend.controller;

import com.energypulse.backend.ai_module.service.MlModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {
    private final MlModelService mlModelService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "energypulse-backend",
                "timestamp", Instant.now(),
                "mlTrained", mlModelService.getStatus().trained()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
                "application", "EnergyPulse",
                "purpose", "Household electricity consumption prediction",
                "target", "electricity_consumption_kwh",
                "mlFeatures", 28,
                "models", mlModelService.getStatus().models()
        ));
    }
}
