package com.energypulse.backend.ai_module.dto;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import weka.core.Instances;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class DatasetLoader {

    public static final String DATASET_RESOURCE = "sri_lanka_household_electricity_36000.csv";

    private static final Map<String, String> DISTRICT_PROVINCE = Map.of(
            "Colombo", "Western",
            "Galle", "Southern",
            "Gampaha", "Western",
            "Kandy", "Central",
            "Kegalle", "Sabaragamuwa",
            "Kurunegala", "North Western"
    );

    private volatile DatasetLoadReport lastReport = new DatasetLoadReport(0, 0, 0, 0, 0);

    public Instances loadDataset() throws Exception {
        Instances dataset = FeatureEngineer.createEmptyDataset("SriLankaHouseholdEnergy");
        int totalRows = 0;
        int invalidRows = 0;
        int duplicateRows = 0;
        long missingCells = 0;
        Set<String> fingerprints = new HashSet<>();

        ClassPathResource resource = new ClassPathResource(DATASET_RESOURCE);
        if (!resource.exists()) {
            throw new IllegalStateException("Dataset resource not found: " + DATASET_RESOURCE);
        }

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord row : parser) {
                totalRows++;
                String fingerprint = row.toString();
                if (!fingerprints.add(fingerprint)) {
                    duplicateRows++;
                    continue;
                }

                try {
                    String[] requiredColumns = {
                            "date", "time", "district", "province", "temperature_c", "humidity_pct",
                            "previous_consumption_kwh", "household_size", "ac_usage", "fan_usage",
                            "work_from_home", "electricity_consumption_kwh"
                    };
                    int rowMissingCells = 0;
                    for (String column : requiredColumns) {
                        if (row.get(column) == null || row.get(column).isBlank()) {
                            rowMissingCells++;
                        }
                    }
                    if (rowMissingCells > 0) {
                        missingCells += rowMissingCells;
                        throw new IllegalArgumentException("Missing required value(s)");
                    }

                    String dateString = row.get("date").trim();
                    String timeString = row.get("time").trim();
                    String district = row.get("district").trim();
                    String province = row.get("province").trim();
                    double temperature = Double.parseDouble(row.get("temperature_c").trim());
                    double humidity = Double.parseDouble(row.get("humidity_pct").trim());
                    double previous = Double.parseDouble(row.get("previous_consumption_kwh").trim());
                    int household = Integer.parseInt(row.get("household_size").trim());
                    int ac = Integer.parseInt(row.get("ac_usage").trim());
                    int fan = Integer.parseInt(row.get("fan_usage").trim());
                    int wfh = Integer.parseInt(row.get("work_from_home").trim());
                    double target = Double.parseDouble(row.get("electricity_consumption_kwh").trim());

                    LocalDate date = LocalDate.parse(dateString);
                    LocalTime time = LocalTime.parse(timeString);
                    validateTrainingRow(district, province, temperature, humidity, previous, household, ac, fan, wfh, target);

                    dataset.add(FeatureEngineer.transform(
                            date, time, district, temperature, humidity, previous,
                            household, ac, fan, wfh, target, dataset));
                } catch (RuntimeException ex) {
                    invalidRows++;
                }
            }
        }

        lastReport = new DatasetLoadReport(totalRows, dataset.numInstances(), invalidRows, duplicateRows, missingCells);
        return dataset;
    }

    public DatasetLoadReport getLastReport() { return lastReport; }

    private void validateTrainingRow(String district, String province, double temperature, double humidity,
                                     double previous, int household, int ac, int fan, int wfh, double target) {
        if (!DISTRICT_PROVINCE.containsKey(district) || !DISTRICT_PROVINCE.get(district).equalsIgnoreCase(province))
            throw new IllegalArgumentException("Invalid district/province combination");
        if (!Double.isFinite(temperature) || !Double.isFinite(humidity) || !Double.isFinite(previous) || !Double.isFinite(target))
            throw new IllegalArgumentException("Non-finite numeric value");
        if (temperature < 20 || temperature > 34 || humidity < 48 || humidity > 98 || previous < 0.5 || previous > 3.2
                || household < 2 || household > 7 || ac < 0 || ac > 1 || fan < 0 || fan > 1 || wfh < 0 || wfh > 1 || target <= 0)
            throw new IllegalArgumentException("Out-of-range training row");
    }

    public record DatasetLoadReport(int totalRows, int validRows, int invalidRows, int duplicateRows, long missingCells) {}
}
