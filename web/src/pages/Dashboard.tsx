import { useState, useMemo, useRef, useEffect } from "react";
import { useVirtualizer } from "@tanstack/react-virtual";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { MapView } from "../components/Map";
import { AnalysisCard } from "../components/AnalysisCard";
import { ScenarioEditor } from "../components/ScenarioEditor";
import { HistoricalDiff } from "../components/HistoricalDiff";
import { ErrorBoundary } from "../components/ErrorBoundary";
import { AnimatePresence, motion } from "framer-motion";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Cell,
  LineChart,
  Line,
  Legend,
  Radar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
} from "recharts";
import {
  fetchHistory,
  fetchGlobalFires,
  fetchHeatmap,
  fetchBoundaries,
  fetchAlerts,
  fetchWeatherForecast,
} from "../api";
import {
  RefreshCw,
  Download,
  Filter,
  Activity,
  List,
  Clock,
  Target,
  CloudSun,
  Sliders,
  History,
  Calendar,
} from "lucide-react";
import type { AnalysisRecord } from "../types";

// Skeleton Component for Feed
const FeedSkeleton = () => (
  <div className="space-y-4">
    {[1, 2, 3, 4].map((i) => (
      <div
        key={i}
        className="skeleton-pulse h-[260px] w-full rounded-2xl bg-surface/50"
      />
    ))}
  </div>
);

