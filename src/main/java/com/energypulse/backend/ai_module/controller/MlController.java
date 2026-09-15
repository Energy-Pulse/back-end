package com.energypulse.backend.ai_module.controller;

import com.energypulse.backend.ai_module.dto.*;
import com.energypulse.backend.ai_module.model.PredictionHistory;
import com.energypulse.backend.ai_module.service.MlModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ml")
@RequiredArgsConstructor
public class MlController {
    private final MlModelService mlModelService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody PredictionRequest request) throws Exception {
       try {
           return ResponseEntity.ok(mlModelService.predict(request));
       }catch (Exception e) {
              e.printStackTrace();
              throw new RuntimeException(e);
       }
    }

    @PostMapping("/feature-engineering")
    public ResponseEntity<FeatureEngineeringResponse> featureEngineering(@Valid @RequestBody PredictionRequest request) {
        return ResponseEntity.ok(mlModelService.previewFeatures(request));
    }

    @GetMapping("/status")
    public ResponseEntity<MlStatusResponse> status() { return ResponseEntity.ok(mlModelService.getStatus()); }

    @GetMapping("/dataset-profile")
    public ResponseEntity<DatasetProfileResponse> datasetProfile() throws Exception { return ResponseEntity.ok(mlModelService.getDatasetProfile()); }

    @GetMapping("/evaluation")
    public ResponseEntity<List<ModelEvaluationResponse>> evaluation() { return ResponseEntity.ok(mlModelService.getEvaluationResults()); }

    @GetMapping("/best-model")
    public ResponseEntity<String> bestModel() { return ResponseEntity.ok(mlModelService.getBestModel()); }

    @GetMapping("/dataset-size")
    public ResponseEntity<Integer> datasetSize() { return ResponseEntity.ok(mlModelService.getDatasetSize()); }

    @GetMapping("/dataset-report")
    public ResponseEntity<DatasetLoader.DatasetLoadReport> datasetReport() { return ResponseEntity.ok(mlModelService.getDatasetReport()); }

    @GetMapping("/history")
    public ResponseEntity<List<PredictionHistory>> history() { return ResponseEntity.ok(mlModelService.getPredictionHistory()); }

    @PostMapping("/train")
    public ResponseEntity<MlStatusResponse> train() throws Exception {
        mlModelService.trainModels();
        return ResponseEntity.ok(mlModelService.getStatus());
    }

//    get hotory by user_id
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PredictionHistory>> historyByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(mlModelService.getPredictionHistoryByUserId(userId));
    }
}
