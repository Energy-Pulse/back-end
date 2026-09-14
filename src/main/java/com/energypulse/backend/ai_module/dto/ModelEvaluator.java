package com.energypulse.backend.ai_module.dto;

import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

@Component
public class ModelEvaluator {

    public Metrics evaluate(
            Classifier model,
            Instances testData
    ) throws Exception {

        double absoluteError = 0.0;

        double squaredError = 0.0;

        double actualMean = 0.0;

        int n = testData.numInstances();

        for (int i = 0; i < n; i++) {

            actualMean +=
                    testData.instance(i)
                            .classValue();
        }

        actualMean /= n;

        double totalSquaredError =
                0.0;

        for (int i = 0; i < n; i++) {

            Instance instance =
                    testData.instance(i);

            double actual =
                    instance.classValue();

            double predicted =
                    model.classifyInstance(
                            instance
                    );

            double error =
                    actual - predicted;

            absoluteError +=
                    Math.abs(error);

            squaredError +=
                    error * error;

            double deviation =
                    actual - actualMean;

            totalSquaredError +=
                    deviation * deviation;
        }

        double mae =
                absoluteError / n;

        double mse =
                squaredError / n;

        double rmse =
                Math.sqrt(mse);

        double r2;

        if (totalSquaredError == 0) {

            r2 = 0;

        } else {

            r2 =
                    1 -
                            (
                                    squaredError
                                            /
                                            totalSquaredError
                            );
        }

        return new Metrics(
                mae,
                mse,
                rmse,
                r2
        );
    }

    public record Metrics(
            double mae,
            double mse,
            double rmse,
            double r2
    ) {
    }
}