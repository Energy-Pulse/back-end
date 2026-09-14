package com.energypulse.backend.ai_module.dto;

import com.energypulse.backend.ai_module.config.MlConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DatasetLoader {

    public Instances loadDataset() throws Exception {

        Instances dataset =
                FeatureEngineer.createEmptyDataset(
                        "SmartEnergyDataset"
                );

        try (Reader reader =
                     new FileReader(
                             MlConfig.DATASET_PATH
                     );

             CSVParser parser =
                     CSVFormat.DEFAULT
                             .builder()
                             .setHeader()
                             .setSkipHeaderRecord(true)
                             .build()
                             .parse(reader)) {

            for (CSVRecord row : parser) {

                try {

                    String dateString =
                            row.get("date");

                    String timeString =
                            row.get("time");

                    LocalDate date =
                            LocalDate.parse(
                                    dateString
                            );

                    LocalTime time =
                            LocalTime.parse(
                                    timeString
                            );

                    double temperature =
                            parseDouble(
                                    row.get("temperature_c")
                            );

                    double humidity =
                            parseDouble(
                                    row.get("humidity_pct")
                            );

                    double previous =
                            parseDouble(
                                    row.get(
                                            "previous_consumption_kwh"
                                    )
                            );

                    int household =
                            parseInt(
                                    row.get(
                                            "household_size"
                                    )
                            );

                    int ac =
                            parseInt(
                                    row.get("ac_usage")
                            );

                    int fan =
                            parseInt(
                                    row.get("fan_usage")
                            );

                    double target =
                            parseDouble(
                                    row.get(
                                            "electricity_consumption_kwh"
                                    )
                            );

                    int hour =
                            time.getHour();

                    int day =
                            date.getDayOfMonth();

                    int month =
                            date.getMonthValue();

                    int dayOfWeek =
                            date.getDayOfWeek()
                                    .getValue() - 1;

                    int weekend =
                            dayOfWeek >= 5 ? 1 : 0;

                    int peakHour =
                            hour >= 18 && hour <= 22
                                    ? 1
                                    : 0;

                    double hourSin =
                            Math.sin(
                                    2 * Math.PI
                                            * hour
                                            / 24.0
                            );

                    double hourCos =
                            Math.cos(
                                    2 * Math.PI
                                            * hour
                                            / 24.0
                            );

                    double tempHumidity =
                            temperature * humidity;

                    double temperatureSquared =
                            temperature * temperature;

                    double householdAc =
                            household * ac;

                    double[] values = {

                            temperature,

                            humidity,

                            previous,

                            household,

                            ac,

                            fan,

                            hour,

                            day,

                            month,

                            dayOfWeek,

                            weekend,

                            peakHour,

                            hourSin,

                            hourCos,

                            tempHumidity,

                            temperatureSquared,

                            householdAc,

                            target

                    };

                    DenseInstance instance =
                            new DenseInstance(
                                    1.0,
                                    values
                            );

                    instance.setDataset(dataset);

                    dataset.add(instance);

                } catch (Exception ignored) {

                    // Invalid rows are skipped.
                }
            }
        }

        removeMissingValues(dataset);

        return dataset;
    }

    private double parseDouble(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return 0.0;
        }

        return Double.parseDouble(
                value.trim()
        );
    }

    private int parseInt(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return 0;
        }

        return Integer.parseInt(
                value.trim()
        );
    }

    private void removeMissingValues(
            Instances dataset
    ) {

        for (int i =
             dataset.numInstances() - 1;
             i >= 0;
             i--) {

            if (dataset.instance(i)
                    .hasMissingValue()) {

                dataset.delete(i);
            }
        }
    }
}