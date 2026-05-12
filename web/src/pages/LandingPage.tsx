import { Link } from "react-router-dom";
import { Leaf, ShieldCheck, Map, ArrowRight } from "lucide-react";
import { motion } from "framer-motion";

export function LandingPage() {
  const features = [
    {
      icon: Leaf,
      title: "Real-time Analysis",
      description: "Detect tree species and health instantly using advanced on-device ML models.",
    },
    {
      icon: Map,
      title: "Geospatial Tracking",
      description: "Map out entire forests with precise GPS coordinates and health indicators.",
    },
    {
      icon: ShieldCheck,
      title: "Offline Capable",
      description: "Work deep in the forest without internet. Sync securely when you return.",
    },
  ];

  return (
    <div className="flex-1 overflow-y-auto">
      {/* Hero Section */}
      <section className="relative px-6 py-20 md:py-32 flex flex-col items-center text-center">
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-40 -left-40 w-96 h-96 bg-emerald-500/20 rounded-full blur-3xl" />
          <div className="absolute top-20 -right-20 w-80 h-80 bg-lime-500/20 rounded-full blur-3xl" />
        </div>
        
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="relative z-10 max-w-4xl mx-auto"
        >
          <div className="inline-flex items-center space-x-2 bg-emerald-900/50 border border-emerald-500/30 text-emerald-300 px-4 py-1.5 rounded-full text-sm font-medium mb-8">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>ForestSnap v2.0 is live</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6">
            Empowering Forestry with <span className="bg-gradient-to-r from-emerald-400 to-lime-400 bg-clip-text text-transparent">AI & Vision</span>
          </h1>
          
          <p className="text-xl md:text-2xl text-emerald-100/80 mb-10 max-w-2xl mx-auto">
            Analyze tree health, detect species, and map ecosystems in real-time. The ultimate tool for modern conservationists and rangers.
          </p>
          
          <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-6">
            <Link
              to="/dashboard"
              className="px-8 py-4 bg-emerald-500 hover:bg-emerald-400 text-[#022c22] font-bold rounded-xl transition-all shadow-[0_0_20px_rgba(16,185,129,0.3)] hover:shadow-[0_0_30px_rgba(16,185,129,0.5)] flex items-center space-x-2 group w-full sm:w-auto justify-center"
            >
              <span>Open Dashboard</span>
              <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
            </Link>
            <Link
              to="/app"
              className="px-8 py-4 bg-[#064e3b] hover:bg-[#065f46] text-emerald-100 font-bold rounded-xl border border-emerald-800 transition-colors w-full sm:w-auto justify-center flex"
            >
              Get Mobile App
            </Link>
          </div>
        </motion.div>
      </section>

      {/* Features Grid */}
      <section className="px-6 py-20 bg-[#022c22]/50 border-t border-[#064e3b]">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-4">Built for the Field</h2>
            <p className="text-emerald-100/70 max-w-2xl mx-auto">Everything you need to monitor and protect forest ecosystems, right in your pocket and accessible from anywhere.</p>
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
                  transition={{ delay: idx * 0.2 }}
                  className="bg-[#064e3b]/50 border border-emerald-800/50 p-8 rounded-2xl hover:bg-[#064e3b] transition-colors"
                >
                  <div className="w-12 h-12 bg-emerald-500/20 rounded-xl flex items-center justify-center text-emerald-400 mb-6">
                    <Icon size={24} />
                  </div>
                  <h3 className="text-xl font-bold mb-3">{feature.title}</h3>
                  <p className="text-emerald-100/70 leading-relaxed">{feature.description}</p>
                </motion.div>
              );
            })}
          </div>
        </div>
      </section>
    </div>
  );
}