export function Dashboard() {
  const queryClient = useQueryClient();
  const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000";

  // --- UI STATE ---
  const [activeTab, setActiveTab] = useState<
    "feed" | "analytics" | "sandbox" | "audit"
  >("feed");
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);

  // --- FILTER STATE ---
  const [filterRisk, setFilterRisk] = useState<
    "All" | "Low" | "Moderate" | "High" | "Critical"
  >("All");
  const [filterTime, setFilterTime] = useState<"All" | "24h" | "7d">("All");

  const [virtualRecord, setVirtualRecord] = useState<AnalysisRecord | null>(
    null,
  );
  const [auditDaysA, setAuditDaysA] = useState<number>(30);
  const [auditDaysB, setAuditDaysB] = useState<number>(60);

  const parentRef = useRef<HTMLDivElement>(null);
  const activeTabRef = useRef(activeTab);

  useEffect(() => {
    activeTabRef.current = activeTab;
  }, [activeTab]);

  // --- REACT QUERY DATA FETCHING ---
  const {
    data: records = [],
    isLoading: recordsLoading,
    refetch: refetchRecords,
    isRefetching,
  } = useQuery({
    queryKey: ["history"],
    queryFn: () => fetchHistory(0, 1000),
    staleTime: 5 * 60 * 1000,
  });

  const { data: firmsData = [] } = useQuery({
    queryKey: ["firms"],
    queryFn: fetchGlobalFires,
    refetchInterval: 60000,
  });
  const { data: boundaries = null } = useQuery({
    queryKey: ["boundaries"],
    queryFn: fetchBoundaries,
    staleTime: Infinity,
  });
  const { data: alerts = [] } = useQuery({
    queryKey: ["alerts"],
    queryFn: fetchAlerts,
    refetchInterval: 10000,
  });

  const { data: heatmapData = [] } = useQuery({
    queryKey: ["heatmap", records.length],
    queryFn: () => {
      if (!records.length) return [];
      const minLat = Math.min(...records.map((r) => r.latitude)) - 0.1;
      const maxLat = Math.max(...records.map((r) => r.latitude)) + 0.1;
      const minLon = Math.min(...records.map((r) => r.longitude)) - 0.1;
      const maxLon = Math.max(...records.map((r) => r.longitude)) + 0.1;
      return fetchHeatmap(minLat, maxLat, minLon, maxLon);
    },
    enabled: records.length > 0,
  });

  const mapCenterLat = records.length > 0 ? records[0].latitude : 13.03;
  const mapCenterLon = records.length > 0 ? records[0].longitude : 77.56;
  const { data: forecastData = null } = useQuery({
    queryKey: ["forecast", mapCenterLat, mapCenterLon],
    queryFn: () => fetchWeatherForecast(mapCenterLat, mapCenterLon),
    enabled: records.length > 0,
  });

  // --- LIVE SSE STREAM INTEGRATION ---
  useEffect(() => {
    let eventSource: EventSource;

    const connectTimer = setTimeout(() => {
      eventSource = new EventSource(`${API_BASE_URL}/stream`);

      eventSource.addEventListener("new_record", (event) => {
        const newRecord: AnalysisRecord = JSON.parse(event.data);

        queryClient.setQueryData(
          ["history"],
          (oldData: AnalysisRecord[] | undefined) => {
            if (!oldData) return [newRecord];
            return [newRecord, ...oldData];
          },
        );

        if (activeTabRef.current === "feed") {
          setSelectedRecordId(newRecord.id);
        }
      });

      eventSource.onerror = () => {
        if (eventSource.readyState === EventSource.CLOSED) {
          eventSource.close();
        }
      };
    }, 500);

    return () => {
      clearTimeout(connectTimer);
      if (eventSource) eventSource.close();
    };
  }, [queryClient, API_BASE_URL]);

  useEffect(() => {
    if (records.length > 0 && selectedRecordId === null)
      setSelectedRecordId(records[0].id);
  }, [records, selectedRecordId]);

  // --- FILTERS & COMPUTATIONS ---
  const filteredRecords = useMemo(() => {
    return records.filter((r) => {
      if (filterRisk === "Low" && r.final_fire_risk_percent >= 25) return false;
      if (
        filterRisk === "Moderate" &&
        (r.final_fire_risk_percent < 25 || r.final_fire_risk_percent >= 50)
      )
        return false;
      if (
        filterRisk === "High" &&
        (r.final_fire_risk_percent < 50 || r.final_fire_risk_percent >= 75)
      )
        return false;
      if (filterRisk === "Critical" && r.final_fire_risk_percent < 75)
        return false;

      if (filterTime !== "All") {
        const recordDate = new Date(r.timestamp).getTime();
        const diffHours = (Date.now() - recordDate) / (1000 * 60 * 60);
        if (filterTime === "24h" && diffHours > 24) return false;
        if (filterTime === "7d" && diffHours > 24 * 7) return false;
      }
      return true;
    });
  }, [records, filterRisk, filterTime]);

  const rowVirtualizer = useVirtualizer({
    count: filteredRecords.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 260, // Baseline estimate, will dynamically adjust
    overscan: 3,
  });

  const selectedRecord = useMemo(
    () =>
      filteredRecords.find((r) => r.id === selectedRecordId) ||
      filteredRecords[0],
    [filteredRecords, selectedRecordId],
  );

  const mapRecords = useMemo(() => {
    return activeTab === "sandbox" && virtualRecord
      ? [...filteredRecords, virtualRecord]
      : filteredRecords;
  }, [activeTab, virtualRecord, filteredRecords]);

  // --- ANALYTICS ---
  const riskDistribution = useMemo(() => {
    const dist = { Low: 0, Moderate: 0, High: 0, Critical: 0 };
    filteredRecords.forEach((r) => {
      if (r.final_fire_risk_percent < 25) dist.Low++;
      else if (r.final_fire_risk_percent < 50) dist.Moderate++;
      else if (r.final_fire_risk_percent < 75) dist.High++;
      else dist.Critical++;
    });
    return [
      { name: "Low", count: dist.Low, fill: "#34d399" },
      { name: "Moderate", count: dist.Moderate, fill: "#fbbf24" },
      { name: "High", count: dist.High, fill: "#f97316" },
      { name: "Critical", count: dist.Critical, fill: "#ef4444" },
    ];
  }, [filteredRecords]);

  const radarData = useMemo(() => {
    if (!selectedRecord) return [];
    return [
      {
        subject: "Temp",
        value: Math.min((selectedRecord.temperature_c / 50) * 100, 100),
      },
      { subject: "Dryness", value: 100 - selectedRecord.humidity_percent },
      {
        subject: "Wind",
        value: Math.min((selectedRecord.wind_speed_ms / 20) * 100, 100),
      },
      { subject: "Fuel", value: selectedRecord.fuel_load_score },
      { subject: "Risk", value: (selectedRecord.dryness_risk_tier / 5) * 100 },
    ];
  }, [selectedRecord]);

  const forecastChartData = useMemo(() => {
    if (!forecastData?.hourly) return [];
    return forecastData.hourly.time.slice(0, 48).map((t: string, i: number) => {
      const temp = forecastData.hourly.temperature_2m[i];
      const hum = forecastData.hourly.relative_humidity_2m[i];
      const Risk = Math.max(0, Math.min(100, temp * 2.5 - hum * 0.5));
      return {
        time: new Date(t).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        }),
        Temp: temp,
        Humidity: hum,
        Risk: parseFloat(Risk.toFixed(1)),
      };
    });
  }, [forecastData]);

  // --- AUDIT DATA ---
  const auditData = useMemo(() => {
    const now = Date.now();
    const periodAMs = auditDaysA * 24 * 60 * 60 * 1000;
    const periodBMs = auditDaysB * 24 * 60 * 60 * 1000;

    const pA = records.filter(
      (r) => now - new Date(r.timestamp).getTime() <= periodAMs,
    );
    const pB = records.filter((r) => {
      const d = now - new Date(r.timestamp).getTime();
      return d > periodAMs && d <= periodBMs;
    });

    const getAvg = (arr: any[], k: string) =>
      arr.length ? arr.reduce((sum, r) => sum + r[k], 0) / arr.length : 0;
    return [
      {
        metric: "Avg Risk (%)",
        PeriodA: parseFloat(getAvg(pA, "final_fire_risk_percent").toFixed(1)),
        PeriodB: parseFloat(getAvg(pB, "final_fire_risk_percent").toFixed(1)),
      },
      {
        metric: "Avg Temp (°C)",
        PeriodA: parseFloat(getAvg(pA, "temperature_c").toFixed(1)),
        PeriodB: parseFloat(getAvg(pB, "temperature_c").toFixed(1)),
      },
      {
        metric: "Avg Fuel Load",
        PeriodA: parseFloat(getAvg(pA, "fuel_load_score").toFixed(1)),
        PeriodB: parseFloat(getAvg(pB, "fuel_load_score").toFixed(1)),
      },
    ];
  }, [records, auditDaysA, auditDaysB]);

  const exportCSV = () => {
    if (!filteredRecords.length) return;
    const headers = Object.keys(filteredRecords[0]).join(",");
    const rows = filteredRecords
      .map((r) =>
        Object.values(r)
          .map((v) => '"' + String(v).replace(/"/g, '""') + '"')
          .join(","),
      )
      .join("\n");
    const blob = new Blob([`${headers}\n${rows}`], { type: "text/csv" });
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "forestsnap_export.csv";
    a.click();
  };

  return (
    <div className="flex-1 relative w-full h-full min-h-0 overflow-hidden bg-background">
      <div className="absolute inset-0 z-0">
        <ErrorBoundary>
          <MapView
            records={mapRecords}
            firmsData={firmsData}
            heatmapData={heatmapData}
            boundaries={boundaries}
            selectedRecordId={selectedRecordId}
            onMarkerClick={(id) => setSelectedRecordId(id)}
          />
        </ErrorBoundary>
      </div>

      <div className="absolute inset-0 z-10 pointer-events-none flex flex-col p-4 md:p-6 overflow-hidden">
        {/* HEADER CONTROLS */}
        <div className="flex flex-col xl:flex-row justify-between items-start xl:items-center gap-4 shrink-0 w-full">
          <div className="glass-panel p-2 rounded-2xl flex overflow-x-auto pointer-events-auto custom-scrollbar w-full xl:w-auto shadow-2xl">
            {(["feed", "analytics", "sandbox", "audit"] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-5 py-2.5 rounded-xl text-sm font-bold flex items-center space-x-2 transition-all capitalize whitespace-nowrap ${activeTab === tab ? "bg-primary text-white shadow-lg shadow-primary/30" : "text-text-muted hover:text-text-main hover:bg-surface"}`}
              >
                {tab === "feed" && <List size={16} />}
                {tab === "analytics" && <Activity size={16} />}
                {tab === "sandbox" && <Sliders size={16} />}
                {tab === "audit" && <History size={16} />}
                <span>{tab}</span>
              </button>
            ))}
          </div>

          <div className="flex flex-wrap items-center gap-3 ml-auto pointer-events-auto">
            {activeTab !== "sandbox" && activeTab !== "audit" && (
              <>
                <div className="glass-panel flex items-center space-x-2 px-3 py-2.5 rounded-xl">
                  <Filter size={16} className="text-primary" />
                  <select
                    value={filterRisk}
                    onChange={(e) => setFilterRisk(e.target.value as any)}
                    className="bg-transparent text-text-main text-sm outline-none cursor-pointer font-medium"
                  >
                    <option value="All">All Risks</option>
                    <option value="Low">Low Risk</option>
                    <option value="Moderate">Moderate Risk</option>
                    <option value="High">High Risk</option>
                    <option value="Critical">Critical Risk</option>
                  </select>
                </div>
                <div className="glass-panel flex items-center space-x-2 px-3 py-2.5 rounded-xl">
                  <Clock size={16} className="text-primary" />
                  <select
                    value={filterTime}
                    onChange={(e) => setFilterTime(e.target.value as any)}
                    className="bg-transparent text-text-main text-sm outline-none cursor-pointer font-medium"
                  >
                    <option value="All">All Time</option>
                    <option value="24h">Last 24 Hours</option>
                    <option value="7d">Last 7 Days</option>
                  </select>
                </div>
              </>
            )}

            <button
              onClick={exportCSV}
              className="glass-panel flex items-center space-x-2 px-4 py-2.5 rounded-xl text-sm font-bold text-text-main hover:bg-surface/80 transition-colors"
            >
              <Download size={16} className="text-primary" />{" "}
              <span className="hidden sm:inline">Export</span>
            </button>
            <button
              onClick={() => refetchRecords()}
              disabled={isRefetching}
              className="flex items-center space-x-2 px-4 py-2.5 bg-primary hover:bg-primary/90 rounded-xl text-sm font-bold text-white transition-colors shadow-lg shadow-primary/20 disabled:opacity-50"
            >
              <RefreshCw
                size={16}
                className={isRefetching ? "animate-spin" : ""}
              />{" "}
              <span className="hidden sm:inline">Refresh</span>
            </button>
          </div>
        </div>

        {/* FLOATING PANELS AREA */}
        <div className="flex-1 mt-6 relative min-h-0 w-full pointer-events-none">
          <AnimatePresence mode="wait">
            {/* FEED PANEL - *FIXED DYNAMIC HEIGHT MEASURING* */}
            {activeTab === "feed" && (
              <motion.div
                key="feed"
                initial={{ opacity: 0, x: -40 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -40 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
                className="absolute inset-y-0 left-0 w-full max-w-[420px] glass-panel pointer-events-auto rounded-3xl overflow-hidden shadow-2xl flex flex-col"
              >
                <div className="p-4 bg-surface/80 border-b border-border-main backdrop-blur-md">
                  <h2 className="font-bold text-text-main flex items-center gap-2">
                    <List size={18} className="text-primary" /> Recent Analysis
                    Logs
                  </h2>
                  <p className="text-xs text-text-muted mt-1">
                    {filteredRecords.length} records matching criteria
                  </p>
                </div>
                <div className="flex-1 overflow-y-auto p-4 custom-scrollbar">
                  {recordsLoading ? (
                    <FeedSkeleton />
                  ) : (
                    <div
                      ref={parentRef}
                      style={{ height: "100%", overflow: "auto" }}
                      className="custom-scrollbar pr-2"
                    >
                      <div
                        style={{
                          height: `${rowVirtualizer.getTotalSize()}px`,
                          width: "100%",
                          position: "relative",
                        }}
                      >
                        {rowVirtualizer.getVirtualItems().map((vItem) => {
                          const record = filteredRecords[vItem.index];
                          return (
                            <div
                              key={vItem.key}
                              data-index={vItem.index}
                              ref={rowVirtualizer.measureElement}
                              style={{
                                position: "absolute",
                                top: 0,
                                left: 0,
                                width: "100%",
                                transform: `translateY(${vItem.start}px)`,
                              }}
                            >
                              <div className="pb-4">
                                <AnalysisCard
                                  record={record}
                                  isSelected={selectedRecordId === record.id}
                                  onSelect={() =>
                                    setSelectedRecordId(record.id)
                                  }
                                />
                              </div>
                            </div>
                          );
                        })}
                      </div>
                      {filteredRecords.length === 0 && (
                        <div className="text-center py-10 text-text-muted">
                          No records found matching filters.
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </motion.div>
            )}

            {activeTab === "analytics" && (
              <motion.div
                key="analytics"
                initial={{ opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 40 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
                className="absolute inset-x-0 bottom-0 top-auto md:top-0 md:inset-y-0 md:left-0 md:w-full md:max-w-4xl glass-panel pointer-events-auto rounded-3xl overflow-y-auto p-6 custom-scrollbar shadow-2xl flex flex-col gap-6"
              >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-surface/60 p-5 rounded-2xl border border-border-main">
                    <h3 className="text-sm font-bold text-text-main mb-4 flex items-center gap-2">
                      <Target size={16} className="text-primary" /> Risk Profile
                      Matrix
                    </h3>
                    <div className="w-full h-56">
                      <ResponsiveContainer
                        width="100%"
                        height="100%"
                        minWidth={1}
                        minHeight={1}
                      >
                        <RadarChart
                          cx="50%"
                          cy="50%"
                          outerRadius="70%"
                          data={radarData}
                        >
                          <PolarGrid stroke="var(--color-border-main)" />
                          <PolarAngleAxis
                            dataKey="subject"
                            tick={{
                              fill: "var(--color-text-muted)",
                              fontSize: 11,
                            }}
                          />
                          <Radar
                            dataKey="value"
                            stroke="#ef4444"
                            fill="#ef4444"
                            fillOpacity={0.4}
                          />
                        </RadarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>

                  <div className="bg-surface/60 p-5 rounded-2xl border border-border-main">
                    <h3 className="text-sm font-bold text-text-main mb-4 flex items-center gap-2">
                      <Activity size={16} className="text-primary" /> Severity
                      Breakdown
                    </h3>
                    <div className="w-full h-56">
                      <ResponsiveContainer
                        width="100%"
                        height="100%"
                        minWidth={1}
                        minHeight={1}
                      >
                        <BarChart data={riskDistribution}>
                          <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="var(--color-border-main)"
                            vertical={false}
                          />
                          <XAxis
                            dataKey="name"
                            stroke="var(--color-text-muted)"
                            fontSize={12}
                            tickLine={false}
                            axisLine={false}
                          />
                          <YAxis
                            stroke="var(--color-text-muted)"
                            fontSize={12}
                            tickLine={false}
                            axisLine={false}
                          />
                          <Tooltip
                            cursor={{ fill: "var(--color-surface)" }}
                            contentStyle={{
                              backgroundColor: "var(--color-background)",
                              border: "1px solid var(--color-border-main)",
                              borderRadius: "8px",
                              color: "var(--color-text-main)",
                            }}
                          />
                          <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                            {riskDistribution.map((e, i) => (
                              <Cell key={i} fill={e.fill} />
                            ))}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                </div>

                <div className="bg-surface/60 p-5 rounded-2xl border border-border-main min-h-[300px]">
                  <h3 className="text-sm font-bold text-text-main mb-6 flex items-center gap-2">
                    <CloudSun size={16} className="text-primary" /> 48-Hour
                    Proactive Forecast
                  </h3>
                  <div className="w-full h-64">
                    <ResponsiveContainer
                      width="100%"
                      height="100%"
                      minWidth={1}
                      minHeight={1}
                    >
                      <LineChart
                        data={forecastChartData}
                        margin={{ top: 5, right: 20, left: -20, bottom: 5 }}
                      >
                        <CartesianGrid
                          strokeDasharray="3 3"
                          stroke="var(--color-border-main)"
                          vertical={false}
                        />
                        <XAxis
                          dataKey="time"
                          stroke="var(--color-text-muted)"
                          fontSize={12}
                          tickLine={false}
                          axisLine={false}
                          minTickGap={30}
                        />
                        <YAxis
                          stroke="var(--color-text-muted)"
                          fontSize={12}
                          tickLine={false}
                          axisLine={false}
                        />
                        <Tooltip
                          contentStyle={{
                            backgroundColor: "var(--color-background)",
                            border: "1px solid var(--color-border-main)",
                            borderRadius: "8px",
                            color: "var(--color-text-main)",
                          }}
                        />
                        <Legend wrapperStyle={{ paddingTop: "20px" }} />
                        <Line
                          type="monotone"
                          name="Temp (°C)"
                          dataKey="Temp"
                          stroke="#ef4444"
                          strokeWidth={2}
                          dot={false}
                        />
                        <Line
                          type="monotone"
                          name="Humidity (%)"
                          dataKey="Humidity"
                          stroke="#06b6d4"
                          strokeWidth={2}
                          dot={false}
                        />
                        <Line
                          type="monotone"
                          name="Risk Curve"
                          dataKey="Risk"
                          stroke="#f59e0b"
                          strokeWidth={3}
                          strokeDasharray="5 5"
                          dot={false}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </motion.div>
            )}

            {activeTab === "sandbox" && (
              <motion.div
                key="sandbox"
                initial={{ opacity: 0, x: -40 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -40 }}
                transition={{ duration: 0.3 }}
                className="absolute inset-y-0 left-0 w-full max-w-md glass-panel pointer-events-auto rounded-3xl overflow-y-auto p-6 custom-scrollbar shadow-2xl flex flex-col gap-6"
              >
                <div className="bg-surface/60 p-6 rounded-2xl border border-border-main flex flex-col justify-center items-center text-center">
                  <h2 className="text-3xl font-black mb-2 text-text-main">
                    Simulated Risk:{" "}
                    <span
                      className={
                        (virtualRecord?.final_fire_risk_percent || 0) > 75
                          ? "text-red-500"
                          : "text-primary"
                      }
                    >
                      {virtualRecord?.final_fire_risk_percent.toFixed(1)}%
                    </span>
                  </h2>
                  <p className="text-text-muted text-sm">
                    Adjust parameters below. The predictive spread cone on the
                    map updates instantly.
                  </p>
                </div>
                <ScenarioEditor
                  baseRecord={selectedRecord}
                  onSimulate={(record) => setVirtualRecord(record)}
                />
              </motion.div>
            )}

            {activeTab === "audit" && (
              <motion.div
                key="audit"
                initial={{ opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 40 }}
                transition={{ duration: 0.3 }}
                className="absolute inset-x-0 bottom-0 top-auto md:top-0 md:inset-y-0 md:left-0 md:w-full md:max-w-4xl glass-panel pointer-events-auto rounded-3xl overflow-y-auto p-6 custom-scrollbar shadow-2xl flex flex-col gap-6"
              >
                <HistoricalDiff records={records} />
                <div className="flex flex-col md:flex-row gap-4 items-start md:items-center bg-surface/60 p-5 rounded-2xl border border-border-main">
                  <div className="flex items-center gap-2">
                    <Calendar className="text-primary" />
                    <span className="text-xs text-primary font-bold uppercase tracking-wider">
                      Compare
                    </span>
                  </div>
                  <select
                    value={auditDaysA}
                    onChange={(e) => setAuditDaysA(Number(e.target.value))}
                    className="bg-background text-text-main text-sm font-bold p-2.5 rounded-lg outline-none cursor-pointer border border-border-main"
                  >
                    <option value={7}>Last 7 Days (A)</option>
                    <option value={30}>Last 30 Days (A)</option>
                  </select>
                  <span className="text-text-muted font-black px-2 hidden md:block">
                    VS
                  </span>
                  <select
                    value={auditDaysB}
                    onChange={(e) => setAuditDaysB(Number(e.target.value))}
                    className="bg-background text-text-main text-sm font-bold p-2.5 rounded-lg outline-none cursor-pointer border border-border-main"
                  >
                    <option value={30}>Previous 30 Days (B)</option>
                    <option value={60}>Previous 60 Days (B)</option>
                    <option value={365}>Previous Year (B)</option>
                  </select>
                </div>
                <div className="bg-surface/60 p-5 rounded-2xl border border-border-main flex-1 min-h-[300px]">
                  <h3 className="text-sm font-bold text-text-main mb-6">
                    Historical Delta (A vs B)
                  </h3>
                  <div className="w-full h-72">
                    <ResponsiveContainer
                      width="100%"
                      height="100%"
                      minWidth={1}
                      minHeight={1}
                    >
                      <BarChart
                        data={auditData}
                        margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                      >
                        <CartesianGrid
                          strokeDasharray="3 3"
                          stroke="var(--color-border-main)"
                          vertical={false}
                        />
                        <XAxis
                          dataKey="metric"
                          stroke="var(--color-text-muted)"
                          fontSize={12}
                          tickLine={false}
                          axisLine={false}
                        />
                        <YAxis
                          stroke="var(--color-text-muted)"
                          fontSize={12}
                          tickLine={false}
                          axisLine={false}
                        />
                        <Tooltip
                          cursor={{ fill: "var(--color-surface)" }}
                          contentStyle={{
                            backgroundColor: "var(--color-background)",
                            border: "1px solid var(--color-border-main)",
                            borderRadius: "12px",
                            color: "var(--color-text-main)",
                          }}
                        />
                        <Legend wrapperStyle={{ paddingTop: "20px" }} />
                        <Bar
                          dataKey="PeriodA"
                          fill="#10b981"
                          name="Recent (A)"
                          radius={[4, 4, 0, 0]}
                        />
                        <Bar
                          dataKey="PeriodB"
                          fill="var(--color-border-main)"
                          name="Past (B)"
                          radius={[4, 4, 0, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}
