import React, { useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { AnalysisRecord } from "../types";

// Fix for default Leaflet markers in React
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

interface MapProps {
  records: AnalysisRecord[];
  selectedRecordId: number | null;
  onMarkerClick: (id: number) => void;
}

// Component to recenter map when selected record changes
const MapController: React.FC<{ selectedRecord: AnalysisRecord | null }> = ({ selectedRecord }) => {
  const map = useMap();
  useEffect(() => {
    if (selectedRecord) {
      map.flyTo([selectedRecord.latitude, selectedRecord.longitude], 14, {
        duration: 1.5,
      });
    }
  }, [selectedRecord, map]);
  return null;
};

export const MapView: React.FC<MapProps> = ({ records, selectedRecordId, onMarkerClick }) => {
  const defaultCenter: [number, number] = records.length > 0 
    ? [records[0].latitude, records[0].longitude] 
    : [37.7749, -122.4194]; // Default to SF if no data

  const selectedRecord = records.find((r) => r.id === selectedRecordId) || null;

  return (
    <div className="h-full w-full overflow-hidden">
      <MapContainer
        center={defaultCenter}
        zoom={10}
        style={{ height: "100%", width: "100%" }}
        className="z-0"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" // Dark theme map
        />
        {records.map((record) => (
          <Marker
            key={record.id}
            position={[record.latitude, record.longitude]}
            eventHandlers={{
              click: () => onMarkerClick(record.id),
            }}
          >
            <Popup className="bg-[#064e3b] text-emerald-50 rounded-lg border border-[#065f46]">
              <div className="font-semibold text-emerald-400">Risk: {record.final_fire_risk_percent.toFixed(1)}%</div>
              <div className="text-sm">Temp: {record.temperature_c.toFixed(1)}°C</div>
              <div className="text-sm">Wind: {record.wind_speed_ms.toFixed(1)} m/s</div>
            </Popup>
          </Marker>
        ))}
        <MapController selectedRecord={selectedRecord} />
      </MapContainer>
    </div>
  );
};
