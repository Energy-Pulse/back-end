package com.energypulse.backend.ai_module.dto;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the transformation applied during both training
 * and prediction. Keeping this logic in one class prevents train/inference drift.
 */
public final class FeatureEngineer {

    private FeatureEngineer() {}

    public static final List<String> DISTRICTS = List.of(
            "Colombo", "Galle", "Gampaha", "Kandy", "Kegalle", "Kurunegala"
    );

    public static final String[] FEATURE_NAMES = {
            "temperature_c",
            "humidity_pct",
            "previous_consumption_kwh",
            "household_size",
            "ac_usage",
            "fan_usage",
            "work_from_home",
            "hour",
            "day",
            "month",
            "day_of_week",
            "is_weekend",
            "is_peak_hour",
            "hour_sin",
            "hour_cos",
            "month_sin",
            "month_cos",
            "temperature_humidity",
            "temperature_squared",
            "household_ac_interaction",
            "household_wfh_interaction",
            "ac_fan_interaction",
            "district_colombo",
            "district_galle",
            "district_gampaha",
            "district_kandy",
            "district_kegalle",
            "district_kurunegala"
    };

    public static ArrayList<Attribute> createAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (String feature : FEATURE_NAMES) {
            attributes.add(new Attribute(feature));
        }
        attributes.add(new Attribute("electricity_consumption_kwh"));
        return attributes;
    }

    public static Instances createEmptyDataset(String name) {
        Instances dataset = new Instances(name, createAttributes(), 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

    public static Instance transform(PredictionRequest request, Instances dataset) {
        return transform(
                request.date(), request.time(), request.district(),
                request.temperatureC(), request.humidityPct(),
                request.previousConsumptionKwh(), request.householdSize(),
                request.acUsage(), request.fanUsage(),
                request.workFromHome() == null ? 0 : request.workFromHome(),
                Utils.missingValue(), dataset
        );
    }

    public static Instance transform(
            LocalDate date,
            LocalTime time,
            String district,
            double temperatureC,
            double humidityPct,
            double previousConsumptionKwh,
            int householdSize,
            int acUsage,
            int fanUsage,
            int workFromHome,
            double target,
            Instances dataset) {

        int hour = time.getHour();
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue() - 1;
        int weekend = dayOfWeekValue >= 5 ? 1 : 0;
        int peakHour = hour >= 18 && hour <= 22 ? 1 : 0;

        double hourSin = Math.sin(2 * Math.PI * hour / 24.0);
        double hourCos = Math.cos(2 * Math.PI * hour / 24.0);
        double monthSin = Math.sin(2 * Math.PI * month / 12.0);
        double monthCos = Math.cos(2 * Math.PI * month / 12.0);
        double tempHumidity = temperatureC * humidityPct;
        double temperatureSquared = temperatureC * temperatureC;
        double householdAc = householdSize * acUsage;
        double householdWfh = householdSize * workFromHome;
        double acFan = acUsage * fanUsage;

        double[] values = {
                temperatureC, humidityPct, previousConsumptionKwh,
                householdSize, acUsage, fanUsage, workFromHome,
                hour, day, month, dayOfWeekValue, weekend, peakHour,
                hourSin, hourCos, monthSin, monthCos,
                tempHumidity, temperatureSquared,
                householdAc, householdWfh, acFan,
                districtFlag(district, "Colombo"),
                districtFlag(district, "Galle"),
                districtFlag(district, "Gampaha"),
                districtFlag(district, "Kandy"),
                districtFlag(district, "Kegalle"),
                districtFlag(district, "Kurunegala"),
                target
        };

        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(dataset);
        return instance;
    }

    public static Map<String, Double> preview(PredictionRequest request) {
        int hour = request.time().getHour();
        int day = request.date().getDayOfMonth();
        int month = request.date().getMonthValue();
        int dow = request.date().getDayOfWeek().getValue() - 1;
        int wfh = request.workFromHome() == null ? 0 : request.workFromHome();
        int weekend = dow >= 5 ? 1 : 0;
        int peak = hour >= 18 && hour <= 22 ? 1 : 0;
        Map<String, Double> result = new java.util.LinkedHashMap<>();
        result.put("temperature_c", request.temperatureC());
        result.put("humidity_pct", request.humidityPct());
        result.put("previous_consumption_kwh", request.previousConsumptionKwh());
        result.put("household_size", (double) request.householdSize());
        result.put("ac_usage", (double) request.acUsage());
        result.put("fan_usage", (double) request.fanUsage());
        result.put("work_from_home", (double) wfh);
        result.put("hour", (double) hour);
        result.put("day", (double) day);
        result.put("month", (double) month);
        result.put("day_of_week", (double) dow);
        result.put("is_weekend", (double) weekend);
        result.put("is_peak_hour", (double) peak);
        result.put("hour_sin", Math.sin(2 * Math.PI * hour / 24.0));
        result.put("hour_cos", Math.cos(2 * Math.PI * hour / 24.0));
        result.put("month_sin", Math.sin(2 * Math.PI * month / 12.0));
        result.put("month_cos", Math.cos(2 * Math.PI * month / 12.0));
        result.put("temperature_humidity", request.temperatureC() * request.humidityPct());
        result.put("temperature_squared", request.temperatureC() * request.temperatureC());
        result.put("household_ac_interaction", (double) request.householdSize() * request.acUsage());
        result.put("household_wfh_interaction", (double) request.householdSize() * wfh);
        result.put("ac_fan_interaction", (double) request.acUsage() * request.fanUsage());
        for (String district : DISTRICTS) {
            result.put("district_" + district.toLowerCase(), districtFlag(request.district(), district));
        }
        return result;
    }

    private static double districtFlag(String actual, String expected) {
        return expected.equalsIgnoreCase(actual == null ? "" : actual.trim()) ? 1.0 : 0.0;
    }
}
