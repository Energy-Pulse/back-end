# EnergyPulse Backend

A complete Spring Boot backend for the Machine Learning Module coursework project **EnergyPulse – Household Electricity Consumption Prediction**.

This version is based on the previously fixed EnergyPulse backend, but it has been redesigned around a larger, multi-location dataset and a clearer ML pipeline so the implementation can be explained directly during the viva.

> Scope: **ML + REST backend only.** No Eureka, Config Server, API Gateway, Cloud Run, GCP or microservice platform is required for this project.

## 1. Coursework Alignment

The assignment requires a real-world ML problem, a suitable dataset, preprocessing, feature engineering, model development/evaluation, model selection and integration into a working full-stack application.

EnergyPulse implements the backend portion of that flow:

```text
Frontend Application
        |
        | REST/JSON
        v
Spring Boot Backend
        |
        +--> Authentication / User API
        |
        +--> ML Prediction Service
        |       |
        |       +--> Dataset validation
        |       +--> Feature engineering
        |       +--> Train/test split
        |       +--> Model training
        |       +--> Evaluation
        |       +--> Best-model selection
        |
        +--> PostgreSQL prediction history
        |
        +--> Energy cost utilities
```

## 2. Problem Statement

Household electricity consumption varies according to time of day, weather, household size, appliance usage and recent consumption. EnergyPulse predicts the expected electricity consumption in **kWh for the supplied hour**.

Target variable:

```text
electricity_consumption_kwh
```

The prediction is not a monthly electricity bill. The backend can additionally estimate the energy cost of the predicted kWh using a configurable LKR planning rate.

## 3. Dataset

File:

```text
src/main/resources/sri_lanka_household_electricity_36000.csv
```

Source description:

```text
Custom synthetically generated Sri Lankan household electricity dataset
created for coursework demonstration and reproducible ML experiments.
```

The dataset is intentionally transparent about being synthetic. It is not presented as a Kaggle/UCI dataset.

### Dataset summary

| Item | Value |
|---|---:|
| Records | 36,000 |
| Total CSV columns | 13 |
| Raw predictor columns | 12 |
| Engineered ML features | 28 |
| Target | electricity_consumption_kwh |
| Missing cells | 0 |
| Duplicate rows | 0 |
| Date range | 2025-01-01 to 2025-09-07 |
| Districts | 6 |
| Provinces | 5 |

### Locations

- Colombo – Western
- Galle – Southern
- Gampaha – Western
- Kandy – Central
- Kegalle – Sabaragamuwa
- Kurunegala – North Western

## 4. Raw Features

| Feature | Type | Description |
|---|---|---|
| record_id | Integer | Unique record identifier; excluded from ML |
| date | Date | Observation date |
| time | Time | Observation hour |
| district | Categorical | Sri Lankan district |
| province | Categorical | Province corresponding to district |
| temperature_c | Numeric | Temperature in Celsius |
| humidity_pct | Numeric | Relative humidity percentage |
| previous_consumption_kwh | Numeric | Previous observed household consumption |
| household_size | Integer | Number of household members |
| ac_usage | Binary | Air-conditioner usage: 0/1 |
| fan_usage | Binary | Fan usage: 0/1 |
| work_from_home | Binary | Work-from-home indicator: 0/1 |
| electricity_consumption_kwh | Numeric target | Consumption for the observation hour |

## 5. Data Preprocessing / Quality Handling

The backend does not silently turn bad values into zero.

During dataset loading it:

1. Reads the CSV from the application classpath.
2. Detects duplicate rows using a row fingerprint.
3. Checks required fields for blank/missing values.
4. Parses dates, times and numeric values.
5. Validates temperature, humidity, previous consumption, household size and binary appliance fields.
6. Validates district/province combinations.
7. Rejects invalid records instead of training on corrupted values.
8. Reports total, valid, invalid, duplicate and missing-cell counts.

For the supplied dataset, the quality report is expected to show **0 missing cells and 0 duplicate rows**. The code still demonstrates how these cases are handled.

## 6. Feature Engineering – Mandatory Requirement

EnergyPulse uses more than the required 5–6 meaningful techniques.

### 6.1 Date/time extraction

From `date` and `time`:

- hour
- day
- month
- day_of_week

### 6.2 Weekend feature

```text
is_weekend = 1 when day_of_week >= Saturday
```

### 6.3 Peak-hour binning

```text
is_peak_hour = 1 for 18:00–22:00
```

This represents the high-demand evening period.

### 6.4 Cyclical encoding

Hour and month are cyclic, so sine/cosine features are created:

