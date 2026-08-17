# ForestSnap

ForestSnap is a full-stack wildfire monitoring and forest management ecosystem. It combines an Android mobile application for field surveys, a React-based web portal for interactive analytics, and an edge computer vision server for real-time fire risk assessment.

---

## Ecosystem Architecture

The repository is structured into three main components:

- **`app/`**: Native Android application built with Kotlin and Jetpack Compose. Captures georeferenced forest snapshots, stores data locally via Room, and syncs automatically with the edge backend when online.
- **`server/`**: Edge inference server powered by FastAPI, OpenCV, and ONNX Runtime. Evaluates fuel load score, biomass dryness risk, integration with real-time OpenWeather, NASA FIRMS thermal anomaly feeds, and live Server-Sent Events (SSE) alert broadcasting.
- **`web/`**: Dashboard frontend constructed with React, TypeScript, Vite, and Mapbox GL. Features live spatial mapping, historical diff comparison, scenario simulation, and export capabilities.

---

## Key Features

- **Field Data Collection**: Offline-first camera and metadata recording with automatic background upload queue.
- **Computer Vision Risk Scoring**: Edge ONNX models assess canopy density and fuel accumulation from field photos.
- **Live Geospatial Mapping**: Mapbox integration displaying active hotspots, perimeter boundaries, and risk tiers.
- **Environmental Context**: Automated weather fetching and NASA satellite fire monitoring integration.
- **Scenario Simulation**: Interactive web tool for testing fire risk parameters under varying environmental conditions.

---

## Quick Start

### 1. Edge Server Setup
Requires Python 3.10+ and ONNX Runtime dependencies.

```bash
cd server
pip install -r requirements.txt
python seed_db.py  # Seed initial database records
python server.py   # Starts FastAPI server on http://localhost:8000
```

### 2. Web Portal Setup
Requires Node.js 18+.

```bash
cd web
npm install
npm run dev        # Starts Vite dev server on http://localhost:5173
```

### 3. Mobile App Setup
Requires Android Studio (API level 26+).

```bash
./gradlew installDebug
```

---

## License

This project is licensed under the MIT License.
