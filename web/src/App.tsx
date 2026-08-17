import { Suspense, lazy } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Layout } from "./components/Layout";
import { LandingPage } from "./pages/LandingPage";
import { AboutPage } from "./pages/AboutPage";
import { AppPromoPage } from "./pages/AppPromoPage";
import { Loader2 } from "lucide-react";

// Lazy-loaded Dashboard module for route-based code splitting
const Dashboard = lazy(() =>
  import("./pages/Dashboard").then((m) => ({ default: m.Dashboard })),
);

// Fallback spinner component rendered while the heavy Dashboard bundle loads
const DashboardLoader = () => (
  <div className="flex-1 flex flex-col items-center justify-center h-full text-emerald-400 gap-4">
    <Loader2 size={40} className="animate-spin" />
    <span className="font-mono text-sm tracking-widest uppercase">
      Initializing Geospatial Engine...
    </span>
  </div>
);

/**
 * Root Application component configuring client-side routes and lazy loading.
 */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<LandingPage />} />
          <Route path="about" element={<AboutPage />} />
          <Route path="app" element={<AppPromoPage />} />
          <Route
            path="dashboard"
            element={
              <Suspense fallback={<DashboardLoader />}>
                <Dashboard />
              </Suspense>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
