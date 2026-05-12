import { Outlet, Link, useLocation } from "react-router-dom";
import { Trees, Info, LayoutDashboard, Smartphone } from "lucide-react";

export function Layout() {
  const location = useLocation();

  const navLinks = [
    { path: "/", label: "Home", icon: Trees },
    { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { path: "/about", label: "About", icon: Info },
    { path: "/app", label: "Mobile App", icon: Smartphone },
  ];

  return (
    <div className="h-screen bg-[#022c22] text-[#ecfdf5] font-sans flex flex-col">
      {/* Navigation Header */}
      <header className="px-6 py-4 border-b border-[#064e3b] bg-[#022c22]/80 backdrop-blur-md sticky top-0 z-50 flex justify-between items-center">
        <Link to="/" className="flex items-center space-x-3 group">
          <div className="p-2 bg-emerald-500/20 rounded-xl text-emerald-400 group-hover:bg-emerald-500/30 transition-colors">
            <Trees size={24} />
          </div>
          <h1 className="text-xl font-bold tracking-tight bg-gradient-to-r from-emerald-400 to-lime-400 bg-clip-text text-transparent">
            ForestSnap
          </h1>
        </Link>
        <nav className="hidden md:flex space-x-1">
          {navLinks.map((link) => {
            const Icon = link.icon;
            const isActive = location.pathname === link.path;
            return (
              <Link
                key={link.path}
                to={link.path}
                className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-[#064e3b] text-emerald-300"
                    : "hover:bg-[#064e3b]/50 text-emerald-100 hover:text-emerald-300"
                }`}
              >
                <Icon size={16} />
                <span>{link.label}</span>
              </Link>
            );
          })}
        </nav>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col relative min-h-0 overflow-y-auto overflow-x-hidden">
        <Outlet />
      </main>
    </div>
  );
}
