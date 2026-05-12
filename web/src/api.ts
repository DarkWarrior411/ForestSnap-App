import axios from "axios";
import type { AnalysisRecord } from "./types";

const API_BASE_URL = "http://localhost:8000";

export const fetchHistory = async (): Promise<AnalysisRecord[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/history`);
    return response.data;
  } catch (error) {
    console.error("Error fetching history:", error);
    return [];
  }
};
