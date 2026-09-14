package com.energypulse.backend.ai_module.service;

import com.energypulse.backend.ai_module.dto.*;
import com.energypulse.backend.ai_module.utils.ModelType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Randomizable;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MlModelService {

    private final DatasetLoader datasetLoader;

    private final ModelEvaluator evaluator;

    private final ElectricityTariffService tariffService;

    private final Map<ModelType, Classifier> models =
            new EnumMap<>(ModelType.class);

    private final Map<ModelType, ModelEvaluator.Metrics> metrics =
            new EnumMap<>(ModelType.class);

    private ModelType bestModel;

    private Instances trainingHeader;

    @PostConstruct
    public void initialize() {

        try {

            trainModels();

        } catch (Exception e) {

            throw new RuntimeException(
                    "ML model initialization failed",
                    e
            );
        }
    }

    public synchronized void trainModels()
            throws Exception {

        Instances dataset =
                datasetLoader.loadDataset();

        if (dataset.numInstances() < 20) {

            throw new IllegalStateException(
                    "Dataset must contain at least 20 valid records."
            );
        }

        dataset.randomize(
                new Random(42)
        );

        int trainSize =
                (int) Math.round(
                        dataset.numInstances()
                                * 0.80
                );

        int testSize =
                dataset.numInstances()
                        - trainSize;

        Instances train =
                new Instances(
                        dataset,
                        0,
                        trainSize
                );

        Instances test =
                new Instances(
                        dataset,
                        trainSize,
                        testSize
                );

        this.trainingHeader =
                new Instances(
                        dataset,
                        0,
                        0
                );

        trainLinearRegression(
                train,
                test
        );

        trainDecisionTree(
                train,
                test
        );

        trainRandomForest(
                train,
                test
        );

        trainGradientBoosting(
                train,
                test
        );

        selectBestModel();
    }

    private void trainLinearRegression(
            Instances train,
            Instances test
    ) throws Exception {

        LinearRegression model =
                new LinearRegression();

        model.setEliminateColinearAttributes(
                true
        );

        model.buildClassifier(train);

        models.put(
                ModelType.LINEAR_REGRESSION,
                model
        );

        metrics.put(
                ModelType.LINEAR_REGRESSION,
                evaluator.evaluate(
                        model,
                        test
                )
        );
    }

    private void trainDecisionTree(
            Instances train,
            Instances test
    ) throws Exception {

        REPTree model =
                new REPTree();

        model.setMaxDepth(10);

        model.setMinNum(2);

        model.setSeed(42);

        model.buildClassifier(train);

        models.put(
                ModelType.DECISION_TREE,
                model
        );

        metrics.put(
                ModelType.DECISION_TREE,
                evaluator.evaluate(
                        model,
                        test
                )
        );
    }

    private void trainRandomForest(
            Instances train,
            Instances test
    ) throws Exception {

        RandomForest model =
                new RandomForest();

        model.setNumIterations(100);

        model.setNumFeatures(
                0
        );

        model.setSeed(42);

        model.buildClassifier(train);

        models.put(
                ModelType.RANDOM_FOREST,
                model
        );

        metrics.put(
                ModelType.RANDOM_FOREST,
                evaluator.evaluate(
                        model,
                        test
                )
        );
    }

    private void trainGradientBoosting(
            Instances train,
            Instances test
    ) throws Exception {

        REPTree baseTree =
                new REPTree();

        baseTree.setMaxDepth(5);

        baseTree.setMinNum(2);

        AdditiveRegression model =
                new AdditiveRegression();

        model.setClassifier(
                baseTree
        );

        model.setNumIterations(
                100
        );

        model.setShrinkage(
                0.05
        );

        model.buildClassifier(train);

        models.put(
                ModelType.GRADIENT_BOOSTING,
                model
        );

        metrics.put(
                ModelType.GRADIENT_BOOSTING,
                evaluator.evaluate(
                        model,
                        test
                )
        );
    }

    private void selectBestModel() {

        bestModel =
                metrics.entrySet()
                        .stream()
                        .max(
                                Comparator.comparingDouble(
                                        entry ->
                                                entry.getValue()
                                                        .r2()
                                )
                        )
                        .map(Map.Entry::getKey)
                        .orElse(
                                ModelType.RANDOM_FOREST
                        );
    }

    public PredictionResponse predict(
            PredictionRequest request
    ) throws Exception {

        Classifier model =
                models.get(bestModel);

        Instances structure =
                FeatureEngineer.createEmptyDataset(
                        "Prediction"
                );

        var instance =
                FeatureEngineer.transform(
                        request,
                        structure
                );

        double prediction =
                model.classifyInstance(
                        instance
                );

        prediction =
                Math.max(
                        0,
                        prediction
                );

        double cost =
                tariffService.calculateCost(
                        prediction
                );

        double r2 =
                metrics.get(bestModel)
                        .r2();

        return new PredictionResponse(
                round(prediction),
                round(cost),
                bestModel.name(),
                round(r2)
        );
    }

    public List<ModelEvaluationResponse>
    getEvaluationResults() {

        return metrics.entrySet()
                .stream()
                .map(entry -> {

                    ModelEvaluator.Metrics m =
                            entry.getValue();

                    return new ModelEvaluationResponse(
                            entry.getKey().name(),
                            round(m.mae()),
                            round(m.mse()),
                            round(m.rmse()),
                            round(m.r2())
                    );

                })
                .toList();
    }

    public String getBestModel() {

        return bestModel.name();
    }

    public int getDatasetSize()
            throws Exception {

        return datasetLoader
                .loadDataset()
                .numInstances();
    }

    private double round(double value) {

        return Math.round(
                value * 10000.0
        ) / 10000.0;
    }
}