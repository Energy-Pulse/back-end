package com.energypulse.backend.ai_module.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MlConfig {
    public static final int RANDOM_SEED = 42;
    public static final double TRAINING_RATIO = 0.80;
    public static final String DATASET_RESOURCE = "sri_lanka_household_electricity_36000.csv";
}
