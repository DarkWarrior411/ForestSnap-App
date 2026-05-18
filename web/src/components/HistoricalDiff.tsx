import React, { useState, useMemo, ElementType } from "react";
import {
  ArrowRight,
  ArrowUpRight,
  ArrowDownRight,
  Thermometer,
  Droplets,
  Wind,
  Flame,
  TreePine,
  ShieldAlert,
} from "lucide-react";
import type { AnalysisRecord } from "../types";
import { cn } from "../utils";

interface HistoricalDiffProps {
  records: AnalysisRecord[];
}

// MOVED OUTSIDE: Fixes performance degradation and typing issue
const MetricRow = ({
  icon: Icon,
  label,
  baseVal,
  compVal,
  unit,
  higherIsWorse = true,
}: {
  icon: ElementType;
  label: string;
  baseVal: number;
  compVal: number;
  unit: string;
  higherIsWorse?: boolean;
}) => {
  const delta = compVal - baseVal;
  const isNeutral = delta === 0;
  const isBad = higherIsWorse ? delta > 0 : delta < 0;

  let deltaColor = "text-primary";
  if (isNeutral) deltaColor = "text-text-muted";
  else if (isBad) deltaColor = "text-red-500";

  return (
    <div className="flex items-center justify-between p-3 rounded-lg bg-background/50 border border-border-main">
      <div className="flex items-center space-x-3 min-w-[120px]">
        <Icon size={18} className="text-primary shrink-0" />
        <span className="text-sm text-text-main font-medium">{label}</span>
      </div>

      <div className="flex flex-1 items-center justify-end">
        <div className="flex items-center justify-end w-32 space-x-2 shrink-0">
          <span className="text-text-muted font-mono whitespace-nowrap">
            {baseVal.toFixed(1)}
            {unit}
          </span>
          <ArrowRight
            size={14}
            className="text-text-muted opacity-50 shrink-0"
          />
          <span className="text-text-main font-mono font-bold whitespace-nowrap">
            {compVal.toFixed(1)}
            {unit}
          </span>
        </div>

        <div
          className={cn(
            "flex items-center justify-end w-24 font-mono text-sm ml-4 shrink-0",
            deltaColor,
          )}
        >
          {delta > 0 ? (
            <ArrowUpRight size={14} className="mr-1 shrink-0" />
          ) : delta < 0 ? (
            <ArrowDownRight size={14} className="mr-1 shrink-0" />
          ) : null}
          {delta > 0 ? "+" : ""}
          {delta.toFixed(1)}
          {unit}
        </div>
      </div>
    </div>
  );
};

export const HistoricalDiff: React.FC<HistoricalDiffProps> = ({ records }) => {
  const sortedRecords = useMemo(() => {
    return [...records].sort(
      (a, b) =>
        new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime(),
    );
  }, [records]);

  const [baselineId, setBaselineId] = useState<number | "">(
    sortedRecords.length > 1 ? sortedRecords[1].id : "",
  );
  const [comparisonId, setComparisonId] = useState<number | "">(
    sortedRecords.length > 0 ? sortedRecords[0].id : "",
  );

  const baseline = useMemo(
    () => records.find((r) => r.id === Number(baselineId)),
    [records, baselineId],
  );
  const comparison = useMemo(
    () => records.find((r) => r.id === Number(comparisonId)),
    [records, comparisonId],
  );

  const formatDate = (isoString: string) => {
    return new Date(isoString).toLocaleString([], {
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col md:flex-row gap-4 p-5 bg-surface/50 rounded-xl border border-border-main">
        <div className="flex-1 flex flex-col">
          <label className="text-xs text-primary font-bold uppercase mb-2">
            Baseline (Past)
          </label>
          <select
            value={baselineId}
            onChange={(e) => setBaselineId(e.target.value)}
            className="bg-background border border-border-main text-text-main text-sm rounded-lg p-2.5 outline-none cursor-pointer"
          >
            <option value="" disabled>
              Select Baseline...
            </option>
            {sortedRecords.map((r) => (
              <option key={r.id} value={r.id}>
                {formatDate(r.timestamp)} — Risk:{" "}
                {r.final_fire_risk_percent.toFixed(0)}%
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center justify-center px-4 pt-6 hidden md:flex">
          <div className="w-10 h-10 rounded-full bg-background border border-border-main flex items-center justify-center text-primary">
            <ArrowRight size={18} />
          </div>
        </div>

        <div className="flex-1 flex flex-col">
          <label className="text-xs text-primary font-bold uppercase mb-2">
            Comparison (Recent)
          </label>
          <select
            value={comparisonId}
            onChange={(e) => setComparisonId(e.target.value)}
            className="bg-background border border-border-main text-text-main text-sm rounded-lg p-2.5 outline-none cursor-pointer"
          >
            <option value="" disabled>
              Select Comparison...
            </option>
            {sortedRecords.map((r) => (
              <option key={r.id} value={r.id}>
                {formatDate(r.timestamp)} — Risk:{" "}
                {r.final_fire_risk_percent.toFixed(0)}%
              </option>
            ))}
          </select>
        </div>
      </div>

      {!baseline || !comparison ? (
        <div className="text-center py-10 text-text-muted">
          Select two records to view historical diff.
        </div>
      ) : (
        <div className="bg-surface/30 p-5 rounded-xl border border-border-main shadow-inner">
          <div className="flex items-center space-x-2 mb-6 pb-4 border-b border-border-main">
            <ShieldAlert size={20} className="text-primary" />
            <h3 className="text-md font-bold text-text-main">
              Environmental Shift Analysis
            </h3>
          </div>

          <div className="space-y-3">
            <MetricRow
              icon={Thermometer}
              label="Temperature"
              baseVal={baseline.temperature_c}
              compVal={comparison.temperature_c}
              unit="°C"
              higherIsWorse={true}
            />
            <MetricRow
              icon={Droplets}
              label="Humidity"
              baseVal={baseline.humidity_percent}
              compVal={comparison.humidity_percent}
              unit="%"
              higherIsWorse={false}
            />
            <MetricRow
              icon={Wind}
              label="Wind Speed"
              baseVal={baseline.wind_speed_ms}
              compVal={comparison.wind_speed_ms}
              unit="m/s"
              higherIsWorse={true}
            />
            <MetricRow
              icon={TreePine}
              label="Fuel Load"
              baseVal={baseline.fuel_load_score}
              compVal={comparison.fuel_load_score}
              unit=""
              higherIsWorse={true}
            />

            <div className="mt-6 pt-4 border-t border-border-main">
              <MetricRow
                icon={Flame}
                label="Final Fire Risk"
                baseVal={baseline.final_fire_risk_percent}
                compVal={comparison.final_fire_risk_percent}
                unit="%"
                higherIsWorse={true}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
