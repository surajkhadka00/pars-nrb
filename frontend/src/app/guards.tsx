import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { Alert, Button } from "../components/common/ui";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { status, isAuthenticated } = useAuth();
  const location = useLocation();

  if (status === "loading") {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-slate-300 border-t-navy-700" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <>{children}</>;
}

export function RequireGuest({ children }: { children: React.ReactNode }) {
  const { status, isAuthenticated } = useAuth();

  if (status === "loading") {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-slate-300 border-t-navy-700" />
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

export function AccessDenied({ permission }: { permission?: string }) {
  return (
    <div className="mx-auto max-w-xl">
      <Alert variant="destructive" className="mb-4">
        <p className="font-medium">Access denied</p>
        <p className="mt-1">Your account does not have the permission required to view this page.</p>
        {permission ? <p className="mt-1 font-mono text-xs">Required permission: {permission}</p> : null}
      </Alert>
      <div className="flex justify-end">
        <Button onClick={() => window.history.back()}>Go back</Button>
      </div>
    </div>
  );
}
