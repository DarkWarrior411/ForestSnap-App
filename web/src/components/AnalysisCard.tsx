import { motion } from "framer-motion";
import {
  Thermometer,
  Droplets,
  Wind,
  Flame,
  MapPin,
  TreePine,
} from "lucide-react";
import type { AnalysisRecord } from "../types";
import { cn } from "../utils";

interface AnalysisCardProps {
  record: AnalysisRecord;
  isSelected: boolean;
  onSelect: () => void;
}

// MOVED OUTSIDE: This prevents React from destroying and recreating the DOM node on every render
const MetricBar = ({
  value,
  max,
  colorClass,
}: {
  value: number;
  max: number;
  colorClass: string;
}) => (
  <div className="w-full bg-background rounded-full h-1.5 mt-1 overflow-hidden border border-border-main">
    <div
      className={cn("h-full rounded-full", colorClass)}
      style={{ width: `${Math.min((value / max) * 100, 100)}%` }}
    />
  </div>
);

export const AnalysisCard: React.FC<AnalysisCardProps> = ({
  record,
  isSelected,
  onSelect,
}) => {
  const getRiskColor = (risk: number) => {
    if (risk < 25)
      return "text-emerald-500 bg-emerald-500/10 border-emerald-500/30";
    if (risk < 50) return "text-amber-500 bg-amber-500/10 border-amber-500/30";
    if (risk < 75)
      return "text-orange-500 bg-orange-500/10 border-orange-500/30";
    return "text-red-500 bg-red-500/10 border-red-500/30";
  };

  const getRiskLabel = (risk: number) => {
    if (risk < 25) return "Low Risk";
    if (risk < 50) return "Moderate Risk";
    if (risk < 75) return "High Risk";
    return "Critical Risk";
  };

  const riskClasses = getRiskColor(record.final_fire_risk_percent);

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.95 }}
      whileHover={{ scale: 1.01 }}
      whileTap={{ scale: 0.98 }}
      onClick={onSelect}
      className={cn(
        "cursor-pointer rounded-2xl p-5 transition-all duration-200",
        "glass-panel hover:bg-surface/80",
        isSelected
          ? "border-primary shadow-[0_0_20px_rgba(16,185,129,0.15)] ring-1 ring-primary/50"
          : "border-border-main hover:border-primary/50",
      )}
    >
      <div className="flex justify-between items-start mb-5">
        <div className="flex items-center space-x-2 text-text-muted">
          <div className="p-1.5 bg-background rounded-lg border border-border-main">
            <MapPin size={14} className="text-primary" />
          </div>
          <span className="text-sm font-bold font-mono tracking-tight text-text-main">
            {record.latitude.toFixed(3)}, {record.longitude.toFixed(3)}
          </span>
        </div>
        <span className="text-xs font-bold text-primary uppercase tracking-wider bg-background px-2 py-1 rounded-md border border-border-main">
          {new Date(record.timestamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-x-4 gap-y-5 mb-5">
        <div>
          <div className="flex items-center space-x-2 mb-1">
            <Thermometer size={14} className="text-rose-500" />
            <p className="text-xs text-text-muted font-medium">Temperature</p>
          </div>
          <p className="font-bold text-text-main">
            {record.temperature_c.toFixed(1)}°C
          </p>
          <MetricBar
            value={record.temperature_c}
            max={50}
            colorClass="bg-rose-500"
          />
        </div>
        <div>
          <div className="flex items-center space-x-2 mb-1">
            <Droplets size={14} className="text-cyan-500" />
            <p className="text-xs text-text-muted font-medium">Humidity</p>
          </div>
          <p className="font-bold text-text-main">{record.humidity_percent}%</p>
          <MetricBar
            value={record.humidity_percent}
            max={100}
            colorClass="bg-cyan-500"
          />
        </div>
        <div>
          <div className="flex items-center space-x-2 mb-1">
            <Wind size={14} className="text-primary" />
            <p className="text-xs text-text-muted font-medium">Wind Speed</p>
          </div>
          <p className="font-bold text-text-main">
            {record.wind_speed_ms.toFixed(1)} m/s
          </p>
          <MetricBar
            value={record.wind_speed_ms}
            max={25}
            colorClass="bg-primary"
          />
        </div>
        <div>
          <div className="flex items-center space-x-2 mb-1">
            <TreePine size={14} className="text-amber-500" />
            <p className="text-xs text-text-muted font-medium">Fuel Load</p>
          </div>
          <p className="font-bold text-text-main">
            {record.fuel_load_score.toFixed(1)}
          </p>
          <MetricBar
            value={record.fuel_load_score}
            max={100}
            colorClass="bg-amber-500"
          />
        </div>
      </div>

      <div
        className={cn(
          "rounded-xl p-3 flex items-center justify-between border backdrop-blur-md",
          riskClasses,
        )}
      >
        <div className="flex items-center space-x-2">
          <Flame
            size={18}
            className={
              record.final_fire_risk_percent >= 75 ? "animate-pulse" : ""
            }
          />
          <span className="font-black text-sm tracking-wide">
            {getRiskLabel(record.final_fire_risk_percent)}
          </span>
        </div>
        <span className="font-black text-lg">
          {record.final_fire_risk_percent.toFixed(1)}%
        </span>
      </div>
    </motion.div>
  );
};
