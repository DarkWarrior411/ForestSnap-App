import os
import cv2
import numpy as np
import onnxruntime as ort
import httpx
from fastapi import (
    FastAPI,
    UploadFile,
    File,
    Form,
    HTTPException,
    Depends,
    BackgroundTasks,
)
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import and_
from database import AsyncSessionLocal, AnalysisRecord
import asyncio
from concurrent.futures import ThreadPoolExecutor
from collections import defaultdict
import time
import io
import csv
import math
from datetime import datetime, timedelta, timezone
from sse_starlette.sse import EventSourceResponse
import json
from fastapi import Request

app = FastAPI(title="ForestSnap Edge Server")

active_connections = set()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

WEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY", "YOUR_API_KEY_HERE")
SLACK_WEBHOOK_URL = os.getenv("SLACK_WEBHOOK_URL", "")
SEG_MODEL_PATH = "models/deeplabv3_mobile.onnx"
CLS_MODEL_PATH = "models/efficientnet_v2.onnx"

try:
    seg_session = ort.InferenceSession(
        SEG_MODEL_PATH, providers=["CPUExecutionProvider"]
    )
    cls_session = ort.InferenceSession(
        CLS_MODEL_PATH, providers=["CPUExecutionProvider"]
    )
except Exception as e:
    print(
        f"Warning: Models not found or failed to load. Ensure they are in /models. Error: {e}"
    )


class AnalysisResponse(BaseModel):
    fuel_load_score: float
    dryness_risk_tier: int
    temperature_c: float
    humidity_percent: int
    wind_speed_ms: float
    wind_direction_deg: int
    final_fire_risk_percent: float


executor = ThreadPoolExecutor(max_workers=os.cpu_count() or 4)


def preprocess_image(img: np.ndarray, target_size: tuple):
    img_resized = cv2.resize(img, target_size)
    img_float = img_resized.astype(np.float32) / 255.0
    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)
    img_norm = (img_float - mean) / std
    img_transposed = np.transpose(img_norm, (2, 0, 1))
    return np.expand_dims(img_transposed, axis=0)


def decode_image(image_bytes: bytes):
    np_arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    return cv2.cvtColor(img, cv2.COLOR_BGR2RGB)


async def fetch_weather(lat: float, lon: float):
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={WEATHER_API_KEY}&units=metric"
    async with httpx.AsyncClient() as client:
        resp = await client.get(url)
        if resp.status_code == 200:
            data = resp.json()
            return {
                "temp": data["main"]["temp"],
                "humidity": data["main"]["humidity"],
                "wind_speed": data["wind"]["speed"],
                "wind_direction": data["wind"].get("deg", 0),
            }
        return {"temp": 25.0, "humidity": 50, "wind_speed": 5.0, "wind_direction": 0}


async def get_db():
    async with AsyncSessionLocal() as session:
        yield session


async def dispatch_webhook_alert(
    record_id: int, lat: float, lon: float, risk: float, fuel: float
):
    """Fires an alert to a configured webhook (e.g., Slack, Teams) without blocking the main thread."""
    if not SLACK_WEBHOOK_URL:
        return

    payload = {
        "text": "🚨 *CRITICAL FIRE THREAT DETECTED* 🚨",
        "blocks": [
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": f"📍 *Location:* {lat}, {lon}\n🔥 *Risk Level:* {risk}%\n🪵 *Fuel Load:* {fuel}%\n\nImmediate review of the dashboard is recommended.",
                },
            }
        ],
    }

    try:
        async with httpx.AsyncClient() as client:
            await client.post(SLACK_WEBHOOK_URL, json=payload)
    except Exception as e:
        print(f"Failed to dispatch webhook: {e}")


@app.on_event("startup")
async def on_startup():
    from database import init_db

    await init_db()


@app.get("/health")
async def health_check():
    return {"status": "online"}


@app.get("/stream")
async def stream_updates(request: Request):
    """Maintains an open connection with the client and pushes new records."""
    queue = asyncio.Queue()
    active_connections.add(queue)

    async def event_generator():
        try:
            while True:

                if await request.is_disconnected():
                    break

                record_data = await queue.get()
                yield {"event": "new_record", "data": json.dumps(record_data)}
        except asyncio.CancelledError:
            pass
        finally:
            if queue in active_connections:
                active_connections.remove(queue)

    return EventSourceResponse(
        event_generator(),
        ping=15,
        headers={"Cache-Control": "no-cache", "Connection": "keep-alive"},
    )


