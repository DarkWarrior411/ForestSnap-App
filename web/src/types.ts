import type { FeatureCollection } from "geojson";

export interface AnalysisRecord {
  id: number;
  timestamp: string;
  latitude: number;
  longitude: number;
  fuel_load_score: number;
  dryness_risk_tier: number;
  temperature_c: number;
  humidity_percent: number;
  wind_speed_ms: number;
  wind_direction_deg: number;
  final_fire_risk_percent: number;
}

export interface FirmsFirePoint {
  latitude: number;
  longitude: number;
  brightness: number;
  confidence: string;
}

export interface HeatmapSquare {
  center_lat: number;
  center_lon: number;
  avg_risk: number;
  point_count: number;
  grid_size: number;
}

export interface SystemAlert {
  id: string;
  type: string;
  severity: "critical" | "high" | "warning";
  title: string;
  message: string;
  lat: number;
  lon: number;
}

export type GeoJsonFeatureCollection = FeatureCollection;
