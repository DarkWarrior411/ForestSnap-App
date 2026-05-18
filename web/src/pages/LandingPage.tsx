import { Link } from "react-router-dom";
import { 
  ArrowRight, 
  Activity, 
  Globe, 
  Cpu, 
  Smartphone, 
  Map, 
  Trees 
} from "lucide-react";
import { motion } from "framer-motion";

export function LandingPage() {
  // --- REVERTED TO ORIGINAL FEATURES ---
  const features = [
    {
      icon: Cpu,
      title: "Edge AI Processing",
      desc: "Run real-time segmentation and classification directly on field devices, offline.",
    },
    {
      icon: Globe,
      title: "Geospatial Dashboard",
      desc: "Monitor predictive fire spreads and ecosystem health from a centralized command center.",
    },
    {
      icon: Activity,
      title: "Live Streaming",
      desc: "Receive instantaneous alerts and telemetry data via Server-Sent Events (SSE).",
    },
  ];

  // --- KEPT THE UPDATED PIPELINE TO REFLECT YOUR ARCHITECTURE ---
  const pipelineSteps = [
    {
      icon: Smartphone,
      title: "1. Capture & Validate",
      desc: "Rangers capture ecological photos. On-device AI instantly drops blurry images, temporarily storing only high-quality data.",
    },
    {
      icon: Activity,
      title: "2. Auto-Sync",
      desc: "Upon detecting a connection, the app forwards the queued images to the cloud server and deletes local files to free up space.",
    },
    {
      icon: Map,
      title: "3. Cloud Analysis",
      desc: "Heavy cloud models calculate risk factors from the synced data, mapping predictive ecosystem health directly to the Command Center.",
    }
  ];

  return (
    <div className="flex-1 overflow-y-auto custom-scrollbar bg-background text-text-main transition-colors duration-300">
      
      {/* --- HERO SECTION --- */}
      <section className="relative px-6 pt-24 pb-24 md:pt-32 md:pb-40 flex flex-col items-center text-center overflow-hidden">
        {/* Subtle grid background pattern */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] pointer-events-none" />
        
        {/* Glowing orb effect */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-3xl h-[400px] bg-primary/20 blur-[120px] rounded-full pointer-events-none opacity-50 dark:opacity-100" />

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
          className="relative z-10 max-w-5xl mx-auto"
        >
          <div className="inline-flex items-center space-x-2 bg-primary/10 border border-primary/30 text-primary px-5 py-2 rounded-full text-sm font-bold mb-10 shadow-lg shadow-primary/10">
            <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            <span>ForestSnap v2.0 Enterprise is Live</span>
          </div>

          <h1 className="text-5xl md:text-8xl font-black tracking-tighter mb-8 leading-tight">
            Protect ecosystems with <br className="hidden md:block" />
            <span className="bg-gradient-to-r from-primary to-cyan-500 bg-clip-text text-transparent">
              predictive intelligence.
            </span>
          </h1>

          <p className="text-xl md:text-2xl text-text-muted mb-12 max-w-3xl mx-auto font-medium">
            The complete geospatial pipeline for modern conservation. Deploy
            on-device AI to track tree health, calculate fuel loads, and
            simulate wildfire spreads in real-time.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
            <Link
              to="/dashboard"
              className="px-8 py-4 bg-primary text-white font-bold rounded-xl transition-all hover:scale-105 shadow-[0_0_30px_rgba(16,185,129,0.3)] flex items-center space-x-2 w-full sm:w-auto justify-center"
            >
              <span>Launch Command Center</span>
              <ArrowRight size={18} />
            </Link>
            <Link
              to="/app"
              className="px-8 py-4 bg-surface text-text-main font-bold rounded-xl border border-border-main transition-all hover:bg-border-main w-full sm:w-auto justify-center flex"
            >
              Get Mobile App
            </Link>
          </div>
        </motion.div>
      </section>

      {/* --- FEATURES SECTION (Engineered for the field) --- */}
      <section className="px-6 py-24 bg-surface/30 border-t border-border-main">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-3xl md:text-5xl font-black mb-6">
              Engineered for the field.
            </h2>
            <p className="text-text-muted text-lg max-w-2xl mx-auto">
              No internet? No problem. ForestSnap is built offline-first,
              syncing your critical environmental telemetry the moment you
              reconnect.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {features.map((feature, idx) => {
              const Icon = feature.icon;
              return (
                <motion.div
                  key={idx}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: idx * 0.15, duration: 0.5 }}
                  className="bg-surface border border-border-main p-10 rounded-3xl hover:border-primary/50 transition-colors group shadow-xl shadow-black/5"
                >
                  <div className="w-14 h-14 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-8 group-hover:scale-110 transition-transform">
                    <Icon size={28} />
                  </div>
                  <h3 className="text-2xl font-bold mb-4">{feature.title}</h3>
                  <p className="text-text-muted leading-relaxed font-medium">
                    {feature.desc}
                  </p>
                </motion.div>
              );
            })}
          </div>
        </div>
      </section>

      {/* --- HOW IT WORKS (PIPELINE) SECTION --- */}
      <section className="px-6 py-24 border-t border-border-main relative overflow-hidden">
        <div className="max-w-7xl mx-auto relative z-10">
          <div className="mb-16 text-center md:text-left">
            <h2 className="text-3xl md:text-5xl font-black mb-4">
              The Conservation Pipeline
            </h2>
            <p className="text-text-muted text-lg max-w-2xl">
              From boots on the ground to high-level strategic overviews, ForestSnap connects the dots.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-12 relative">
            {/* Connecting line for desktop */}
            <div className="hidden md:block absolute top-12 left-[15%] right-[15%] h-[2px] bg-gradient-to-r from-primary/10 via-primary/50 to-primary/10" />

            {pipelineSteps.map((step, idx) => {
              const Icon = step.icon;
              return (
                <motion.div
                  key={idx}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  whileHover={{ scale: 1.05 }}
                  viewport={{ once: true }}
                  transition={{ delay: idx * 0.2, duration: 0.3 }}
                  className="relative z-10 flex flex-col items-center text-center cursor-pointer group"
                >
                  <div className="w-24 h-24 bg-background border-4 border-surface rounded-full flex items-center justify-center text-primary mb-6 shadow-xl z-10 transition-all duration-300 group-hover:bg-primary/10 group-hover:border-primary/50 group-hover:shadow-primary/20">
                    <Icon size={40} className="transition-transform duration-300 group-hover:scale-110" />
                  </div>
                  <h3 className="text-2xl font-bold mb-4">{step.title}</h3>
                  <p className="text-text-muted leading-relaxed">
                    {step.desc}
                  </p>
                </motion.div>
              );
            })}
          </div>
        </div>
      </section>

      {/* --- FOOTER SECTION --- */}
      <footer className="bg-surface/50 border-t border-border-main px-6 py-12 mt-10">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center space-x-3 text-text-main">
            <div className="p-2 bg-primary/10 rounded-xl text-primary">
              <Trees size={24} />
            </div>
            <span className="text-xl font-bold tracking-tight">ForestSnap</span>
          </div>
          
          <div className="flex space-x-6 text-sm font-medium text-text-muted">
            <Link to="/about" className="hover:text-primary transition-colors">About Us</Link>
            <Link to="/app" className="hover:text-primary transition-colors">Download App</Link>
            <a href="#" className="hover:text-primary transition-colors">Documentation</a>
            <a href="#" className="hover:text-primary transition-colors">Privacy Policy</a>
          </div>

          <div className="text-text-muted text-sm">
            &copy; {new Date().getFullYear()} ForestSnap. All rights reserved.
          </div>
        </div>
      </footer>

    </div>
  );
}