import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// Vite build configuration with React and Tailwind CSS v4 plugins
export default defineConfig({
  plugins: [tailwindcss(), react()],
});
