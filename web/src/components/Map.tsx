import React, {
  useMemo,
  useEffect,
  useRef,
  useState,
  useCallback,
} from "react";
import Map, {
  Source,
  Layer,
  Popup,
  type MapRef,
  type LayerProps,
} from "react-map-gl/mapbox";
import * as turf from "@turf/turf";
import "mapbox-gl/dist/mapbox-gl.css";
import type {
  AnalysisRecord,
  FirmsFirePoint,
  HeatmapSquare,
  GeoJsonFeatureCollection,
} from "../types";
import { Play, Pause, Flame, Droplets, Wind } from "lucide-react";

interface MapProps {
  records: AnalysisRecord[];
  firmsData: FirmsFirePoint[];
  heatmapData: HeatmapSquare[];
  boundaries: GeoJsonFeatureCollection | null;
  selectedRecordId: number | null;
  onMarkerClick: (id: number) => void;
}

// Mapbox API access token from environment variables
const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN;

/**
 * Geospatial MapView component handling vector rendering, satellite overlays, and time-lapse animation.
 */
export const MapView: React.FC<MapProps> = ({
  records,
  firmsData,
  heatmapData,
  boundaries,
  selectedRecordId,
  onMarkerClick,
}) => {
  const mapRef = useRef<MapRef>(null);

  const [timeProgress, setTimeProgress] = useState(100);
  const [isPlaying, setIsPlaying] = useState(false);
  const [hoveredRecord, setHoveredRecord] = useState<AnalysisRecord | null>(
    null,
  );
  const [mapTheme, setMapTheme] = useState(() => {
    if (typeof document !== "undefined") {
      return document.documentElement.classList.contains("dark")
        ? "dark-v11"
        : "light-v11";
    }
    return "dark-v11";
  });

  // Dynamically update map style when application theme switches between dark and light modes
  useEffect(() => {
    const observer = new MutationObserver(() => {
      const darkActive = document.documentElement.classList.contains("dark");
      setMapTheme(darkActive ? "dark-v11" : "light-v11");
    });
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ["class"],
    });
    return () => observer.disconnect();
  }, []);

  // Control time-lapse progression playback interval
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (isPlaying) {
      interval = setInterval(() => {
        setTimeProgress((prev) => {
          if (prev >= 100) {
            setIsPlaying(false);
            return 100;
          }
          return prev + 1;
        });
      }, 150);
    }
    return () => clearInterval(interval);
  }, [isPlaying]);

  // Smooth camera transition to selected snapshot coordinates
  useEffect(() => {
    if (selectedRecordId && mapRef.current) {
      const record = records.find((r) => r.id === selectedRecordId);
      if (record) {
        mapRef.current.flyTo({
          center: [record.longitude, record.latitude],
          zoom: 14,
          duration: 2000,
          essential: true,
        });
      }
    }
  }, [selectedRecordId, records]);

  // Slice survey records based on current time-lapse slider position
  const visibleRecords = useMemo(() => {
    if (records.length === 0) return [];
    const sorted = [...records].sort(
      (a, b) =>
        new Date(a.timestamp).getTime() - new Date(a.timestamp).getTime(),
    );
    const limitIndex = Math.max(
      1,
      Math.ceil((timeProgress / 100) * sorted.length),
    );
    return sorted.slice(0, limitIndex);
  }, [records, timeProgress]);

  // Transform snapshot records into GeoJSON Point features
  const snapsGeoJSON = useMemo(
    () => ({
      type: "FeatureCollection" as const,
      features: visibleRecords.map((record) => ({
        type: "Feature" as const,
        properties: {
          id: record.id,
          risk: record.final_fire_risk_percent,
          recordStr: JSON.stringify(record),
        },
        geometry: {
          type: "Point" as const,
          coordinates: [record.longitude, record.latitude],
        },
      })),
    }),
    [visibleRecords],
  );

  // Compute fire spread fan vectors based on wind speed and direction for high-risk points
  const spreadConesGeoJSON = useMemo(() => {
    const criticalRecords = visibleRecords.filter(
      (r) => r.final_fire_risk_percent >= 75,
    );
    const features = criticalRecords.map((record) => {
      const center = [record.longitude, record.latitude];
      const bearing = (record.wind_direction_deg + 180) % 360;
      const distance = Math.max(0.5, record.wind_speed_ms * 0.25);
      const spreadAngle = 45;

      const p1 = center;
      const p2 = turf.destination(center, distance, bearing - spreadAngle / 2)
        .geometry.coordinates;
      const p3 = turf.destination(center, distance * 1.1, bearing).geometry
        .coordinates;
      const p4 = turf.destination(center, distance, bearing + spreadAngle / 2)
        .geometry.coordinates;

      return turf.polygon([[p1, p2, p3, p4, p1]], {
        risk: record.final_fire_risk_percent,
      });
    });
    return { type: "FeatureCollection" as const, features };
  }, [visibleRecords]);

  // Convert NASA FIRMS satellite data into GeoJSON format
  const firmsGeoJSON = useMemo(
    () => ({
      type: "FeatureCollection" as const,
      features: firmsData.map((fire) => ({
        type: "Feature" as const,
        properties: { brightness: fire.brightness },
        geometry: {
          type: "Point" as const,
          coordinates: [fire.longitude, fire.latitude],
        },
      })),
    }),
    [firmsData],
  );

  // Convert local risk heatmap grid cells into GeoJSON format
  const riskHeatmapGeoJSON = useMemo(
    () => ({
      type: "FeatureCollection" as const,
      features: heatmapData.map((h) => ({
        type: "Feature" as const,
        properties: { avg_risk: h.avg_risk },
        geometry: {
          type: "Point" as const,
          coordinates: [h.center_lon, h.center_lat],
        },
      })),
    }),
    [heatmapData],
  );

  // Styling configurations for vector map layers
  const boundaryLayer: LayerProps = {
    id: "forest-boundaries",
    type: "line",
    paint: {
      "line-color": "#10b981",
      "line-width": 2,
      "line-opacity": 0.5,
      "line-dasharray": [2, 2],
    },
  };

  const boundaryFillLayer: LayerProps = {
    id: "forest-boundaries-fill",
    type: "fill",
    paint: { "fill-color": "#10b981", "fill-opacity": 0.05 },
  };

  const firmsHeatmapLayer: LayerProps = {
    id: "firms-heat",
    type: "heatmap",
    paint: {
      "heatmap-weight": [
        "interpolate",
        ["linear"],
        ["get", "brightness"],
        300,
        0,
        400,
        1,
      ],
      "heatmap-intensity": 1,
      "heatmap-color": [
        "interpolate",
        ["linear"],
        ["heatmap-density"],
        0,
        "rgba(0,0,0,0)",
        0.2,
        "rgba(251,191,36,0.5)",
        0.8,
        "rgba(239,68,68,0.8)",
        1,
        "rgb(153,27,27)",
      ],
      "heatmap-radius": 20,
      "heatmap-opacity": 0.6,
    },
  };

  const localHeatmapLayer: LayerProps = {
    id: "local-heat",
    type: "heatmap",
    paint: {
      "heatmap-weight": [
        "interpolate",
        ["linear"],
        ["get", "avg_risk"],
        0,
        0,
        100,
        1,
      ],
      "heatmap-color": [
        "interpolate",
        ["linear"],
        ["heatmap-density"],
        0,
        "rgba(0,0,0,0)",
        0.4,
        "rgba(16,185,129,0.3)",
        0.8,
        "rgba(245,158,11,0.5)",
        1,
        "rgba(239,68,68,0.6)",
      ],
      "heatmap-radius": 30,
    },
  };

  const spreadConeLayer: LayerProps = {
    id: "spread-cones",
    type: "fill",
    paint: {
      "fill-color": "#ef4444",
      "fill-opacity": 0.25,
      "fill-outline-color": "#991b1b",
    },
  };

  const unclusteredPointLayer: LayerProps = {
    id: "unclustered-point",
    type: "circle",
    paint: {
      "circle-color": [
        "step",
        ["get", "risk"],
        "#10b981",
        25,
        "#f59e0b",
        50,
        "#f97316",
        75,
        "#ef4444",
      ],
      "circle-radius": [
        "case",
        ["boolean", ["feature-state", "hover"], false],
        10,
        7,
      ],
      "circle-stroke-width": 2,
      "circle-stroke-color": "#ffffff",
      "circle-opacity": 0.9,
    },
  };

  const onMouseEnter = useCallback((e: any) => {
    if (e.features && e.features.length > 0) {
      mapRef.current?.getCanvas().style.setProperty("cursor", "pointer");
      const record = JSON.parse(e.features[0].properties.recordStr);
      setHoveredRecord(record);
    }
  }, []);

  const onMouseLeave = useCallback(() => {
    mapRef.current?.getCanvas().style.setProperty("cursor", "grab");
    setHoveredRecord(null);
  }, []);

  const initialViewState = useMemo(() => {
    if (records.length > 0) {
      return {
        latitude: records[0].latitude,
        longitude: records[0].longitude,
        zoom: 6,
      };
    }
    return { latitude: 13.03, longitude: 77.56, zoom: 4 };
  }, [records]);

  return (
    <div className="relative h-full w-full overflow-hidden flex flex-col bg-background">
      <Map
        key={mapTheme}
        ref={mapRef}
        mapboxAccessToken={MAPBOX_TOKEN}
        initialViewState={initialViewState}
        mapStyle={`mapbox://styles/mapbox/${mapTheme}`}
        interactiveLayerIds={["unclustered-point"]}
        onClick={(e) => {
          if (e.features && e.features.length > 0) {
            onMarkerClick(e.features[0].properties?.id);
          }
        }}
        onMouseEnter={onMouseEnter}
        onMouseLeave={onMouseLeave}
      >
        {boundaries && (
          <Source id="boundaries" type="geojson" data={boundaries}>
            <Layer {...boundaryFillLayer} />
            <Layer {...boundaryLayer} />
          </Source>
        )}

        <Source id="local-heatmap" type="geojson" data={riskHeatmapGeoJSON}>
          <Layer {...localHeatmapLayer} />
        </Source>

        <Source id="firms" type="geojson" data={firmsGeoJSON}>
          <Layer {...firmsHeatmapLayer} />
        </Source>

        <Source id="cones" type="geojson" data={spreadConesGeoJSON}>
          <Layer {...spreadConeLayer} />
        </Source>

        <Source id="snaps" type="geojson" data={snapsGeoJSON}>
          <Layer {...unclusteredPointLayer} />
        </Source>

        {hoveredRecord && (
          <Popup
            longitude={hoveredRecord.longitude}
            latitude={hoveredRecord.latitude}
            closeButton={false}
            anchor="bottom"
            offset={12}
            className="custom-map-popup"
            maxWidth="250px"
          >
            <div className="bg-surface p-3 rounded-lg border border-border-main shadow-xl text-text-main">
              <div className="flex items-center justify-between mb-2 pb-2 border-b border-border-main">
                <span className="font-bold text-sm">Risk Profile</span>
                <span
                  className={`font-black text-sm ${hoveredRecord.final_fire_risk_percent >= 75 ? "text-red-500" : "text-primary"}`}
                >
                  {hoveredRecord.final_fire_risk_percent.toFixed(1)}%
                </span>
              </div>
              <div className="space-y-1 text-xs text-text-muted">
                <div className="flex justify-between items-center">
                  <span className="flex items-center gap-1">
                    <Flame size={12} /> Fuel
                  </span>{" "}
                  <span>{hoveredRecord.fuel_load_score.toFixed(1)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="flex items-center gap-1">
                    <Droplets size={12} /> Humid
                  </span>{" "}
                  <span>{hoveredRecord.humidity_percent}%</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="flex items-center gap-1">
                    <Wind size={12} /> Wind
                  </span>{" "}
                  <span>{hoveredRecord.wind_speed_ms.toFixed(1)}m/s</span>
                </div>
              </div>
            </div>
          </Popup>
        )}
      </Map>

      {/* Floating time-lapse playback control bar */}
      <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 bg-background/95 backdrop-blur-md p-4 rounded-2xl border border-border-main w-11/12 max-w-lg flex items-center gap-4 shadow-2xl z-10">
        <button
          onClick={() => {
            if (timeProgress >= 100) setTimeProgress(0);
            setIsPlaying(!isPlaying);
          }}
          className={`p-3 rounded-full text-white transition-colors shadow-lg ${isPlaying ? "bg-amber-500 hover:bg-amber-400" : "bg-primary hover:bg-emerald-400"}`}
        >
          {isPlaying ? (
            <Pause size={20} fill="currentColor" />
          ) : (
            <Play size={20} fill="currentColor" className="ml-0.5" />
          )}
        </button>
        <div className="flex-1 flex flex-col pt-1">
          <input
            type="range"
            min="0"
            max="100"
            value={timeProgress}
            onChange={(e) => {
              setTimeProgress(Number(e.target.value));
              setIsPlaying(false);
            }}
            className="w-full accent-primary h-2 bg-surface rounded-lg appearance-none cursor-pointer"
          />
          <div className="flex justify-between text-[11px] text-text-muted font-bold mt-2 uppercase tracking-widest">
            <span>Historical</span>
            <span className="opacity-40">Time Progression</span>
            <span>Live Sync</span>
          </div>
        </div>
      </div>
    </div>
  );
};
