import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getSession } from './session';

export function RequireAuth() {
  const session = getSession();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export function RequireAdmin() {
  const session = getSession();

  if (!session) {
    return <Navigate to="/" replace />;
  }

  if (session.role !== 'ADMIN') {
    return <Navigate to="/app/dashboard" replace />;
  }

  return <Outlet />;
}
