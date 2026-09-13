package com.energypulse.backend.ai_module.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MlConfig {

    public static final int RANDOM_SEED = 42;

    public static final double TEST_SIZE = 0.20;

    public static final String DATASET_PATH =
            "src/main/resources/kegalle_household_electricity_10000.csv";
}
