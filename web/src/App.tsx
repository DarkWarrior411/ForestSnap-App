import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Layout } from "./components/Layout";
import { Dashboard } from "./pages/Dashboard";
import { LandingPage } from "./pages/LandingPage";
import { AboutPage } from "./pages/AboutPage";
import { AppPromoPage } from "./pages/AppPromoPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<LandingPage />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="about" element={<AboutPage />} />
          <Route path="app" element={<AppPromoPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
