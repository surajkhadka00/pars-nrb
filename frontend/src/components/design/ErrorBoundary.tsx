import { Component, type ErrorInfo, type ReactNode } from "react";
import { AlertCircle, RefreshCw } from "lucide-react";
import { Button } from "../common/ui";

interface State { error: Error | null }

export class ErrorBoundary extends Component<{ children: ReactNode }, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("PARS UI error", error, info.componentStack);
  }

  render() {
    if (!this.state.error) {
      return this.props.children;
    }
    return (
      <div className="flex min-h-screen items-center justify-center p-6">
        <div className="pars-card max-w-md p-6 text-center">
          <AlertCircle className="mx-auto h-8 w-8 text-red-500" aria-hidden="true" />
          <h1 className="mt-3 text-lg font-semibold">The page could not be displayed</h1>
          <p className="mt-2 text-sm text-slate-500">An unexpected interface error occurred. Your session remains active.</p>
          <Button className="mt-4" onClick={() => { this.setState({ error: null }); window.location.reload(); }}>
            <RefreshCw className="h-4 w-4" /> Reload page
          </Button>
        </div>
      </div>
    );
  }
}
