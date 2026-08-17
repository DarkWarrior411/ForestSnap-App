# ForestSnap Web Portal

The web analytics interface for the ForestSnap ecosystem. Built with React, TypeScript, Vite, and Tailwind CSS, this application allows forest managers and emergency responders to visualize real-time field survey data, monitor wildfire risk maps, analyze historical environmental shifts, and simulate environmental risk scenarios.

---

## Technical Overview

- **Framework**: React 18 with TypeScript and Vite
- **Styling**: Tailwind CSS with custom dark mode theme
- **Mapping**: Mapbox GL JS (`react-map-gl`) for vector map rendering, boundary polylines, and heatmap layers
- **State & Data Handling**: Custom hooks and REST client connecting to the Python edge server
- **Icons & UI Utilities**: Lucide React icons, Canvas Confetti, and HTML-to-Image export

---

## Application Layout & Pages

- **Dashboard (`/`)**: Main operational view featuring interactive map layers, active risk statistics, real-time alert feed, and snap inspection drawers.
- **Historical Comparison (`/history`)**: Side-by-side or slider comparison of forest snapshots over time to measure vegetation density and moisture decay.
- **Scenario Simulator (`/simulator`)**: Interactive playground for tweaking temperature, humidity, wind, and fuel load parameters to forecast fire risk score shifts.
- **App Download Promo (`/app`)**: Mobile app feature showcase and direct APK download portal.
- **About & Documentation (`/about`)**: Overview of the technology stack, ML model architecture, and system workflow.

---

## Development Setup

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Environment Configuration**:
   Create a `.env` file in the `web/` root (or configure variables):
   ```env
   VITE_API_URL=http://localhost:8000
   VITE_MAPBOX_TOKEN=your_mapbox_token_here
   ```

3. **Start Development Server**:
   ```bash
   npm run dev
   ```

4. **Build for Production**:
   ```bash
   npm run build
   ```

---

## License

MIT License.
