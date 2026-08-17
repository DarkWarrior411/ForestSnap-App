import random
import asyncio
from datetime import datetime, timedelta, timezone
from sqlalchemy import delete
from database import engine, Base, AsyncSessionLocal, AnalysisRecord

# Initialize table metadata synchronously if needed
Base.metadata.create_all(bind=engine)

# Geographic regions and environmental parameters for mock data generation
REGIONS = [
    {
        "name": "Bengaluru & Western Ghats",
        "center_lat": 13.03,
        "center_lon": 77.56,
        "spread_deg": 2.5,
        "points": 500,
        "temp_range": (22.0, 36.0),
        "hum_range": (30, 75),
        "wind_range": (2.0, 10.0),
        "fuel_range": (40.0, 85.0),
    },
    {
        "name": "California Wildfire Zones (USA)",
        "center_lat": 37.0,
        "center_lon": -120.0,
        "spread_deg": 4.0,
        "points": 500,
        "temp_range": (28.0, 42.0),
        "hum_range": (10, 25),
        "wind_range": (5.0, 15.0),
        "fuel_range": (70.0, 98.0),
    },
    {
        "name": "Amazon Rainforest (Brazil)",
        "center_lat": -3.46,
        "center_lon": -62.21,
        "spread_deg": 5.0,
        "points": 500,
        "temp_range": (26.0, 32.0),
        "hum_range": (75, 98),
        "wind_range": (1.0, 5.0),
        "fuel_range": (80.0, 100.0),
    },
    {
        "name": "Australian Outback & Bush",
        "center_lat": -31.25,
        "center_lon": 146.92,
        "spread_deg": 5.0,
        "points": 500,
        "temp_range": (30.0, 46.0),
        "hum_range": (5, 20),
        "wind_range": (8.0, 20.0),
        "fuel_range": (50.0, 95.0),
    },
    {
        "name": "Siberian Boreal Forests (Russia)",
        "center_lat": 61.0,
        "center_lon": 100.0,
        "spread_deg": 6.0,
        "points": 500,
        "temp_range": (-5.0, 15.0),
        "hum_range": (60, 85),
        "wind_range": (2.0, 8.0),
        "fuel_range": (30.0, 60.0),
    },
]


def calculate_risk(fuel_load, dryness_tier, temp, humidity, wind_speed):
    """Calculate composite fire risk index based on visual and environmental telemetry."""
    base_visual_risk = (dryness_tier / 3.0) * 100
    temp_mod = max(0, (temp - 20) * 1.5)
    hum_mod = max(0, (50 - humidity) * 0.8)
    wind_mod = wind_speed * 2.0

    final_risk = (
        (base_visual_risk * 0.4)
        + (fuel_load * 0.3)
        + (temp_mod + hum_mod + wind_mod) * 0.3
    )
    return round(min(max(final_risk, 0.0), 100.0), 2)


async def seed_database():
    """Populate the database with realistic global survey data for testing."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    async with AsyncSessionLocal() as db:
        try:
            print("Clearing old records...")
            await db.execute(delete(AnalysisRecord))
            await db.commit()

            print("Generating realistic geographic datasets...")
            records_to_insert = []
            now = datetime.now(timezone.utc)

            for region in REGIONS:
                print(f" -> Seeding {region['name']} ({region['points']} points)...")

                for _ in range(region["points"]):
                    lat = region["center_lat"] + random.uniform(
                        -region["spread_deg"], region["spread_deg"]
                    )
                    lon = region["center_lon"] + random.uniform(
                        -region["spread_deg"], region["spread_deg"]
                    )

                    temp = round(random.uniform(*region["temp_range"]), 1)
                    humidity = random.randint(*region["hum_range"])
                    wind_speed = round(random.uniform(*region["wind_range"]), 1)
                    wind_dir = random.randint(0, 359)

                    fuel_load = round(random.uniform(*region["fuel_range"]), 1)

                    if humidity < 30 and temp > 30:
                        dryness_tier = random.choice([2, 3])
                    elif humidity > 70:
                        dryness_tier = random.choice([0, 1])
                    else:
                        dryness_tier = random.randint(0, 3)

                    final_risk = calculate_risk(
                        fuel_load, dryness_tier, temp, humidity, wind_speed
                    )

                    days_ago = random.randint(0, 90)
                    hours_ago = random.randint(0, 23)
                    record_time = now - timedelta(days=days_ago, hours=hours_ago)

                    record = AnalysisRecord(
                        timestamp=record_time,
                        latitude=round(lat, 4),
                        longitude=round(lon, 4),
                        fuel_load_score=fuel_load,
                        dryness_risk_tier=dryness_tier,
                        temperature_c=temp,
                        humidity_percent=humidity,
                        wind_speed_ms=wind_speed,
                        wind_direction_deg=wind_dir,
                        final_fire_risk_percent=final_risk,
                    )
                    records_to_insert.append(record)

            print(f"Injecting {len(records_to_insert)} records into the database...")
            db.add_all(records_to_insert)
            await db.commit()
            print("✅ Database successfully seeded! Fire up the frontend.")

        except Exception as e:
            print(f"❌ Error seeding database: {e}")
            await db.rollback()


if __name__ == "__main__":
    asyncio.run(seed_database())
