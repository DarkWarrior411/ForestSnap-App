import { motion } from "framer-motion";

export function AboutPage() {
  return (
    <div className="flex-1 overflow-y-auto p-6 md:p-12 bg-background transition-colors duration-300">
      <div className="max-w-4xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-surface/40 border border-border-main rounded-3xl p-8 md:p-12"
        >
          <h1 className="text-4xl md:text-5xl font-bold mb-6 text-text-main">
            Our Mission
          </h1>
          <p className="text-xl text-text-muted mb-8 leading-relaxed">
            ForestSnap was built to bridge the gap between advanced artificial
            intelligence and boots-on-the-ground conservation efforts. We
            believe that protecting our forests requires precise data,
            accessible tools, and real-time insights.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-12">
            <div className="space-y-4">
              <h3 className="text-2xl font-semibold text-primary">
                The Problem
              </h3>
              <p className="text-text-muted leading-relaxed">
                Traditional forest monitoring is manual, time-consuming, and
                often lacks the immediate data required to stop diseases or
                track rapid ecological changes. Rangers need better tools to
                analyze vast areas efficiently.
              </p>
            </div>
            <div className="space-y-4">
              <h3 className="text-2xl font-semibold text-primary">
                Our Solution
              </h3>
              <p className="text-text-muted leading-relaxed">
                By combining on-device ML models for species and health
                detection with robust offline-first synchronization, ForestSnap
                empowers rangers to capture critical ecological data directly
                from their smartphones.
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