@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_environment(
    background_tasks: BackgroundTasks,
    lat: float = Form(...),
    lon: float = Form(...),
    image: UploadFile = File(...),
    db: AsyncSession = Depends(get_db),
):
    try:
        image_bytes = await image.read()
        img_array = decode_image(image_bytes)
        seg_input = preprocess_image(img_array, (256, 256))
        loop = asyncio.get_running_loop()

        seg_out = await loop.run_in_executor(
            executor,
            lambda: seg_session.run(
                None, {seg_session.get_inputs()[0].name: seg_input}
            )[0],
        )

        predicted_classes = np.argmax(seg_out[0], axis=0)
        fuel_pixels = np.sum((predicted_classes == 3) | (predicted_classes == 4))
        total_pixels = 256 * 256
        fuel_load_score = (fuel_pixels / total_pixels) * 100

        cls_input = preprocess_image(img_array, (224, 224))
        cls_out = await loop.run_in_executor(
            executor,
            lambda: cls_session.run(
                None, {cls_session.get_inputs()[0].name: cls_input}
            )[0],
        )
        dryness_risk_tier = int(np.argmax(cls_out[0]))

        weather = await fetch_weather(lat or 0.0, lon or 0.0)

        base_visual_risk = (dryness_risk_tier / 3.0) * 100
        temp_mod = max(0, (weather["temp"] - 20) * 1.5)
        hum_mod = max(0, (50 - weather["humidity"]) * 0.8)
        wind_mod = weather["wind_speed"] * 2.0

        final_risk = (
            (base_visual_risk * 0.4)
            + (fuel_load_score * 0.3)
            + (temp_mod + hum_mod + wind_mod) * 0.3
        )
        final_risk = min(max(final_risk, 0.0), 100.0)

        new_record = AnalysisRecord(
            latitude=lat,
            longitude=lon,
            fuel_load_score=float(fuel_load_score),
            dryness_risk_tier=dryness_risk_tier,
            temperature_c=weather["temp"],
            humidity_percent=weather["humidity"],
            wind_speed_ms=weather["wind_speed"],
            wind_direction_deg=weather["wind_direction"],
            final_fire_risk_percent=round(final_risk, 2),
        )
        db.add(new_record)
        await db.commit()
        await db.refresh(new_record)

        if final_risk >= 80.0:
            background_tasks.add_task(
                dispatch_webhook_alert,
                record_id=new_record.id,
                lat=new_record.latitude,
                lon=new_record.longitude,
                risk=new_record.final_fire_risk_percent,
                fuel=new_record.fuel_load_score,
            )

        record_dict = {
            "id": new_record.id,
            "timestamp": new_record.timestamp.isoformat(),
            "latitude": new_record.latitude,
            "longitude": new_record.longitude,
            "fuel_load_score": new_record.fuel_load_score,
            "dryness_risk_tier": new_record.dryness_risk_tier,
            "temperature_c": new_record.temperature_c,
            "humidity_percent": new_record.humidity_percent,
            "wind_speed_ms": new_record.wind_speed_ms,
            "wind_direction_deg": new_record.wind_direction_deg,
            "final_fire_risk_percent": new_record.final_fire_risk_percent,
        }
        for q in active_connections:
            await q.put(record_dict)

        return {
            "fuel_load_score": float(fuel_load_score),
            "dryness_risk_tier": dryness_risk_tier,
            "temperature_c": weather["temp"],
            "humidity_percent": weather["humidity"],
            "wind_speed_ms": weather["wind_speed"],
            "wind_direction_deg": weather["wind_direction"],
            "final_fire_risk_percent": round(final_risk, 2),
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/history")
async def get_history(
    skip: int = 0, limit: int = 100, db: AsyncSession = Depends(get_db)
):

    query = (
        select(AnalysisRecord)
        .order_by(AnalysisRecord.timestamp.desc())
        .offset(skip)
        .limit(limit)
    )
    result = await db.execute(query)
    return result.scalars().all()


@app.get("/history/region")
async def get_history_region(
    minLat: float,
    maxLat: float,
    minLon: float,
    maxLon: float,
    db: AsyncSession = Depends(get_db),
):

    query = select(AnalysisRecord).filter(
        and_(
            AnalysisRecord.latitude >= minLat,
            AnalysisRecord.latitude <= maxLat,
            AnalysisRecord.longitude >= minLon,
            AnalysisRecord.longitude <= maxLon,
        )
    )
    result = await db.execute(query)
    return result.scalars().all()


@app.get("/heatmap/region")
async def get_heatmap_region(
    minLat: float,
    maxLat: float,
    minLon: float,
    maxLon: float,
    grid_size: float = 0.01,
    db: AsyncSession = Depends(get_db),
):
    query = select(AnalysisRecord).filter(
        and_(
            AnalysisRecord.latitude >= minLat,
            AnalysisRecord.latitude <= maxLat,
            AnalysisRecord.longitude >= minLon,
            AnalysisRecord.longitude <= maxLon,
        )
    )
    result = await db.execute(query)
    records = result.scalars().all()

    grid = defaultdict(lambda: {"risk_sum": 0, "count": 0})
    for r in records:
        grid_lat = round(r.latitude / grid_size) * grid_size
        grid_lon = round(r.longitude / grid_size) * grid_size
        key = (f"{grid_lat:.4f}", f"{grid_lon:.4f}")
        grid[key]["risk_sum"] += r.final_fire_risk_percent
        grid[key]["count"] += 1

    results = []
    for (lat, lon), data in grid.items():
        results.append(
            {
                "center_lat": float(lat),
                "center_lon": float(lon),
                "avg_risk": round(data["risk_sum"] / data["count"], 2),
                "point_count": data["count"],
                "grid_size": grid_size,
            }
        )
    return results


@app.get("/forests/boundaries")
async def get_forest_boundaries():
    return {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {
                    "name": "Jarakabande Kaval Forest",
                    "type": "Protected Area",
                },
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [77.5450, 13.0450],
                            [77.5650, 13.0450],
                            [77.5650, 13.0250],
                            [77.5450, 13.0250],
                            [77.5450, 13.0450],
                        ]
                    ],
                },
            }
        ],
    }


