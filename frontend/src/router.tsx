import { Navigate, createBrowserRouter } from 'react-router-dom';
import { RequireAdmin, RequireAuth } from './auth/RouteGuards';
import { MainLayout } from './layouts/MainLayout';
import { SectionLayout } from './layouts/SectionLayout';
import { AdminAuditLogsPage } from './pages/admin/AdminAuditLogsPage';
import { AdminSubscriptionsPage } from './pages/admin/AdminSubscriptionsPage';
import { AdminUserDetailPage } from './pages/admin/AdminUserDetailPage';
import { AdminUsersPage } from './pages/admin/AdminUsersPage';
import { AccountSettingsPage } from './pages/app/AccountSettingsPage';
import { DashboardPage } from './pages/app/DashboardPage';
import { NewSubscriptionPage } from './pages/app/NewSubscriptionPage';
import { SubscriptionDetailPage } from './pages/app/SubscriptionDetailPage';
import { SubscriptionsPage } from './pages/app/SubscriptionsPage';
import { ChzzkCallbackPage } from './pages/auth/ChzzkCallbackPage';
import { ChzzkLoginPage } from './pages/auth/ChzzkLoginPage';
import { LandingPage } from './pages/LandingPage';
import { NotFoundPage } from './pages/NotFoundPage';

const appNavItems = [
  { to: '/app/dashboard', label: 'Dashboard' },
  { to: '/app/subscriptions', label: 'Subscriptions' },
  { to: '/app/subscriptions/new', label: 'New Subscription' },
  { to: '/app/settings/account', label: 'Account Settings' },
];

const adminNavItems = [
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/subscriptions', label: 'Subscriptions' },
  { to: '/admin/audit-logs', label: 'Audit Logs' },
];

export const routes = [
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'auth/chzzk/login', element: <ChzzkLoginPage /> },
      { path: 'auth/chzzk/callback', element: <ChzzkCallbackPage /> },
      {
        element: <RequireAuth />,
        children: [
          {
            path: 'app',
            element: <SectionLayout title="Application" navItems={appNavItems} />,
            children: [
              { index: true, element: <Navigate to="dashboard" replace /> },
              { path: 'dashboard', element: <DashboardPage /> },
              { path: 'subscriptions', element: <SubscriptionsPage /> },
              { path: 'subscriptions/new', element: <NewSubscriptionPage /> },
              { path: 'subscriptions/:id', element: <SubscriptionDetailPage /> },
              { path: 'settings/account', element: <AccountSettingsPage /> },
            ],
          },
        ],
      },
      {
        element: <RequireAdmin />,
        children: [
          {
            path: 'admin',
            element: <SectionLayout title="Administration" navItems={adminNavItems} />,
            children: [
              { index: true, element: <Navigate to="users" replace /> },
              { path: 'users', element: <AdminUsersPage /> },
              { path: 'users/:id', element: <AdminUserDetailPage /> },
              { path: 'subscriptions', element: <AdminSubscriptionsPage /> },
              { path: 'audit-logs', element: <AdminAuditLogsPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

export const router = createBrowserRouter(routes);
