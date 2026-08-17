import axios from "axios";
import type {
  AnalysisRecord,
  FirmsFirePoint,
  HeatmapSquare,
  GeoJsonFeatureCollection,
  SystemAlert,
} from "./types";

// Base URL for the edge backend service
const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000";

// Axios client instance configured with default timeout
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

/**
 * Fetch paginated analysis records from the edge backend.
 */
export const fetchHistory = async (
  skip: number = 0,
  limit: number = 500,
): Promise<AnalysisRecord[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/history`, {
      params: { skip, limit },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching history:", error);
    return [];
  }
};

/**
 * Fetch active satellite fire data points from NASA FIRMS integration.
 */
export const fetchGlobalFires = async (): Promise<FirmsFirePoint[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/firms/active-fires`);
    return response.data;
  } catch (error) {
    console.error("Error fetching NASA FIRMS data:", error);
    return [];
  }
};

/**
 * Fetch aggregated fire risk heatmap grid cells for a geographic bounding region.
 */
export const fetchHeatmap = async (
  minLat: number,
  maxLat: number,
  minLon: number,
  maxLon: number,
): Promise<HeatmapSquare[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/heatmap/region`, {
      params: { minLat, maxLat, minLon, maxLon, grid_size: 0.01 },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching heatmap:", error);
    return [];
  }
};

/**
 * Fetch protected forest polygon boundaries in GeoJSON format.
 */
export const fetchBoundaries =
  async (): Promise<GeoJsonFeatureCollection | null> => {
    try {
      const response = await apiClient.get(
        `${API_BASE_URL}/forests/boundaries`,
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching boundaries:", error);
      return null;
    }
  };

/**
 * Fetch high-priority system alerts and cross-referenced satellite warnings.
 */
export const fetchAlerts = async (): Promise<SystemAlert[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/alerts`);
    return response.data;
  } catch (error) {
    console.error("Error fetching alerts:", error);
    return [];
  }
};

/**
 * Fetch hourly weather forecasts from the Open-Meteo public API.
 */
export const fetchWeatherForecast = async (
  lat: number,
  lon: number,
): Promise<any> => {
  try {
    const response = await axios.get(`https://api.open-meteo.com/v1/forecast`, {
      params: {
        latitude: lat,
        longitude: lon,
        hourly: "temperature_2m,relative_humidity_2m",
        forecast_days: 2,
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching forecast:", error);
    return null;
  }
};