FIRMS_MAP_KEY = os.getenv("FIRMS_MAP_KEY", "59f97dbb35f3ef8e70d51fff95d18a7b")
firms_cache = {"data": [], "last_fetched": 0}
CACHE_TTL = 600


@app.get("/firms/active-fires")
async def get_global_fires():
    current_time = time.time()
    if current_time - firms_cache["last_fetched"] < CACHE_TTL and firms_cache["data"]:
        return firms_cache["data"]

    if FIRMS_MAP_KEY == "YOUR_FIRMS_API_KEY":
        raise HTTPException(
            status_code=500, detail="NASA FIRMS API Key not configured on server."
        )

    url = f"https://firms.modaps.eosdis.nasa.gov/api/area/csv/{FIRMS_MAP_KEY}/VIIRS_SNPP_NRT/world/1"
    async with httpx.AsyncClient(timeout=30.0) as client:
        resp = await client.get(url)
        if resp.status_code != 200:
            raise HTTPException(
                status_code=resp.status_code, detail="Failed to fetch from NASA"
            )

        parsed_points = []
        csv_reader = csv.DictReader(io.StringIO(resp.text))
        for row in csv_reader:
            try:
                parsed_points.append(
                    {
                        "latitude": float(row["latitude"]),
                        "longitude": float(row["longitude"]),
                        "brightness": float(row["bright_ti4"]),
                        "confidence": row.get("confidence", "n"),
                    }
                )
            except (ValueError, KeyError):
                continue

        parsed_points.sort(key=lambda x: x["brightness"], reverse=True)
        top_fires = parsed_points[:1000]
        firms_cache["data"] = top_fires
        firms_cache["last_fetched"] = current_time
        return top_fires


def haversine_distance(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (
        math.sin(dlat / 2) ** 2
        + math.cos(math.radians(lat1))
        * math.cos(math.radians(lat2))
        * math.sin(dlon / 2) ** 2
    )
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c


@app.get("/alerts")
async def get_system_alerts(db: AsyncSession = Depends(get_db)):
    alerts = []
    time_threshold = datetime.now(timezone.utc) - timedelta(hours=72)

    query = select(AnalysisRecord).filter(
        AnalysisRecord.final_fire_risk_percent >= 80,
        AnalysisRecord.timestamp >= time_threshold,
    )
    result = await db.execute(query)
    critical_local = result.scalars().all()

    for record in critical_local:
        alerts.append(
            {
                "id": f"local-{record.id}",
                "type": "local",
                "severity": "high",
                "title": "High Local Fire Risk",
                "message": f"Critical fuel & dryness ({record.final_fire_risk_percent}%) reported by ground team.",
                "lat": record.latitude,
                "lon": record.longitude,
            }
        )

    firms_data = firms_cache.get("data", [])
    for fire in firms_data:
        if fire.get("confidence") == "h" or fire.get("brightness", 0) > 340:
            for local in critical_local:
                dist = haversine_distance(
                    fire["latitude"], fire["longitude"], local.latitude, local.longitude
                )
                if dist < 15.0:
                    alerts.append(
                        {
                            "id": f"nasa-cross-{fire['latitude']}-{fire['longitude']}",
                            "type": "cross-reference",
                            "severity": "critical",
                            "title": "IMMINENT THREAT CONFIRMED",
                            "message": f"NASA satellite thermal anomaly detected within {round(dist, 1)}km of a high-risk ground report!",
                            "lat": fire["latitude"],
                            "lon": fire["longitude"],
                        }
                    )
                    break

    severity_order = {"critical": 0, "high": 1, "warning": 2}
    alerts.sort(key=lambda x: severity_order.get(x["severity"], 3))
    return alerts[:5]


@app.get("/weather/current")
async def get_current_weather(lat: float, lon: float):
    return await fetch_weather(lat, lon)


if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)
