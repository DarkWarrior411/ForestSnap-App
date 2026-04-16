import os
import cv2
import numpy as np
import onnxruntime as ort
import httpx
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
import uvicorn
from fastapi import Depends
from sqlalchemy.orm import Session
from database import SessionLocal, AnalysisRecord

app = FastAPI(title="ForestSnap Edge Server")

# --- Globals & Configuration ---
WEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY", "YOUR_API_KEY_HERE")
SEG_MODEL_PATH = "models/deeplabv3_mobile_quantized.onnx"
CLS_MODEL_PATH = "models/efficientnet_v2_quantized.onnx"

# --- Load ONNX Sessions ---
# Using CPUExecutionProvider as specified for edge CPU constraints
try:
    seg_session = ort.InferenceSession(SEG_MODEL_PATH, providers=["CPUExecutionProvider"])
    cls_session = ort.InferenceSession(CLS_MODEL_PATH, providers=["CPUExecutionProvider"])
except Exception as e:
    print(f"Warning: Models not found or failed to load. Ensure they are in /models. Error: {e}")

# --- Data Models ---
class AnalysisResponse(BaseModel):
    fuel_load_score: float
    dryness_risk_tier: int
    temperature_c: float
    humidity_percent: int
    wind_speed_ms: float
    final_fire_risk_percent: float

# --- Helper Functions ---
def preprocess_image(image_bytes: bytes, target_size: tuple):
    """Decodes and preprocesses image bytes to match Colab PyTorch transforms."""
    np_arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, target_size)
    
    # Normalize: mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]
    img = img.astype(np.float32) / 255.0
    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)
    img = (img - mean) / std
    
    # HWC to CHW format expected by PyTorch/ONNX
    img = np.transpose(img, (2, 0, 1))
    return np.expand_dims(img, axis=0) # Add batch dimension

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
                "wind_speed": data["wind"]["speed"]
            }
        return {"temp": 25.0, "humidity": 50, "wind_speed": 5.0} # Fallback defaults

# --- Primary Endpoint ---

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
    image: UploadFile = File(...)
):
    try:
        image_bytes = await image.read()
        
        # 1. Visual Phase: Semantic Segmentation (Fuel Load) -> 256x256
        seg_input = preprocess_image(image_bytes, (256, 256))
        seg_out = seg_session.run(None, {seg_session.get_inputs()[0].name: seg_input})[0]
        # Calculate geometric volume of class 3 & 4 (Green Undergrowth & Dead Fuel)
        predicted_classes = np.argmax(seg_out[0], axis=0)
        fuel_pixels = np.sum((predicted_classes == 3) | (predicted_classes == 4))
        total_pixels = 256 * 256
        fuel_load_score = (fuel_pixels / total_pixels) * 100 

        # 2. Visual Phase: Classification (Dryness/Risk) -> 224x224
        cls_input = preprocess_image(image_bytes, (224, 224))
        cls_out = cls_session.run(None, {cls_session.get_inputs()[0].name: cls_input})[0]
        dryness_risk_tier = int(np.argmax(cls_out[0])) # 0-3 Scale

        # 3. Environmental Phase: Live REST API lookup
        weather = await fetch_weather(lat, lon)

        # 4. Late-Fusion Phase: Algorithmic Heuristic
        # Base risk from visual dryness tier (e.g., Tier 0=10%, Tier 3=80%)
        base_visual_risk = (dryness_risk_tier / 3.0) * 100
        
        # Modifiers based on weather
        temp_mod = max(0, (weather["temp"] - 20) * 1.5) # Hotter = riskier
        hum_mod = max(0, (50 - weather["humidity"]) * 0.8) # Drier = riskier
        wind_mod = weather["wind_speed"] * 2.0 # Wind accelerates spread
        
        # Final calculation: Visual baseline weighted by Fuel Volume and Weather catalysts
        final_risk = (base_visual_risk * 0.4) + (fuel_load_score * 0.3) + (temp_mod + hum_mod + wind_mod) * 0.3
        final_risk = min(max(final_risk, 0.0), 100.0) # Clamp between 0 and 100

        # --- NEW: Save to Database Before Returning ---
        new_record = AnalysisRecord(
            latitude=lat,
            longitude=lon,
            fuel_load_score=float(fuel_load_score),
            dryness_risk_tier=dryness_risk_tier,
            temperature_c=weather["temp"],
            humidity_percent=weather["humidity"],
            wind_speed_ms=weather["wind_speed"],
            final_fire_risk_percent=round(final_risk, 2)
        )
        db.add(new_record)
        db.commit()
        db.refresh(new_record)

        # Return the response to the Android App
        return {
            "fuel_load_score": float(fuel_load_score),
            "dryness_risk_tier": dryness_risk_tier,
            "temperature_c": weather["temp"],
            "humidity_percent": weather["humidity"],
            "wind_speed_ms": weather["wind_speed"],
            "final_fire_risk_percent": round(final_risk, 2)
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=True)