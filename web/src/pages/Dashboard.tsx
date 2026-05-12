import { useEffect, useState, useMemo } from "react";
import { fetchHistory } from "../api";
import type { AnalysisRecord } from "../types";
import { MapView } from "../components/Map";
import { AnalysisCard } from "../components/AnalysisCard";
import { RefreshCw, Download, Filter, Activity, List } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell } from "recharts";

export function Dashboard() {
  const [records, setRecords] = useState<AnalysisRecord[]>([]);
  const [selectedRecordId, setSelectedRecordId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<"feed" | "analytics">("feed");
  const [filterRisk, setFilterRisk] = useState<"All" | "Low" | "Moderate" | "High" | "Critical">("All");
  const [showToast, setShowToast] = useState(false);

  const loadData = async () => {
    setIsLoading(true);
    const data = await fetchHistory();
    setRecords(data);
    if (data.length > 0 && selectedRecordId === null) {
      setSelectedRecordId(data[0].id);
    }
    setIsLoading(false);
    
    // Show toast
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredRecords = useMemo(() => {
    return records.filter((r) => {
      if (filterRisk === "All") return true;
      if (filterRisk === "Low") return r.final_fire_risk_percent < 25;
      if (filterRisk === "Moderate") return r.final_fire_risk_percent >= 25 && r.final_fire_risk_percent < 50;
      if (filterRisk === "High") return r.final_fire_risk_percent >= 50 && r.final_fire_risk_percent < 75;
      if (filterRisk === "Critical") return r.final_fire_risk_percent >= 75;
      return true;
    });
  }, [records, filterRisk]);

  const exportCSV = () => {
    if (filteredRecords.length === 0) return;
    const headers = Object.keys(filteredRecords[0]).join(",");
    const rows = filteredRecords.map(r => Object.values(r).map(v => '"' + String(v).replace(/"/g, '""') + '"').join(",")).join("\n");
    const csv = `${headers}\n${rows}`;
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "forestsnap_export.csv";
    a.click();
  };

  // Prepare chart data
  const riskDistribution = useMemo(() => {
    const dist = { Low: 0, Moderate: 0, High: 0, Critical: 0 };
    filteredRecords.forEach(r => {
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

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden p-6 gap-6">
      {/* Toast Notification */}
      <AnimatePresence>
        {showToast && (
          <motion.div
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -50 }}
            className="absolute top-4 left-1/2 transform -translate-x-1/2 z-50 bg-emerald-500 text-[#022c22] px-4 py-2 rounded-full font-bold shadow-lg flex items-center space-x-2"
          >
            <Activity size={16} />
            <span>Data Refreshed</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-[#064e3b]/50 p-4 rounded-2xl border border-[#065f46]">
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2 bg-[#022c22] px-3 py-1.5 rounded-lg border border-[#065f46]">
            <Filter size={16} className="text-emerald-400" />
            <select
              value={filterRisk}
              onChange={(e) => setFilterRisk(e.target.value as "All" | "Low" | "Moderate" | "High" | "Critical")}
              className="bg-transparent text-emerald-50 text-sm outline-none cursor-pointer"
            >
              <option value="All">All Risks</option>
              <option value="Low">Low Risk</option>
              <option value="Moderate">Moderate Risk</option>
              <option value="High">High Risk</option>
              <option value="Critical">Critical Risk</option>
            </select>
          </div>
          
          <div className="flex space-x-1 bg-[#022c22] p-1 rounded-lg border border-[#065f46]">
            <button
              onClick={() => setActiveTab("feed")}
              className={`px-3 py-1.5 rounded-md text-sm font-medium flex items-center space-x-1 transition-colors ${activeTab === "feed" ? "bg-[#064e3b] text-emerald-300" : "text-emerald-100/60 hover:text-emerald-100"}`}
            >
              <List size={14} /> <span>Feed</span>
            </button>
            <button
              onClick={() => setActiveTab("analytics")}
              className={`px-3 py-1.5 rounded-md text-sm font-medium flex items-center space-x-1 transition-colors ${activeTab === "analytics" ? "bg-[#064e3b] text-emerald-300" : "text-emerald-100/60 hover:text-emerald-100"}`}
            >
              <Activity size={14} /> <span>Analytics</span>
            </button>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={exportCSV}
            className="flex items-center space-x-2 px-4 py-2 bg-[#022c22] hover:bg-[#022c22]/80 border border-[#065f46] rounded-lg text-sm font-medium transition-colors text-emerald-100"
          >
            <Download size={16} />
            <span>Export</span>
          </button>
          <button
            onClick={loadData}
            disabled={isLoading}
            className="flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 rounded-lg text-sm font-bold transition-colors disabled:opacity-50 text-[#022c22]"
          >
            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-6 overflow-hidden">
        {/* Left Column */}
        <div className="flex flex-col h-full overflow-hidden bg-[#064e3b]/30 rounded-2xl border border-[#065f46]">
          {activeTab === "feed" ? (
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              <AnimatePresence>
                {filteredRecords.map((record) => (
                  <AnalysisCard
                    key={record.id}
                    record={record}
                    isSelected={selectedRecordId === record.id}
                    onSelect={() => setSelectedRecordId(record.id)}
                  />
                ))}
              </AnimatePresence>
              {!isLoading && filteredRecords.length === 0 && (
                <div className="text-center py-10 text-emerald-100/50">
                  <p>No records match the current filter.</p>
                </div>
              )}
            </div>
          ) : (
            <div className="flex-1 p-6 flex flex-col items-center justify-center">
              <h3 className="text-xl font-bold mb-6 text-emerald-100">Risk Distribution</h3>
              <div className="w-full h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={riskDistribution}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#065f46" vertical={false} />
                    <XAxis dataKey="name" stroke="#a7f3d0" />
                    <YAxis stroke="#a7f3d0" allowDecimals={false} />
                    <Tooltip cursor={{fill: 'transparent'}} contentStyle={{ backgroundColor: '#022c22', borderColor: '#065f46', color: '#ecfdf5' }} />
                    <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                      {
                        riskDistribution.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.fill} />
                        ))
                      }
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Map */}
        <div className="h-[40vh] lg:h-full rounded-2xl overflow-hidden border border-[#065f46]">
          <MapView
            records={filteredRecords}
            selectedRecordId={selectedRecordId}
            onMarkerClick={(id) => setSelectedRecordId(id)}
          />
        </div>
      </div>
    </div>
  );
}
