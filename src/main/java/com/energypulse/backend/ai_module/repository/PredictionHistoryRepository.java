package com.energypulse.backend.ai_module.repository;

import com.energypulse.backend.ai_module.model.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionHistoryRepository
        extends JpaRepository<PredictionHistory, Long> {

    List<PredictionHistory> findAllByOrderByCreatedAtDesc();

    List<PredictionHistory> findByDistrictOrderByCreatedAtDesc(
            String district
    );

    List<PredictionHistory> findByProvinceOrderByCreatedAtDesc(
            String province
    );

//    List<PredictionHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
List<PredictionHistory> findAllByUserOrderByCreatedAtDesc(Long userId);
}