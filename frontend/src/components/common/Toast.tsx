import * as React from "react";
import { CheckCircle2, AlertCircle, Info, X } from "lucide-react";
import { cn } from "../../utils/cn";

export interface ToastItem {
  id: number;
  kind: "success" | "error" | "info";
  message: string;
}

interface ToastContextValue {
  toasts: ToastItem[];
  push: (kind: ToastItem["kind"], message: string) => void;
  dismiss: (id: number) => void;
}

const ToastContext = React.createContext<ToastContextValue | null>(null);

let nextToastId = 1;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = React.useState<ToastItem[]>([]);

  const dismiss = React.useCallback((id: number) => {
    setToasts((current) => current.filter((t) => t.id !== id));
  }, []);

  const push = React.useCallback(
    (kind: ToastItem["kind"], message: string) => {
      const id = nextToastId++;
      setToasts((current) => [...current, { id, kind, message }]);
      window.setTimeout(() => {
        setToasts((current) => current.filter((t) => t.id !== id));
      }, 5000);
    },
    [],
  );

  const value = React.useMemo(() => ({ toasts, push, dismiss }), [toasts, push, dismiss]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed bottom-4 right-4 z-[100] flex w-full max-w-sm flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={cn(
              "pointer-events-auto flex items-start gap-3 rounded-lg border bg-white p-3 shadow-lg",
              toast.kind === "success" && "border-emerald-200",
              toast.kind === "error" && "border-red-200",
              toast.kind === "info" && "border-slate-200",
            )}
            role="status"
          >
            {toast.kind === "success" && <CheckCircle2 className="mt-0.5 h-4 w-4 text-emerald-600" />}
            {toast.kind === "error" && <AlertCircle className="mt-0.5 h-4 w-4 text-red-600" />}
            {toast.kind === "info" && <Info className="mt-0.5 h-4 w-4 text-navy-600" />}
            <p className="flex-1 text-sm text-slate-700">{toast.message}</p>
            <button
              type="button"
              className="rounded p-1 text-slate-400 hover:text-slate-600"
              onClick={() => dismiss(toast.id)}
              aria-label="Dismiss notification"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const context = React.useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within ToastProvider");
  }
  return context;
}
