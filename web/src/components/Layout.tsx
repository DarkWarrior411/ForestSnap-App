import { useState, useEffect } from "react";
import { Outlet, Link, useLocation } from "react-router-dom";
import {
  Trees,
  Info,
  LayoutDashboard,
  Smartphone,
  Sun,
  Moon,
} from "lucide-react";

export function Layout() {
  const location = useLocation();
  const [isDark, setIsDark] = useState(true);

  useEffect(() => {
    if (isDark) document.documentElement.classList.add("dark");
    else document.documentElement.classList.remove("dark");
  }, [isDark]);

  const navLinks = [
    { path: "/", label: "Home", icon: Trees },
    { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { path: "/about", label: "About", icon: Info },
    { path: "/app", label: "Mobile App", icon: Smartphone },
  ];

  return (
    <div className="h-screen bg-background text-text-main font-sans flex flex-col overflow-hidden transition-colors duration-300">
      <header className="px-4 md:px-6 py-3 border-b border-border-main bg-background/80 backdrop-blur-xl z-50 flex justify-between items-center shrink-0 shadow-sm">
        <Link to="/" className="flex items-center space-x-3 group">
          <div className="p-2 bg-primary/10 rounded-xl text-primary group-hover:bg-primary/20 transition-colors border border-primary/20">
            <Trees size={22} />
          </div>
          <h1 className="text-xl font-bold tracking-tight text-text-main">
            ForestSnap
          </h1>
        </Link>

        <div className="flex items-center gap-4">
          <nav className="hidden md:flex space-x-2">
            {navLinks.map((link) => {
              const Icon = link.icon;
              const isActive = location.pathname === link.path;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                    isActive
                      ? "bg-primary/10 text-primary border border-primary/20"
                      : "text-text-muted hover:bg-surface/50 hover:text-text-main border border-transparent"
                  }`}
                >
                  <Icon size={16} />
                  <span>{link.label}</span>
                </Link>
              );
            })}
          </nav>

          {}
          <button
            onClick={() => setIsDark(!isDark)}
            className="p-2 rounded-lg border border-border-main text-text-muted hover:text-text-main hover:bg-surface transition-colors"
            aria-label="Toggle Theme"
          >
            {isDark ? <Sun size={18} /> : <Moon size={18} />}
          </button>
        </div>
      </header>

      <main className="flex-1 flex flex-col relative min-h-0 overflow-y-auto custom-scrollbar pb-16 md:pb-0">
        <Outlet />
      </main>

      {}
    </div>
  );
}