```text
hour_sin
hour_cos
month_sin
month_cos
```

### 6.5 Feature interaction

```text
temperature_humidity = temperature_c × humidity_pct
```

### 6.6 Polynomial feature

```text
temperature_squared = temperature_c²
```

### 6.7 Household/appliance interaction

```text
household_ac_interaction = household_size × ac_usage
```

### 6.8 Household/work interaction

```text
household_wfh_interaction = household_size × work_from_home
```

### 6.9 Appliance interaction

```text
ac_fan_interaction = ac_usage × fan_usage
```

### 6.10 Categorical encoding

District is transformed into one-hot numeric features:

```text
district_colombo
district_galle
district_gampaha
district_kandy
district_kegalle
district_kurunegala
```

### Final engineered feature list

```text
1  temperature_c
2  humidity_pct
3  previous_consumption_kwh
4  household_size
5  ac_usage
6  fan_usage
7  work_from_home
8  hour
9  day
10 month
11 day_of_week
12 is_weekend
13 is_peak_hour
14 hour_sin
15 hour_cos
16 month_sin
17 month_cos
18 temperature_humidity
19 temperature_squared
20 household_ac_interaction
21 household_wfh_interaction
22 ac_fan_interaction
23 district_colombo
24 district_galle
25 district_gampaha
26 district_kandy
27 district_kegalle
28 district_kurunegala
```

## 7. ML Training Pipeline

```text
CSV dataset
   |
   v
Validation + duplicate/missing checks
   |
   v
Feature Engineering (28 features)
   |
   v
Chronological 80/20 split
   |
   +------------------+-------------------+-------------------+
   |                  |                   |                   |
Linear Regression  REPTree          Random Forest      Additive Regression
   |                  |                   |                   |
   +------------------+-------------------+-------------------+
                              |
                              v
                    MAE / MSE / RMSE / R²
                              |
                              v
                    Highest R² = Best Model
                              |
                              v
                         Prediction API
```

### Why chronological split?

Electricity data is time ordered. The first 80% is used for training and the final 20% is held out for evaluation. This avoids randomly mixing future observations into the training set.

## 8. Models

Four regression models are trained:

1. Linear Regression
2. REPTree Decision Tree
3. Random Forest
4. Additive Regression with REPTree base learners (gradient-boosting style ensemble)

The backend does not hard-code a winner. It evaluates every model and selects the model with the highest holdout R².

## 9. Model Evaluation

The evaluation endpoint returns:

- MAE – Mean Absolute Error
- MSE – Mean Squared Error
- RMSE – Root Mean Squared Error
- R² – coefficient of determination

The values are calculated from the final 20% holdout set.

Use:

```text
GET /api/ml/evaluation
```

## 10. Training Lifecycle

The model is trained automatically once when the Spring Boot application starts.

It is **not retrained for every prediction request**.

Manual retraining is available through:

```text
POST /api/ml/train
```

This reloads the dataset, redoes preprocessing and feature engineering, trains all four models, evaluates them and selects the best model again.

## 11. Prediction API

```text
POST /api/ml/predict
Authorization: Bearer <JWT>
```

Example:

```json
{
  "date": "2025-08-20",
  "time": "20:00:00",
  "district": "Kegalle",
  "province": "Sabaragamuwa",
  "temperatureC": 29.5,
  "humidityPct": 76.0,
  "previousConsumptionKwh": 2.05,
  "householdSize": 5,
  "acUsage": 1,
  "fanUsage": 0,
  "workFromHome": 0
}
```

Example response shape:

```json
{
  "predictedConsumptionKwh": 2.31,
  "estimatedCostLkr": 27.72,
  "tariffRateLkrPerKwh": 12.0,
  "selectedModel": "LINEAR_REGRESSION",
  "confidenceR2": 0.87,
  "costNote": "Hourly energy-cost estimate using the configured planning rate; not a monthly utility bill."
}
```

The exact prediction and selected model depend on the runtime Weka training result.

## 12. Feature Engineering Demonstration API

For viva/demo purposes:

```text
POST /api/ml/feature-engineering
```

It returns the 28 engineered values generated from the same input used by the prediction pipeline.

This makes it possible to demonstrate that feature engineering is actually implemented, rather than only describing it in the report.

## 13. ML Status API

```text
GET /api/ml/status
```

Returns whether the model is trained, training time, best model, dataset size, train/test sizes and feature count.

## 14. Dataset Profile API

```text
GET /api/ml/dataset-profile
```

Returns dataset source description, record count, raw/engineered feature counts, target variable, date range, missing/duplicate counts and locations.

## 15. Prediction History

