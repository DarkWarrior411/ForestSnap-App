import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { 
  Smartphone, 
  Download, 
  WifiOff, 
  Cpu, 
  QrCode, 
  Scan,
  Trees 
} from "lucide-react";

export function AppPromoPage() {
  const features = [
    {
      icon: Cpu,
      title: "Edge AI Validation",
      desc: "Instantly drop blurry captures using lightweight on-device models to save storage.",
    },
    {
      icon: WifiOff,
      title: "Store & Forward Sync",
      desc: "Queue high-quality telemetry offline and auto-sync when connection is restored.",
    },
  ];

  return (
    <div className="flex-1 overflow-y-auto custom-scrollbar bg-background text-text-main transition-colors duration-300 flex flex-col">
      
      {/* --- HERO / PROMO SECTION --- */}
      <section className="flex-1 px-6 py-12 md:py-24 flex flex-col lg:flex-row items-center justify-center gap-16 max-w-7xl mx-auto w-full">
        
        {/* Left Column: Text & CTAs */}
        <motion.div
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6 }}
          className="flex-1 space-y-8 w-full"
        >
          <div className="inline-flex items-center space-x-2 bg-primary/10 border border-primary/20 text-primary px-4 py-1.5 rounded-full text-sm font-medium shadow-sm shadow-primary/5">
            <Smartphone size={16} />
            <span>Android App Available</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl font-black text-text-main leading-tight tracking-tight">
            Take the <span className="text-primary">Forest</span> <br className="hidden lg:block" /> with You
          </h1>
          
          <p className="text-xl text-text-muted leading-relaxed max-w-xl font-medium">
            The ForestSnap mobile app puts the power of AI right in your pocket.
            Designed specifically for field workers to map, capture, and analyze nature seamlessly—even off the grid.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-4">
            {features.map((f, i) => (
              <div
                key={i}
                className="bg-surface/50 p-6 rounded-2xl border border-border-main hover:border-primary/30 transition-colors"
              >
                <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center mb-4 text-primary">
                  <f.icon size={24} />
                </div>
                <h3 className="text-lg font-bold mb-2 text-text-main">
                  {f.title}
                </h3>
                <p className="text-text-muted text-sm leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>

          <div className="flex flex-col sm:flex-row items-center gap-6 pt-8 border-t border-border-main">
            {/* Primary Download Button */}
            <a
              href="/ForestSnap.apk"
              download
              className="w-full sm:w-auto inline-flex items-center justify-center space-x-2 bg-primary hover:bg-emerald-500 text-white font-bold px-8 py-4 rounded-xl transition-all shadow-[0_0_20px_rgba(16,185,129,0.2)] hover:scale-105 hover:shadow-[0_0_30px_rgba(16,185,129,0.4)]"
            >
              <Download size={20} />
              <span>Download APK</span>
            </a>

            {/* Desktop-to-Mobile QR Bridge (Hidden on tiny mobile screens) */}
            <div className="hidden sm:flex items-center gap-4 px-5 py-3 bg-surface border border-border-main rounded-xl shadow-sm">
              <div className="p-2 bg-white rounded-lg">
                <QrCode size={32} className="text-zinc-900" />
              </div>
              <div className="flex flex-col">
                <span className="text-sm font-bold text-text-main leading-none mb-1">Scan to install</span>
                <span className="text-xs text-text-muted font-medium">Android 10.0+</span>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Right Column: Dynamic Phone Mockup */}
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="flex-1 flex justify-center w-full lg:justify-end"
        >
          {/* Phone Hardware Container */}
          <div className="w-[300px] h-[600px] bg-zinc-950 rounded-[3rem] border-[10px] border-zinc-900 shadow-2xl overflow-hidden relative flex flex-col ring-1 ring-border-main shadow-primary/10">
            {/* Phone Notch */}
            <div className="absolute top-0 inset-x-0 h-6 bg-zinc-900 rounded-b-2xl w-36 mx-auto z-30" />
            
            {/* App UI: Header bar */}
            <div className="pt-10 pb-4 px-6 bg-zinc-950/80 backdrop-blur-md z-20 flex justify-between items-center text-white border-b border-white/10">
              <span className="font-bold text-sm flex items-center gap-2">
                <Trees size={16} className="text-primary" />
                ForestSnap
              </span>
              <div className="flex items-center gap-1.5 text-xs font-medium text-red-400 bg-red-400/10 px-2 py-1 rounded-md">
                <WifiOff size={12} />
                Offline
              </div>
            </div>

            {/* App UI: Camera Viewfinder */}
            <div className="flex-1 relative bg-zinc-900 overflow-hidden flex flex-col justify-between">
              {/* Fake camera feed background (using an Unsplash forest image via standard HTML) */}
              <div 
                className="absolute inset-0 opacity-60 bg-cover bg-center" 
                style={{ backgroundImage: "url('https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=800&auto=format&fit=crop')" }}
              />
              
              {/* Viewfinder Target */}
              <div className="absolute inset-10 border border-primary/40 rounded-2xl flex items-center justify-center bg-primary/5">
                <Scan size={64} className="text-primary/70 animate-pulse" />
              </div>

              {/* Edge AI Active Toast */}
              <div className="absolute bottom-6 inset-x-6 bg-zinc-950/90 backdrop-blur-md rounded-xl p-3 border border-white/10 flex items-center gap-3 text-white shadow-lg">
                <div className="p-1.5 bg-primary/20 rounded-md">
                  <Cpu size={16} className="text-primary" />
                </div>
                <div className="flex flex-col">
                  <span className="text-xs font-bold">Edge Model Active</span>
                  <span className="text-[10px] text-zinc-400">Validating image clarity...</span>
                </div>
              </div>
            </div>

            {/* App UI: Bottom Navigation / Shutter */}
            <div className="h-28 bg-zinc-950 flex items-center justify-center gap-10 pb-4 z-20">
              {/* Gallery icon fake */}
              <div className="w-10 h-10 rounded-lg border border-white/20 bg-white/5" />
              
              {/* Shutter Button */}
              <div className="w-16 h-16 rounded-full bg-white/10 border-[3px] border-zinc-400 flex items-center justify-center cursor-pointer hover:bg-white/20 transition-colors">
                <div className="w-12 h-12 rounded-full bg-white shadow-inner" />
              </div>

              {/* Sync queue icon fake */}
              <div className="w-10 h-10 rounded-full bg-surface border border-white/10 flex items-center justify-center relative">
                <span className="absolute -top-1 -right-1 w-4 h-4 bg-primary text-[8px] font-bold text-white rounded-full flex items-center justify-center">3</span>
              </div>
            </div>
          </div>
        </motion.div>
      </section>

      {/* --- FOOTER SECTION --- */}
      <footer className="bg-surface/50 border-t border-border-main px-6 py-12 mt-auto">
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