import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { 
  AlertTriangle, 
  ShieldCheck, 
  Target, 
  Users, 
  Globe, 
  Mail, 
  Trees 
} from "lucide-react";

/**
 * About page detailing project mission, problem statement, core architecture, and team members.
 */
export function AboutPage() {
  const team = [
    {
      name: "Ishan Gupta",
      image: "https://api.dicebear.com/7.x/avataaars/svg?seed=Ishan",
    },
    {
      name: "Kr. Aadarsh Suman",
      image: "https://api.dicebear.com/7.x/avataaars/svg?seed=Aadarsh",
    },
    {
      name: "Arya N",
      image: "https://api.dicebear.com/7.x/avataaars/svg?seed=Arya",
    },
    {
      name: "Kotagi Shashank",
      image: "https://api.dicebear.com/7.x/avataaars/svg?seed=Shashank",
    },
  ];

  return (
    <div className="flex-1 overflow-y-auto custom-scrollbar bg-background text-text-main transition-colors duration-300">
      
      {/* Mission overview header */}
      <section className="relative px-6 pt-24 pb-20 md:pt-32 md:pb-24 overflow-hidden flex flex-col items-center text-center">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-2xl h-[300px] bg-primary/10 blur-[100px] rounded-full pointer-events-none" />
        
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="relative z-10 max-w-3xl mx-auto"
        >
          <div className="inline-flex items-center space-x-2 bg-surface border border-border-main text-text-muted px-4 py-1.5 rounded-full text-sm font-medium mb-8">
            <Target size={16} className="text-primary" />
            <span>Our Mission</span>
          </div>
          
          <h1 className="text-4xl md:text-6xl font-black mb-8 tracking-tight">
            Bridging the gap between <span className="text-primary">AI</span> and conservation.
          </h1>
          
          <p className="text-xl md:text-2xl text-text-muted leading-relaxed font-medium">
            We believe that protecting our forests requires precise data, accessible tools, and real-time insights in the hands of those on the front lines.
          </p>
        </motion.div>
      </section>

      {/* Problem and solution breakdown cards */}
      <section className="px-6 py-16 max-w-6xl mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 lg:gap-20">
          
          {/* Challenge statement card */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
            className="bg-surface/30 border border-border-main p-10 md:p-12 rounded-3xl relative overflow-hidden group hover:-translate-y-2 hover:border-red-500/30 hover:shadow-2xl transition-all duration-300"
          >
            <div className="absolute top-0 right-0 w-32 h-32 bg-red-500/5 blur-[50px] rounded-full group-hover:bg-red-500/10 transition-colors duration-500" />
            <div className="w-14 h-14 bg-red-500/10 text-red-500 rounded-2xl flex items-center justify-center mb-8">
              <AlertTriangle size={28} />
            </div>
            <h3 className="text-3xl font-bold mb-6">The Problem</h3>
            <p className="text-text-muted text-lg leading-relaxed">
              Traditional forest monitoring is manual, time-consuming, and often lacks the immediate data required to stop diseases or track rapid ecological changes. Rangers are tasked with analyzing vast territories without the digital infrastructure needed to make split-second, informed decisions.
            </p>
          </motion.div>

          {/* Technical solution card */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="bg-surface/30 border border-primary/20 p-10 md:p-12 rounded-3xl relative overflow-hidden group shadow-[0_0_30px_rgba(16,185,129,0.05)] hover:-translate-y-2 hover:border-primary/50 hover:shadow-[0_0_50px_rgba(16,185,129,0.15)] transition-all duration-300"
          >
            <div className="absolute top-0 right-0 w-32 h-32 bg-primary/10 blur-[50px] rounded-full group-hover:bg-primary/20 transition-colors duration-500" />
            <div className="w-14 h-14 bg-primary/10 text-primary rounded-2xl flex items-center justify-center mb-8">
              <ShieldCheck size={28} />
            </div>
            <h3 className="text-3xl font-bold mb-6">Our Solution</h3>
            <p className="text-text-muted text-lg leading-relaxed">
              By combining on-device AI for instant validation with robust, offline-first synchronization to powerful cloud analytics, ForestSnap empowers rangers to capture critical ecological data directly from their smartphones, mapping ecosystem health and fire risks in real-time.
            </p>
          </motion.div>

        </div>
      </section>

      {/* Team member grid */}
      <section className="px-6 py-24 border-t border-border-main mt-12 bg-surface/10">
        <div className="max-w-7xl mx-auto text-center">
          <div className="inline-flex items-center space-x-2 text-text-muted mb-4">
            <Users size={20} />
            <span className="font-semibold uppercase tracking-wider text-sm">The Team</span>
          </div>
          <h2 className="text-3xl md:text-5xl font-black mb-16">Built by conservationists & engineers.</h2>
          
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {team.map((member, idx) => (
              <motion.div
                key={idx}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: idx * 0.1 }}
                className="bg-background border border-border-main p-8 rounded-3xl hover:border-primary/30 transition-colors group"
              >
                <div className="w-24 h-24 mx-auto bg-surface rounded-full mb-6 overflow-hidden border-2 border-border-main group-hover:border-primary transition-colors">
                  <img src={member.image} alt={member.name} className="w-full h-full object-cover" />
                </div>
                <h3 className="text-xl font-bold mb-6">{member.name}</h3>
                
                <div className="flex justify-center space-x-4 text-text-muted">
                  <a href="#" className="hover:text-text-main transition-colors"><Globe size={18} /></a>
                  <a href="#" className="hover:text-primary transition-colors"><Mail size={18} /></a>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Page footer */}
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