Every successful prediction is saved to PostgreSQL.

```text
GET /api/ml/history
```

Database table:

```text
prediction_history
```

Stored information includes input values, predicted kWh, estimated LKR cost, selected model, R² and creation timestamp.

## 16. Authentication

Public:

```text
POST /api/users/v1/signup
POST /api/users/v1/login
POST /api/contacts/submit
```

Protected:

```text
/api/ml/**
/api/energy/**
```

Send:

```text
Authorization: Bearer <JWT_TOKEN>
```

Passwords are stored using BCrypt hashing.

## 17. Energy Utility APIs

### Tariff configuration

```text
GET /api/energy/tariff
```

The default application planning rate is:

```text
12.00 LKR / kWh
```

It can be changed with:

```text
ENERGY_RATE_LKR_PER_KWH
```

This is deliberately described as a **planning rate**, not an official monthly utility tariff. A real utility bill depends on billing period, tariff category, progressive slabs and fixed charges.

### Watts to cost

```text
POST /api/energy/watt-to-cost
```

Formula:

```text
kWh = watts × hours / 1000
cost = kWh × configured LKR/kWh rate
```

### Cost to consumption

```text
POST /api/energy/cost-to-consumption
```

This is the reverse planning estimate using the same configured rate.

## 18. Database

PostgreSQL is used for application data and prediction history.

Create the database:

```sql
CREATE DATABASE energy_pulse;
```

Hibernate automatically creates/updates the tables during development.

## 19. Configuration

Default:

```text
DB_URL=jdbc:postgresql://localhost:5432/energy_pulse
DB_USERNAME=postgres
DB_PASSWORD=1234
SERVER_PORT=8080
ENERGY_RATE_LKR_PER_KWH=12.00
JWT_EXPIRATION=86400000
```

For real deployment, use environment variables instead of committing credentials.

## 20. Run the Backend

Requirements:

- Java 25
- PostgreSQL
- Internet access for the first Maven dependency download

Create database:

```sql
CREATE DATABASE energy_pulse;
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Base URL:

```text
http://localhost:8080/api
```

Swagger:

```text
http://localhost:8080/api/swagger-ui.html
```

## 21. Recommended Demo Order

1. Signup
2. Login and copy JWT
3. Open Swagger
4. Authorize with the JWT
5. `GET /ml/status`
6. `GET /ml/dataset-profile`
7. `GET /ml/dataset-report`
8. `GET /ml/evaluation`
9. `GET /ml/best-model`
10. `POST /ml/feature-engineering`
11. `POST /ml/predict`
12. `GET /ml/history`
13. `POST /ml/train`
14. Repeat evaluation and show that the best model is selected from measured R²
15. Demonstrate `/energy/tariff` and `/energy/watt-to-cost`

## 22. Important Viva Answers

### What is the problem?

Predict hourly household electricity consumption in kWh using historical consumption, household information, appliance usage, weather and time-related information.

### What is the target?

`electricity_consumption_kwh`.

### What preprocessing did you perform?

Missing-value checking, duplicate detection, type conversion, range validation and invalid-row rejection.

### What feature engineering did you perform?

Date/time extraction, weekend detection, peak-hour binning, cyclical hour/month encoding, interaction features, polynomial temperature feature, household/appliance interactions and one-hot district encoding.

### How many engineered features?

28.

### How did you split the data?

Chronologically: 80% training and 20% testing.

### Which models did you compare?

Linear Regression, REPTree, Random Forest and Additive Regression.

### How did you select the final model?

The model with the highest holdout R² is selected.

### Does the model train for every prediction?

No. It trains once at application startup. `/ml/predict` only performs inference. `/ml/train` performs an explicit retraining operation.

### Is the estimated cost a monthly electricity bill?

No. It is an hourly energy-cost estimate using the configured LKR/kWh planning rate.

## 23. Project Structure

```text
back-end/
├── pom.xml
├── README.md
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/energypulse/backend/
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   ├── config/
    │   │   └── ai_module/
    │   │       ├── controller/
    │   │       ├── dto/
    │   │       ├── model/
    │   │       ├── repository/
    │   │       ├── service/
    │   │       └── utils/
    │   └── resources/
    │       ├── application.yaml
    │       └── sri_lanka_household_electricity_36000.csv
    └── test/
```

## 24. Academic Integrity Note

The dataset is explicitly labelled synthetic/custom. Do not describe it as Kaggle, UCI or government data unless you replace it with a real source and update the documentation.

The project is designed so the dataset, preprocessing, feature engineering, training, evaluation and prediction logic can all be demonstrated directly in the code and API.
