import os
import cv2
import numpy as np
import onnxruntime as ort
import httpx
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
from fastapi import Depends
from sqlalchemy.orm import Session
from database import SessionLocal, AnalysisRecord

app = FastAPI(title="ForestSnap Edge Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

WEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY", "YOUR_API_KEY_HERE")
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
    final_fire_risk_percent: float


import asyncio
from concurrent.futures import ThreadPoolExecutor

# Create a thread pool for CPU-bound tasks (image decoding/preprocessing and ONNX inference)
executor = ThreadPoolExecutor(max_workers=os.cpu_count() or 4)

def preprocess_image(img: np.ndarray, target_size: tuple):
    """Preprocesses a pre-decoded image array to match Colab PyTorch transforms."""
    img_resized = cv2.resize(img, target_size)
    img_float = img_resized.astype(np.float32) / 255.0
    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)
    img_norm = (img_float - mean) / std

    img_transposed = np.transpose(img_norm, (2, 0, 1))
    return np.expand_dims(img_transposed, axis=0)  # Add batch dimension

def decode_image(image_bytes: bytes):
    np_arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    return cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

def run_ml_pipeline(image_bytes: bytes):
    img = decode_image(image_bytes)
    
    seg_input = preprocess_image(img, (256, 256))
    seg_out = seg_session.run(None, {seg_session.get_inputs()[0].name: seg_input})[0]
    predicted_classes = np.argmax(seg_out[0], axis=0)
    fuel_pixels = np.sum((predicted_classes == 3) | (predicted_classes == 4))
    total_pixels = 256 * 256
    fuel_load_score = (fuel_pixels / total_pixels) * 100

    cls_input = preprocess_image(img, (224, 224))
    cls_out = cls_session.run(None, {cls_session.get_inputs()[0].name: cls_input})[0]
    dryness_risk_tier = int(np.argmax(cls_out[0]))
    
    return fuel_load_score, dryness_risk_tier

async def fetch_weather(lat: float, lon: float):
    """Asynchronously grabs live meteorological data."""
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={WEATHER_API_KEY}&units=metric"
    async with httpx.AsyncClient() as client:
        resp = await client.get(url)
        if resp.status_code == 200:
            data = resp.json()
            return {
                "temp": data["main"]["temp"],
                "humidity": data["main"]["humidity"],
                "wind_speed": data["wind"]["speed"],
            }
        return {"temp": 25.0, "humidity": 50, "wind_speed": 5.0}  # Fallback defaults


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_environment(
    lat: float = Form(...),
    lon: float = Form(...),
    image: UploadFile = File(...),
    db: Session = Depends(get_db),  # <-- ADD THIS
):
    try:
        image_bytes = await image.read()

        seg_input = preprocess_image(image_bytes, (256, 256))
        seg_out = seg_session.run(None, {seg_session.get_inputs()[0].name: seg_input})[
            0
        ]

        predicted_classes = np.argmax(seg_out[0], axis=0)
        fuel_pixels = np.sum((predicted_classes == 3) | (predicted_classes == 4))
        total_pixels = 256 * 256
        fuel_load_score = (fuel_pixels / total_pixels) * 100

        cls_input = preprocess_image(image_bytes, (224, 224))
        cls_out = cls_session.run(None, {cls_session.get_inputs()[0].name: cls_input})[
            0
        ]
        dryness_risk_tier = int(np.argmax(cls_out[0]))  # 0-3 Scale

        weather = await fetch_weather(lat, lon)

        base_visual_risk = (dryness_risk_tier / 3.0) * 100

        temp_mod = max(0, (weather["temp"] - 20) * 1.5)  # Hotter = riskier
        hum_mod = max(0, (50 - weather["humidity"]) * 0.8)  # Drier = riskier
        wind_mod = weather["wind_speed"] * 2.0  # Wind accelerates spread

        final_risk = (
            (base_visual_risk * 0.4)
            + (fuel_load_score * 0.3)
            + (temp_mod + hum_mod + wind_mod) * 0.3
        )
        final_risk = min(max(final_risk, 0.0), 100.0)  # Clamp between 0 and 100

        new_record = AnalysisRecord(
            latitude=lat,
            longitude=lon,
            fuel_load_score=float(fuel_load_score),
            dryness_risk_tier=dryness_risk_tier,
            temperature_c=weather["temp"],
            humidity_percent=weather["humidity"],
            wind_speed_ms=weather["wind_speed"],
            final_fire_risk_percent=round(final_risk, 2),
        )
        db.add(new_record)
        db.commit()
        db.refresh(new_record)

        return {
            "fuel_load_score": float(fuel_load_score),
            "dryness_risk_tier": dryness_risk_tier,
            "temperature_c": weather["temp"],
            "humidity_percent": weather["humidity"],
            "wind_speed_ms": weather["wind_speed"],
            "final_fire_risk_percent": round(final_risk, 2),
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/history")
async def get_history(db: Session = Depends(get_db)):

    records = (
        db.query(AnalysisRecord).order_by(AnalysisRecord.id.desc()).limit(50).all()
    )

    return records


if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)
