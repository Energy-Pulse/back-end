"""Generate the reproducible EnergyPulse coursework dataset.

This dataset is intentionally synthetic/custom. It must not be described as a
Kaggle/UCI/government dataset.
"""
import csv, math, random
from datetime import datetime, timedelta
from pathlib import Path

random.seed(42)
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/sri_lanka_household_electricity_36000.csv"
LOCATIONS = [
    ("Kegalle", "Sabaragamuwa", 0.0, 1.00),
    ("Colombo", "Western", 0.8, 1.08),
    ("Gampaha", "Western", 0.5, 1.04),
    ("Kandy", "Central", -0.7, 0.96),
    ("Kurunegala", "North Western", 0.2, 1.02),
    ("Galle", "Southern", 0.3, 1.01),
]
START = datetime(2025, 1, 1)
HOURS_PER_LOCATION = 6000

rows = []
for district, province, temp_bias, demand_factor in LOCATIONS:
    previous = 1.05 + random.uniform(-0.10, 0.10)
    household_values = [random.randint(2, 7) for _ in range(10)]
    for i in range(HOURS_PER_LOCATION):
        dt = START + timedelta(hours=i)
        h, doy, dow = dt.hour, dt.timetuple().tm_yday, dt.weekday()
        season = 0.9 * math.sin(2 * math.pi * (doy - 25) / 365.0)
        temperature = 26.5 + temp_bias + 2.1 * season + 1.7 * math.sin(2 * math.pi * (h - 14) / 24.0) + random.gauss(0, 0.7)
        humidity = 78 - 8 * season - 7 * math.sin(2 * math.pi * (h - 13) / 24.0) + random.gauss(0, 2.5)
        temperature = max(20, min(34, temperature))
        humidity = max(48, min(98, humidity))
        household = household_values[i % len(household_values)]
        evening = 18 <= h <= 22
        hot = temperature >= 28.2
        ac = int(hot and (evening or random.random() < 0.16) and household >= 3)
        fan = int(temperature >= 25.5 and not ac and (h >= 8 or h <= 23) and random.random() < 0.88)
        wfh = int(dow < 5 and random.random() < 0.22)
        weekend = int(dow >= 5)
        peak = int(evening)

        base = 0.42 + 0.075 * household + 0.18 * wfh
        time_load = 0.16 + 0.23 * (1 + math.sin(2 * math.pi * (h - 18) / 24.0)) + 0.18 * peak
        appliance = 0.38 * ac + 0.10 * fan
        weather = 0.045 * max(temperature - 24, 0) + 0.0025 * humidity + 0.0009 * temperature * humidity
        lifestyle = 0.10 * weekend + 0.07 * int(6 <= h <= 8)
        persistence = 0.31 * previous
        noise = random.gauss(0, 0.08)
        target = max(0.25, demand_factor * 0.47 * (base + time_load + appliance + weather + lifestyle + persistence) + noise)
        target = round(target, 4)

        rows.append([0, dt.date().isoformat(), dt.time().strftime("%H:%M:%S"), district, province,
                     round(temperature, 2), round(humidity, 2), round(previous, 4), household,
                     ac, fan, wfh, target])
        previous = target

rows.sort(key=lambda r: (r[1], r[2], r[3]))
for idx, row in enumerate(rows, 1):
    row[0] = idx

header = ["record_id", "date", "time", "district", "province", "temperature_c", "humidity_pct",
          "previous_consumption_kwh", "household_size", "ac_usage", "fan_usage", "work_from_home",
          "electricity_consumption_kwh"]
OUT.parent.mkdir(parents=True, exist_ok=True)
with OUT.open("w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(header)
    writer.writerows(rows)
print(f"Generated {len(rows):,} rows -> {OUT}")
