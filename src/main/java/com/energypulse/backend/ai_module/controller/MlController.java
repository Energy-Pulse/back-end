package com.energypulse.backend.ai_module.controller;

import com.energypulse.backend.ai_module.dto.ModelEvaluationResponse;
import com.energypulse.backend.ai_module.dto.PredictionRequest;
import com.energypulse.backend.ai_module.dto.PredictionResponse;
import com.energypulse.backend.ai_module.service.MlModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ml")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MlController {

    private final MlModelService mlModelService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse>
    predict(
            @Valid
            @RequestBody
            PredictionRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                mlModelService.predict(
                        request
                )
        );
    }

    @GetMapping("/evaluation")
    public ResponseEntity<
            List<ModelEvaluationResponse>>
    evaluation() {

        return ResponseEntity.ok(
                mlModelService
                        .getEvaluationResults()
        );
    }

    @GetMapping("/best-model")
    public ResponseEntity<String>
    bestModel() {

        return ResponseEntity.ok(
                mlModelService
                        .getBestModel()
        );
    }

    @GetMapping("/dataset-size")
    public ResponseEntity<Integer>
    datasetSize() throws Exception {

        return ResponseEntity.ok(
                mlModelService
                        .getDatasetSize()
        );
    }

    @PostMapping("/train")
    public ResponseEntity<String>
    train() throws Exception {

        mlModelService.trainModels();

        return ResponseEntity.ok(
                "ML models trained successfully. " +
                        "Best model: " +
                        mlModelService.getBestModel()
        );
    }
}