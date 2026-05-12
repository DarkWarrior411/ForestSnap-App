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
  final_fire_risk_percent: number;
}
