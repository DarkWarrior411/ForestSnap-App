import { motion } from "framer-motion";
import { Thermometer, Droplets, Wind, Flame, MapPin, TreePine } from "lucide-react";
import type { AnalysisRecord } from "../types";
import { cn } from "../utils";

interface AnalysisCardProps {
  record: AnalysisRecord;
  isSelected: boolean;
  onSelect: () => void;
}

export const AnalysisCard: React.FC<AnalysisCardProps> = ({
  record,
  isSelected,
  onSelect,
}) => {
  const getRiskColor = (risk: number) => {
    if (risk < 25) return "text-emerald-400 bg-emerald-500/10 border-emerald-500/20";
    if (risk < 50) return "text-amber-400 bg-amber-500/10 border-amber-500/20";
    if (risk < 75) return "text-orange-400 bg-orange-500/10 border-orange-500/20";
    return "text-red-400 bg-red-500/10 border-red-500/20";
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
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.9, transition: { duration: 0.2 } }}
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      onClick={onSelect}
      className={cn(
        "cursor-pointer rounded-xl border p-5 transition-all duration-300",
        "bg-[#064e3b]/50 backdrop-blur-md hover:shadow-lg hover:shadow-emerald-900/20",
        isSelected ? "border-emerald-500/50 shadow-emerald-500/10" : "border-[#065f46]",
      )}
    >
      <div className="flex justify-between items-start mb-4">
        <div className="flex items-center space-x-2 text-emerald-200">
          <MapPin size={16} className="text-emerald-400" />
          <span className="text-sm font-medium">
            {record.latitude.toFixed(4)}, {record.longitude.toFixed(4)}
          </span>
        </div>
        <span className="text-xs text-emerald-500">
          {new Date(record.timestamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-[#022c22]/50 text-blue-400">
            <Thermometer size={18} />
          </div>
          <div>
            <p className="text-xs text-emerald-400">Temperature</p>
            <p className="font-semibold text-emerald-100">{record.temperature_c.toFixed(1)}°C</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-[#022c22]/50 text-cyan-400">
            <Droplets size={18} />
          </div>
          <div>
            <p className="text-xs text-emerald-400">Humidity</p>
            <p className="font-semibold text-emerald-100">{record.humidity_percent}%</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-[#022c22]/50 text-emerald-300">
            <Wind size={18} />
          </div>
          <div>
            <p className="text-xs text-emerald-400">Wind</p>
            <p className="font-semibold text-emerald-100">{record.wind_speed_ms.toFixed(1)} m/s</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-lg bg-[#022c22]/50 text-emerald-400">
            <TreePine size={18} />
          </div>
          <div>
            <p className="text-xs text-emerald-400">Fuel Load</p>
            <p className="font-semibold text-emerald-100">{record.fuel_load_score.toFixed(1)}</p>
          </div>
        </div>
      </div>

      <div className={cn("rounded-lg p-3 flex items-center justify-between border", riskClasses)}>
        <div className="flex items-center space-x-2">
          <Flame size={20} className="animate-pulse" />
          <span className="font-bold">{getRiskLabel(record.final_fire_risk_percent)}</span>
        </div>
        <span className="font-black text-lg">{record.final_fire_risk_percent.toFixed(1)}%</span>
      </div>
    </motion.div>
  );
};
