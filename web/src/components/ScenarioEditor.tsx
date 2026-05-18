import React, { useState, useEffect, useRef } from "react";
import {
  Sliders,
  Wind,
  Droplets,
  Thermometer,
  Flame,
  RotateCcw,
} from "lucide-react";
import type { AnalysisRecord } from "../types";

interface ScenarioEditorProps {
  baseRecord: AnalysisRecord | null;
  onSimulate: (simulatedRecord: AnalysisRecord) => void;
}

export const ScenarioEditor: React.FC<ScenarioEditorProps> = ({
  baseRecord,
  onSimulate,
}) => {
  const [isModified, setIsModified] = useState(false);
  const [simState, setSimState] = useState({
    temperature_c: 35,
    humidity_percent: 20,
    wind_speed_ms: 10,
    wind_direction_deg: 90,
    fuel_load_score: 80,
  });

  const onSimulateRef = useRef(onSimulate);

  useEffect(() => {
    onSimulateRef.current = onSimulate;
  }, [onSimulate]);

  // Only auto-sync with baseRecord if the user HAS NOT touched the sliders
  useEffect(() => {
    if (baseRecord && !isModified) {
      setSimState((prev) => {
        // If the values are already identical, return the previous state object directly.
        // This tells React NOT to trigger a re-render.
        if (
          prev.temperature_c === baseRecord.temperature_c &&
          prev.humidity_percent === baseRecord.humidity_percent &&
          prev.wind_speed_ms === baseRecord.wind_speed_ms &&
          prev.wind_direction_deg === baseRecord.wind_direction_deg &&
          prev.fuel_load_score === baseRecord.fuel_load_score
        ) {
          return prev;
        }
        return {
          temperature_c: baseRecord.temperature_c,
          humidity_percent: baseRecord.humidity_percent,
          wind_speed_ms: baseRecord.wind_speed_ms,
          wind_direction_deg: baseRecord.wind_direction_deg,
          fuel_load_score: baseRecord.fuel_load_score,
        };
      });
    }
  }, [baseRecord, isModified]);

  useEffect(() => {
    const risk = Math.max(
      0,
      Math.min(
        100,
        simState.temperature_c * 1.5 +
          simState.wind_speed_ms * 2.0 +
          simState.fuel_load_score * 0.5 -
          simState.humidity_percent * 0.8,
      ),
    );

    onSimulateRef.current({
      id: 999999,
      timestamp: new Date().toISOString(),
      latitude: baseRecord?.latitude || 13.03,
      longitude: baseRecord?.longitude || 77.56,
      ...simState,
      dryness_risk_tier: simState.humidity_percent < 30 ? 5 : 2,
      final_fire_risk_percent: risk,
    });
  }, [simState, baseRecord]);

  const handleChange = (key: keyof typeof simState, value: number) => {
    setIsModified(true); // Decouple from live data stream updates
    setSimState((prev) => ({ ...prev, [key]: value }));
  };

  const resetToBase = () => {
    setIsModified(false); // Re-couple to live data
  };

  return (
    <div className="glass-panel p-5 rounded-xl border-2 border-primary shadow-[0_0_15px_rgba(16,185,129,0.2)]">
      <div className="flex items-center justify-between mb-6 border-b border-border-main pb-3">
        <div className="flex items-center space-x-2">
          <Sliders size={20} className="text-primary" />
          <h3 className="text-md font-bold text-text-main">
            Simulation Engine
          </h3>
        </div>
        {isModified && (
          <button
            onClick={resetToBase}
            className="flex items-center gap-1 text-xs font-bold text-primary hover:text-primary/80 transition-colors"
          >
            <RotateCcw size={12} /> Reset to Live
          </button>
        )}
      </div>
      <div className="space-y-5">
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="flex items-center gap-2 text-text-muted">
              <Thermometer size={14} /> Temp (°C)
            </span>
            <span className="font-mono text-primary">
              {simState.temperature_c}°
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="55"
            value={simState.temperature_c}
            onChange={(e) =>
              handleChange("temperature_c", Number(e.target.value))
            }
            className="w-full accent-rose-500"
          />
        </div>
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="flex items-center gap-2 text-text-muted">
              <Droplets size={14} /> Humidity (%)
            </span>
            <span className="font-mono text-primary">
              {simState.humidity_percent}%
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            value={simState.humidity_percent}
            onChange={(e) =>
              handleChange("humidity_percent", Number(e.target.value))
            }
            className="w-full accent-cyan-500"
          />
        </div>
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="flex items-center gap-2 text-text-muted">
              <Wind size={14} /> Wind (m/s)
            </span>
            <span className="font-mono text-primary">
              {simState.wind_speed_ms} m/s
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="35"
            value={simState.wind_speed_ms}
            onChange={(e) =>
              handleChange("wind_speed_ms", Number(e.target.value))
            }
            className="w-full accent-primary"
          />
        </div>
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="flex items-center gap-2 text-text-muted">
              <Wind size={14} /> Wind Bearing (°)
            </span>
            <span className="font-mono text-primary">
              {simState.wind_direction_deg}°
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="360"
            value={simState.wind_direction_deg}
            onChange={(e) =>
              handleChange("wind_direction_deg", Number(e.target.value))
            }
            className="w-full accent-primary"
          />
        </div>
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="flex items-center gap-2 text-text-muted">
              <Flame size={14} /> Fuel Load Score
            </span>
            <span className="font-mono text-primary">
              {simState.fuel_load_score.toFixed(1)}
            </span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            value={simState.fuel_load_score}
            onChange={(e) =>
              handleChange("fuel_load_score", Number(e.target.value))
            }
            className="w-full accent-amber-500"
          />
        </div>
      </div>
    </div>
  );
};
