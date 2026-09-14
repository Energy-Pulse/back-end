package com.energypulse.backend.ai_module.dto;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FeatureEngineer {

    public static final String[] FEATURE_NAMES = {

            "temperature_c",
            "humidity_pct",
            "previous_consumption_kwh",

            "household_size",
            "ac_usage",
            "fan_usage",

            "hour",
            "day",
            "month",
            "day_of_week",
            "is_weekend",

            "is_peak_hour",

            "hour_sin",
            "hour_cos",

            "temperature_humidity",

            "temperature_squared",

            "household_ac_interaction"

    };

    public static ArrayList<Attribute> createAttributes() {

        ArrayList<Attribute> attributes = new ArrayList<>();

        for (String feature : FEATURE_NAMES) {
            attributes.add(new Attribute(feature));
        }

        attributes.add(
                new Attribute("electricity_consumption_kwh")
        );

        return attributes;
    }

    public static Instances createEmptyDataset(String name) {

        ArrayList<Attribute> attributes = createAttributes();

        Instances dataset =
                new Instances(name, attributes, 0);

        dataset.setClassIndex(
                dataset.numAttributes() - 1
        );

        return dataset;
    }

    public static Instance transform(
            PredictionRequest request,
            Instances dataset
    ) {

        LocalDate date = request.date();
        LocalTime time = request.time();

        int hour = time.getHour();

        int day = date.getDayOfMonth();

        int month = date.getMonthValue();

        DayOfWeek dayOfWeek =
                date.getDayOfWeek();

        int dayOfWeekValue =
                dayOfWeek.getValue() - 1;

        int weekend =
                dayOfWeekValue >= 5 ? 1 : 0;

        int peakHour =
                hour >= 18 && hour <= 22 ? 1 : 0;

        double hourSin =
                Math.sin(
                        2 * Math.PI * hour / 24.0
                );

        double hourCos =
                Math.cos(
                        2 * Math.PI * hour / 24.0
                );

        double tempHumidity =
                request.temperatureC()
                        * request.humidityPct();

        double temperatureSquared =
                Math.pow(
                        request.temperatureC(),
                        2
                );

        double householdAc =
                request.householdSize()
                        * request.acUsage();

        double[] values = {

                request.temperatureC(),

                request.humidityPct(),

                request.previousConsumptionKwh(),

                request.householdSize(),

                request.acUsage(),

                request.fanUsage(),

                hour,

                day,

                month,

                dayOfWeekValue,

                weekend,

                peakHour,

                hourSin,

                hourCos,

                tempHumidity,

                temperatureSquared,

                householdAc,

                0.0

        };

        Instance instance =
                new DenseInstance(1.0, values);

        instance.setDataset(dataset);

        return instance;
    }
}
