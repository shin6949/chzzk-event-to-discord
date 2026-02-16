import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function RequireAuth() {
  const { session, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="text-center py-5">Checking session...</div>;
  }

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export function RequireAdmin() {
  const { session, loading } = useAuth();

  if (loading) {
    return <div className="text-center py-5">Checking session...</div>;
  }

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (session.role !== 'ADMIN') {
    return <Navigate to="/subscriptions" replace />;
  }

  return <Outlet />;
}
