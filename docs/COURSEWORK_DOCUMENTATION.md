# EnergyPulse Backend – Coursework Documentation

## Scope
Standalone Spring Boot REST backend for the Machine Learning Module coursework. This version intentionally does **not** include Eureka, Config Server, API Gateway, GCP or other cloud/microservice-platform components.

## Coursework fit
- Real-world problem: hourly household electricity consumption prediction.
- Dataset: 36,000-record custom synthetic Sri Lankan household electricity dataset.
- Preprocessing: missing/blank checks, duplicate detection, type parsing, range validation and invalid-row rejection.
- Feature engineering: 28 engineered predictors using date/time extraction, weekend derivation, peak-hour binning, cyclical encoding, interaction features, polynomial transformation and one-hot district encoding.
- Models: Linear Regression, REPTree, Random Forest and Additive Regression.
- Evaluation: MAE, MSE, RMSE and R² on a chronological 80/20 split.
- Selection: highest holdout R².
- Integration: Spring Boot REST prediction endpoint.

## Dataset profile
- File: `src/main/resources/sri_lanka_household_electricity_36000.csv`
- Records: 36,000
- Total CSV columns: 13
- Raw predictors: 12
- Engineered predictors: 28
- Target: `electricity_consumption_kwh`
- Missing cells: 0
- Duplicate rows: 0
- Date range: 2025-01-01 to 2025-09-07
- Districts: Colombo, Galle, Gampaha, Kandy, Kegalle, Kurunegala

## Feature engineering
1. Date/time extraction: hour, day, month, day_of_week.
2. Weekend detection: is_weekend.
3. Peak-hour binning: is_peak_hour for 18:00–22:00.
4. Cyclical encoding: hour_sin/hour_cos and month_sin/month_cos.
5. Weather interaction: temperature_humidity.
6. Polynomial feature: temperature_squared.
7. Household × AC interaction.
8. Household × work-from-home interaction.
9. AC × fan interaction.
10. District one-hot encoding.

## ML lifecycle
The model is trained once at application startup. Prediction requests do not retrain the model. `POST /api/ml/train` explicitly retrains all models and selects the best model again.

## Key APIs
- `POST /api/ml/predict`
- `POST /api/ml/feature-engineering`
- `GET /api/ml/status`
- `GET /api/ml/dataset-profile`
- `GET /api/ml/dataset-report`
- `GET /api/ml/evaluation`
- `GET /api/ml/best-model`
- `GET /api/ml/history`
- `POST /api/ml/train`
- `GET /api/energy/tariff`
- `POST /api/energy/watt-to-cost`
- `POST /api/energy/cost-to-consumption`
- `GET /api/system/health`
- `GET /api/system/info`

## Cost calculation
The prediction output contains `estimatedCostLkr`. The default planning rate is 12.00 LKR/kWh and is configurable through `ENERGY_RATE_LKR_PER_KWH`. This is an hourly planning estimate, not an official monthly electricity bill.

## Viva evidence
Use `/ml/feature-engineering` to show the actual engineered values and `/ml/status` to show training state, dataset size, train/test counts and feature count. `/ml/evaluation` proves that multiple models were evaluated and `/ml/best-model` proves dynamic model selection.

## Important dataset note
The dataset is explicitly custom and synthetic. Do not claim it came from Kaggle, UCI or a government source. If a public dataset is substituted later, update the source and feature documentation.
