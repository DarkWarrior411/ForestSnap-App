import random
from datetime import datetime, timedelta
from database import SessionLocal, AnalysisRecord

def seed_database():
    db = SessionLocal()
    try:
        # Check if database already has records
        if db.query(AnalysisRecord).count() > 0:
            print("Database already has records. Deleting old records...")
            db.query(AnalysisRecord).delete()
            db.commit()

        print("Seeding database with sample analysis records across global forests...")

        # Define global forest regions with their typical climate characteristics
        # (latitude, longitude, temp_range, humidity_range, wind_range)
        regions = [
            {"name": "Amazon Rainforest, Brazil", "lat": -3.4653, "lon": -62.2159, "temp": (25, 35), "hum": (70, 95), "wind": (0, 5)},
            {"name": "Congo Basin, DRC", "lat": -1.4118, "lon": 23.5186, "temp": (24, 33), "hum": (65, 90), "wind": (0, 6)},
            {"name": "Boreal Forest, Canada", "lat": 56.1304, "lon": -106.3468, "temp": (5, 22), "hum": (30, 60), "wind": (2, 12)},
            {"name": "Taiga, Russia", "lat": 61.5240, "lon": 105.3188, "temp": (2, 18), "hum": (40, 70), "wind": (3, 10)},
            {"name": "Black Forest, Germany", "lat": 48.3366, "lon": 8.1633, "temp": (12, 25), "hum": (50, 80), "wind": (1, 8)},
            {"name": "Daintree Forest, Australia", "lat": -16.1700, "lon": 145.4185, "temp": (22, 32), "hum": (60, 85), "wind": (2, 9)},
            {"name": "Tongass National Forest, Alaska", "lat": 56.8833, "lon": -133.4000, "temp": (5, 15), "hum": (70, 95), "wind": (1, 10)},
            {"name": "Sierra Nevada, CA, USA", "lat": 37.75, "lon": -119.58, "temp": (20, 38), "hum": (10, 40), "wind": (2, 15)},
            {"name": "Mediterranean Forests, Spain", "lat": 39.5, "lon": -2.5, "temp": (22, 36), "hum": (20, 50), "wind": (1, 10)},
            {"name": "Western Ghats, India", "lat": 10.15, "lon": 77.01, "temp": (20, 30), "hum": (70, 90), "wind": (1, 6)},
            {"name": "Coconino National Forest, USA", "lat": 34.85, "lon": -111.48, "temp": (15, 30), "hum": (15, 45), "wind": (3, 14)},
            {"name": "Valdivian Temperate Forest, Chile", "lat": -39.81, "lon": -73.24, "temp": (10, 22), "hum": (60, 85), "wind": (2, 10)},
            {"name": "Sinharaja Forest Reserve, Sri Lanka", "lat": 6.40, "lon": 80.45, "temp": (25, 32), "hum": (75, 95), "wind": (1, 5)},
        ]

        records = []
        now = datetime.utcnow()

        for region in regions:
            # Generate 15-20 points per region
            num_points = random.randint(15, 20)
            for _ in range(num_points):
                # Scatter points around the base coordinates (up to ~100km away)
                lat = region["lat"] + random.uniform(-1.0, 1.0)
                lon = region["lon"] + random.uniform(-1.0, 1.0)

                temp = random.uniform(*region["temp"])
                humidity = random.randint(*region["hum"])
                wind = random.uniform(*region["wind"])
                fuel = random.uniform(10.0, 90.0)
                
                # Make risk correlate somewhat with temp, inverse humidity, and wind
                # Normalizing factors to create a 0-100 score
                temp_factor = max(0, min((temp - 5) / 35.0, 1.0)) * 30
                hum_factor = max(0, min((100 - humidity) / 100.0, 1.0)) * 30
                wind_factor = max(0, min(wind / 15.0, 1.0)) * 20
                fuel_factor = (fuel / 100.0) * 20
                
                risk_base = temp_factor + hum_factor + wind_factor + fuel_factor
                final_risk = min(max(risk_base + random.uniform(-5, 5), 0), 100)
                
                tier = 0
                if final_risk > 75: tier = 3
                elif final_risk > 50: tier = 2
                elif final_risk > 25: tier = 1

                # Distribute timestamps over the last 14 days
                timestamp = now - timedelta(days=random.uniform(0, 14), hours=random.uniform(0, 24))

                record = AnalysisRecord(
                    timestamp=timestamp,
                    latitude=lat,
                    longitude=lon,
                    fuel_load_score=fuel,
                    dryness_risk_tier=tier,
                    temperature_c=temp,
                    humidity_percent=humidity,
                    wind_speed_ms=wind,
                    final_fire_risk_percent=final_risk
                )
                records.append(record)

        db.add_all(records)
        db.commit()
        print(f"Successfully seeded {len(records)} records across {len(regions)} global regions.")

    except Exception as e:
        print(f"An error occurred: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    seed_database()