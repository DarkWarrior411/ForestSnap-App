import { motion } from "framer-motion";
import { Smartphone, Download, WifiOff, Cpu } from "lucide-react";

export function AppPromoPage() {
  const features = [
    {
      icon: WifiOff,
      title: "Offline Sync",
      desc: "Queue analysis offline and sync when connected.",
    },
    {
      icon: Cpu,
      title: "On-Device ML",
      desc: "Fast ONNX models for tree species & health.",
    },
  ];

  return (
    <div className="flex-1 overflow-y-auto p-6 md:p-12 flex flex-col lg:flex-row items-center gap-12 max-w-6xl mx-auto bg-background transition-colors duration-300">
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        className="flex-1 space-y-8"
      >
        <div className="inline-flex items-center space-x-2 bg-primary/20 text-primary px-4 py-1.5 rounded-full text-sm font-medium">
          <Smartphone size={16} />
          <span>Android App Available</span>
        </div>
        <h1 className="text-4xl md:text-6xl font-bold text-text-main">
          Take the Forest with You
        </h1>
        <p className="text-xl text-text-muted">
          The ForestSnap mobile app puts the power of AI right in your pocket.
          Designed for field workers to map and analyze nature seamlessly.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {features.map((f, i) => (
            <div
              key={i}
              className="bg-surface/30 p-6 rounded-2xl border border-border-main"
            >
              <f.icon className="text-primary mb-4" size={28} />
              <h3 className="text-lg font-bold mb-2 text-text-main">
                {f.title}
              </h3>
              <p className="text-text-muted text-sm">{f.desc}</p>
            </div>
          ))}
        </div>

        <a
          href="/ForestSnap.apk"
          download
          className="inline-flex items-center space-x-2 bg-primary hover:bg-emerald-400 text-white font-bold px-8 py-4 rounded-xl transition-all shadow-[0_0_20px_rgba(16,185,129,0.2)]"
        >
          <Download size={20} />
          <span>Download APK</span>
        </a>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, x: 30 }}
        animate={{ opacity: 1, x: 0 }}
        className="flex-1 flex justify-center"
      >
        <div className="w-64 h-[500px] bg-surface rounded-[3rem] border-8 border-background shadow-2xl overflow-hidden relative flex flex-col shadow-primary/20">
          <div className="absolute top-0 inset-x-0 h-6 bg-background rounded-b-xl w-32 mx-auto z-10" />
          <div className="flex-1 bg-[url('/assets/hero.png')] bg-cover bg-center opacity-30" />
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-primary flex flex-col items-center">
              <Smartphone size={48} className="mb-4" />
              <p className="font-bold text-text-main">ForestSnap App</p>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
