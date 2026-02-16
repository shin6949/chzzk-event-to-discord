import { Navigate, createBrowserRouter } from 'react-router-dom';
import { RequireAuth } from './auth/RouteGuards';
import { MainLayout } from './layouts/MainLayout';
import { SectionLayout } from './layouts/SectionLayout';
import { NewSubscriptionPage } from './pages/app/NewSubscriptionPage';
import { SubscriptionDetailPage } from './pages/app/SubscriptionDetailPage';
import { SubscriptionsPage } from './pages/app/SubscriptionsPage';
import { ChzzkLoginPage } from './pages/auth/ChzzkLoginPage';
import { LandingPage } from './pages/LandingPage';
import { NotFoundPage } from './pages/NotFoundPage';

const subscriptionNavItems = [
  { to: '/subscriptions', label: 'Subscriptions' },
  { to: '/subscriptions/new', label: 'New subscription' },
];

export const routes = [
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'login', element: <ChzzkLoginPage /> },
      { path: 'auth/chzzk/login', element: <Navigate to="/login" replace /> },
      {
        element: <RequireAuth />,
        children: [
          {
            path: 'subscriptions',
            element: <SectionLayout title="Subscriptions" navItems={subscriptionNavItems} />,
            children: [
              { index: true, element: <SubscriptionsPage /> },
              { path: 'new', element: <NewSubscriptionPage /> },
              { path: ':id', element: <SubscriptionDetailPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

export const router = createBrowserRouter(routes);
