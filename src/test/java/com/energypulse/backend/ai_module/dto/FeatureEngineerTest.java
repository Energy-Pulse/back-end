package com.energypulse.backend.ai_module.dto;

import org.junit.jupiter.api.Test;
import weka.core.Instances;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FeatureEngineerTest {
    @Test
    void createsExpectedFeatureCount() {
        assertEquals(28, FeatureEngineer.FEATURE_NAMES.length);
        Instances dataset = FeatureEngineer.createEmptyDataset("test");
        assertEquals(29, dataset.numAttributes()); // 28 predictors + target
    }

    @Test
    void weekendPeakAndInteractionsAreGenerated() {
        var request = new PredictionRequest(
                LocalDate.of(2025, 8, 23), LocalTime.of(20, 0),
                "Kegalle", "Sabaragamuwa", 30.0, 75.0,
                2.0, 5, 1, 0, 1
        );
        var features = FeatureEngineer.preview(request);
        assertEquals(1.0, features.get("is_weekend"));
        assertEquals(1.0, features.get("is_peak_hour"));
        assertEquals(150.0, features.get("temperature_humidity"));
        assertEquals(900.0, features.get("temperature_squared"));
        assertEquals(5.0, features.get("household_ac_interaction"));
        assertEquals(1.0, features.get("district_kegalle"));
    }
}
