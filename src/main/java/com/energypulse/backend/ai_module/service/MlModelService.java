package com.energypulse.backend.ai_module.service;

import com.energypulse.backend.ai_module.dto.*;
import com.energypulse.backend.ai_module.model.PredictionHistory;
import com.energypulse.backend.ai_module.repository.PredictionHistoryRepository;
import com.energypulse.backend.ai_module.utils.ModelType;
import com.energypulse.backend.model.User;
import com.energypulse.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MlModelService {

    private final DatasetLoader datasetLoader;
    private final ModelEvaluator evaluator;
    private final ElectricityTariffService tariffService;
    private final PredictionHistoryRepository predictionHistoryRepository;
    private final UserRepository userRepository;;

    private final Map<ModelType, Classifier> models = new EnumMap<>(ModelType.class);
    private final Map<ModelType, ModelEvaluator.Metrics> metrics = new EnumMap<>(ModelType.class);

    private volatile ModelType bestModel;
    private volatile Instances trainingHeader;
    private volatile LocalDateTime trainedAt;
    private volatile int datasetRows;
    private volatile int trainingRows;
    private volatile int testRows;

    @PostConstruct
    public void initialize() {
        try {
            trainModels();
        } catch (Exception e) {
            throw new IllegalStateException("ML model initialization failed", e);
        }
    }

    public synchronized void trainModels() throws Exception {
        Instances dataset = datasetLoader.loadDataset();
        if (dataset.numInstances() < 100) {
            throw new IllegalStateException("Dataset must contain at least 100 valid records.");
        }

        int trainSize = (int) Math.floor(dataset.numInstances() * 0.80);
        int testSize = dataset.numInstances() - trainSize;
        if (testSize < 20) throw new IllegalStateException("Test set is too small.");

        Instances train = new Instances(dataset, 0, trainSize);
        Instances test = new Instances(dataset, trainSize, testSize);
        this.trainingHeader = new Instances(dataset, 0, 0);
        this.models.clear();
        this.metrics.clear();

        trainLinearRegression(train, test);
        trainDecisionTree(train, test);
        trainRandomForest(train, test);
        trainGradientBoosting(train, test);
        selectBestModel();

        this.datasetRows = dataset.numInstances();
        this.trainingRows = trainSize;
        this.testRows = testSize;
        this.trainedAt = LocalDateTime.now();
    }

    private void trainLinearRegression(Instances train, Instances test) throws Exception {
        LinearRegression model = new LinearRegression();
        model.setEliminateColinearAttributes(true);
        model.setRidge(1.0e-8);
        model.buildClassifier(train);
        register(ModelType.LINEAR_REGRESSION, model, test);
    }

    private void trainDecisionTree(Instances train, Instances test) throws Exception {
        REPTree model = new REPTree();
        model.setMaxDepth(10);
        model.setMinNum(4);
        model.setSeed(42);
        model.buildClassifier(train);
        register(ModelType.DECISION_TREE, model, test);
    }

    private void trainRandomForest(Instances train, Instances test) throws Exception {
        RandomForest model = new RandomForest();
        model.setNumIterations(120);
        model.setNumFeatures(0);
        model.setSeed(42);
        model.buildClassifier(train);
        register(ModelType.RANDOM_FOREST, model, test);
    }

    private void trainGradientBoosting(Instances train, Instances test) throws Exception {
        REPTree baseTree = new REPTree();
        baseTree.setMaxDepth(5);
        baseTree.setMinNum(4);
        AdditiveRegression model = new AdditiveRegression();
        model.setClassifier(baseTree);
        model.setNumIterations(120);
        model.setShrinkage(0.05);
        model.buildClassifier(train);
        register(ModelType.GRADIENT_BOOSTING, model, test);
    }

    private void register(ModelType type, Classifier model, Instances test) throws Exception {
        models.put(type, model);
        metrics.put(type, evaluator.evaluate(model, test));
    }

    private void selectBestModel() {
        bestModel = metrics.entrySet().stream()
                .max(Comparator.comparingDouble(e -> safeR2(e.getValue().r2())))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No ML model was trained"));
    }

    @Transactional
    public PredictionResponse predict(PredictionRequest request) throws Exception {
        try {
            if (bestModel == null || trainingHeader == null) {
                throw new IllegalStateException("ML model is not trained yet");
            }
            validateLocation(request);
            Classifier model = models.get(bestModel);
            var instance = FeatureEngineer.transform(request, new Instances(trainingHeader, 0));
            double prediction = Math.max(0.0, model.classifyInstance(instance));
            double cost = tariffService.calculateCost(prediction);
            double r2 = metrics.get(bestModel).r2();

            double roundedPrediction = round4(prediction);
            double roundedCost = round2(cost);
            double roundedR2 = round4(r2);

            double monthlyEstimatedCostLkr =
                    prediction
                            * tariffService.getRateLkrPerKwh()
                            * 24
                            * 30;

            double roundedMonthlyCost = round2(monthlyEstimatedCostLkr);

//        get current user from security context

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("User is not authenticated");
            }

            String email = authentication.getName();

            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException(
                            "Authenticated user not found: " + email
                    ));

            PredictionHistory history = new PredictionHistory();
            history.setDate(request.date());
            history.setTime(request.time());
            history.setDistrict(request.district().trim());
            history.setProvince(request.province().trim());
            history.setTemperatureC(request.temperatureC());
            history.setHumidityPct(request.humidityPct());
            history.setPreviousConsumptionKwh(request.previousConsumptionKwh());
            history.setHouseholdSize(request.householdSize());
            history.setAcUsage(request.acUsage());
            history.setFanUsage(request.fanUsage());
            history.setWorkFromHome(request.workFromHome() == null ? 0 : request.workFromHome());
            history.setPredictedConsumptionKwh(roundedPrediction);
            history.setEstimatedCost(roundedCost);
            history.setSelectedModel(bestModel.name());
            history.setConfidenceR2(roundedR2);
            history.setCreatedAt(LocalDateTime.now());
            history.setMonthlyPredictedConsumptionKwh(roundedMonthlyCost);
            history.setUser(currentUser.getId()); // set user ID instead of User object
            predictionHistoryRepository.save(history);

            return new PredictionResponse(
                    roundedPrediction,
                    roundedCost,
                    tariffService.getRateLkrPerKwh(),
                    bestModel.name(),
                    roundedR2,
                    "Monthly estimate = predicted hourly kWh × tariff rate × 24 × 30.",
                    roundedMonthlyCost
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public FeatureEngineeringResponse previewFeatures(PredictionRequest request) {
        validateLocation(request);
        return new FeatureEngineeringResponse(
                request.district().trim(), request.province().trim(),
                FeatureEngineer.FEATURE_NAMES.length,
                FeatureEngineer.preview(request)
        );
    }

    public MlStatusResponse getStatus() {
        return new MlStatusResponse(
                bestModel != null,
                trainedAt,
                bestModel == null ? null : bestModel.name(),
                datasetRows,
                trainingRows,
                testRows,
                FeatureEngineer.FEATURE_NAMES.length,
                models.keySet().stream().map(Enum::name).toList()
        );
    }

    public DatasetProfileResponse getDatasetProfile() throws Exception {
        Instances dataset = datasetLoader.loadDataset();
        DatasetLoader.DatasetLoadReport report = datasetLoader.getLastReport();
        var districts = new TreeSet<String>();
        var provinces = new TreeSet<String>();
        var resource = new org.springframework.core.io.ClassPathResource(DatasetLoader.DATASET_RESOURCE);
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length > 4) { districts.add(p[3]); provinces.add(p[4]); }
            }
        }
        return new DatasetProfileResponse(
                "Custom synthetically generated Sri Lankan household electricity dataset for coursework demonstration",
                DatasetLoader.DATASET_RESOURCE,
                dataset.numInstances(),
                12,
                FeatureEngineer.FEATURE_NAMES.length,
                "electricity_consumption_kwh",
                "2025-01-01",
                "2025-09-07",
                report.missingCells(),
                report.duplicateRows(),
                List.copyOf(districts),
                List.copyOf(provinces)
        );
    }

    private void validateLocation(PredictionRequest request) {
        String district = request.district().trim();
        String province = request.province().trim();
        Map<String, String> allowed = Map.of(
                "Colombo", "Western", "Galle", "Southern", "Gampaha", "Western",
                "Kandy", "Central", "Kegalle", "Sabaragamuwa", "Kurunegala", "North Western"
        );
        String expected = allowed.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(district))
                .map(Map.Entry::getValue).findFirst().orElse(null);
        if (expected == null || !expected.equalsIgnoreCase(province)) {
            throw new IllegalArgumentException("Unsupported district/province. Supported locations: Colombo/Western, Galle/Southern, Gampaha/Western, Kandy/Central, Kegalle/Sabaragamuwa, Kurunegala/North Western.");
        }
    }

    public List<ModelEvaluationResponse> getEvaluationResults() {
        return metrics.entrySet().stream().map(entry -> {
            var m = entry.getValue();
            return new ModelEvaluationResponse(entry.getKey().name(), round4(m.mae()), round4(m.mse()), round4(m.rmse()), round4(m.r2()));
        }).toList();
    }

    public String getBestModel() {
        return bestModel == null ? "NOT_TRAINED" : bestModel.name();
    }

    public int getDatasetSize() { return datasetRows; }
    public DatasetLoader.DatasetLoadReport getDatasetReport() { return datasetLoader.getLastReport(); }
    public List<PredictionHistory> getPredictionHistory() { return predictionHistoryRepository.findAllByOrderByCreatedAtDesc(); }

    private static double safeR2(double value) { return Double.isFinite(value) ? value : Double.NEGATIVE_INFINITY; }
    private static double round2(double value) { return Math.round(value * 100.0) / 100.0; }
    private static double round4(double value) { return Math.round(value * 10000.0) / 10000.0; }

    public List<PredictionHistory> getPredictionHistoryByUserId(UUID userId) {

        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        return predictionHistoryRepository.findAllByUserOrderByCreatedAtDesc(user.getId());
    }
}
