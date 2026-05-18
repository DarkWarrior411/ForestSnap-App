import axios from "axios";
import type {
  AnalysisRecord,
  FirmsFirePoint,
  HeatmapSquare,
  GeoJsonFeatureCollection,
  SystemAlert,
} from "./types";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

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

export const fetchGlobalFires = async (): Promise<FirmsFirePoint[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/firms/active-fires`);
    return response.data;
  } catch (error) {
    console.error("Error fetching NASA FIRMS data:", error);
    return [];
  }
};

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

export const fetchAlerts = async (): Promise<SystemAlert[]> => {
  try {
    const response = await apiClient.get(`${API_BASE_URL}/alerts`);
    return response.data;
  } catch (error) {
    console.error("Error fetching alerts:", error);
    return [];
  }
};

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
