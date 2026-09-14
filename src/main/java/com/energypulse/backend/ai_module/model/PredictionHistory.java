package com.energypulse.backend.ai_module.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.*;

        import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "prediction_history",
        indexes = {
                @Index(name = "idx_prediction_created_at", columnList = "created_at"),
                @Index(name = "idx_prediction_district", columnList = "district"),
                @Index(name = "idx_prediction_province", columnList = "province")
        }
)
public class PredictionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // Input Data
    // =========================

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(name = "temperature_c", nullable = false)
    private double temperatureC;

    @Column(name = "humidity_pct", nullable = false)
    private double humidityPct;

    @Column(name = "previous_consumption_kwh", nullable = false)
    private double previousConsumptionKwh;

    @Column(name = "household_size", nullable = false)
    private int householdSize;

    @Column(name = "ac_usage", nullable = false)
    private int acUsage;

    @Column(name = "fan_usage", nullable = false)
    private int fanUsage;

    // =========================
    // Prediction Data
    // =========================

    @Column(name = "predicted_consumption_kwh", nullable = false)
    private double predictedConsumptionKwh;

    @Column(name = "estimated_cost", nullable = false)
    private double estimatedCost;

    @Column(name = "selected_model", nullable = false, length = 100)
    private String selectedModel;

    @Column(name = "confidence_r2")
    private double confidenceR2;

    // =========================
    // Audit
    // =========================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // =========================
    // Constructors
    // =========================

    public PredictionHistory() {
    }

    // =========================
    // Getters and Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public double getHumidityPct() {
        return humidityPct;
    }

    public void setHumidityPct(double humidityPct) {
        this.humidityPct = humidityPct;
    }

    public double getPreviousConsumptionKwh() {
        return previousConsumptionKwh;
    }

    public void setPreviousConsumptionKwh(double previousConsumptionKwh) {
        this.previousConsumptionKwh = previousConsumptionKwh;
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int householdSize) {
        this.householdSize = householdSize;
    }

    public int getAcUsage() {
        return acUsage;
    }

    public void setAcUsage(int acUsage) {
        this.acUsage = acUsage;
    }

    public int getFanUsage() {
        return fanUsage;
    }

    public void setFanUsage(int fanUsage) {
        this.fanUsage = fanUsage;
    }

    public double getPredictedConsumptionKwh() {
        return predictedConsumptionKwh;
    }

    public void setPredictedConsumptionKwh(double predictedConsumptionKwh) {
        this.predictedConsumptionKwh = predictedConsumptionKwh;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }

    public double getConfidenceR2() {
        return confidenceR2;
    }

    public void setConfidenceR2(double confidenceR2) {
        this.confidenceR2 = confidenceR2;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}