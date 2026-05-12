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

        print("Seeding database with sample analysis records...")
        
        # Base coordinates (e.g., a forest region like Yosemite or similar)
        base_lat = 37.75
        base_lon = -119.58

        records = []
        now = datetime.utcnow()

        for i in range(25):
            # Generate random coordinates within a small radius
            lat = base_lat + random.uniform(-0.1, 0.1)
            lon = base_lon + random.uniform(-0.1, 0.1)

            # Generate realistic varying data
            temp = random.uniform(15.0, 35.0)
            humidity = random.randint(20, 80)
            wind = random.uniform(0.0, 15.0)
            fuel = random.uniform(10.0, 80.0)
            
            # Make risk correlate somewhat with temp, inverse humidity, and wind
            risk_base = (temp / 35.0 * 30) + ((100 - humidity) / 100.0 * 30) + (wind / 15.0 * 20) + (fuel / 100.0 * 20)
            final_risk = min(max(risk_base + random.uniform(-5, 5), 0), 100)
            
            tier = 0
            if final_risk > 75: tier = 3
            elif final_risk > 50: tier = 2
            elif final_risk > 25: tier = 1

            # Distribute timestamps over the last 7 days
            timestamp = now - timedelta(days=random.uniform(0, 7), hours=random.uniform(0, 24))

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
        print(f"Successfully seeded {len(records)} records.")

    except Exception as e:
        print(f"An error occurred: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    seed_database()