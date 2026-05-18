import React, { Component, ErrorInfo, ReactNode } from "react";
import { MapPinOff, RefreshCw } from "lucide-react";

interface Props {
  children?: ReactNode;
  fallbackMessage?: string;
}

interface State {
  hasError: boolean;
  errorMsg: string;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    errorMsg: "",
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, errorMsg: error.message };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught rendering error:", error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center h-full w-full bg-[#022c22] border border-red-500/30 rounded-2xl p-6 text-center">
          <div className="p-4 bg-red-500/10 rounded-full text-red-400 mb-4">
            <MapPinOff size={32} />
          </div>
          <h2 className="text-xl font-bold text-red-100 mb-2">
            Visualization Failed
          </h2>
          <p className="text-sm text-red-200/60 mb-6 max-w-sm">
            {this.props.fallbackMessage ||
              "The geospatial engine encountered a critical error. This usually happens if WebGL is unavailable."}
          </p>
          <button
            onClick={() => this.setState({ hasError: false })}
            className="flex items-center gap-2 px-4 py-2 bg-[#064e3b] hover:bg-[#065f46] text-emerald-100 rounded-lg transition-colors"
          >
            <RefreshCw size={16} /> Try Reloading Component
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
