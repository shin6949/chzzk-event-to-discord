import { Navigate, createBrowserRouter } from 'react-router-dom';
import { RequireAuth } from './auth/RouteGuards';
import { MainLayout } from './layouts/MainLayout';
import { SectionLayout } from './layouts/SectionLayout';
import { BotProfilesPage } from './pages/app/BotProfilesPage';
import { NewSubscriptionPage } from './pages/app/NewSubscriptionPage';
import { SubscriptionDetailPage } from './pages/app/SubscriptionDetailPage';
import { TwitchSubscriptionsPage } from './pages/app/TwitchSubscriptionsPage';
import { SubscriptionsPage } from './pages/app/SubscriptionsPage';
import { WebhooksPage } from './pages/app/WebhooksPage';
import { YouTubeSubscriptionsPage } from './pages/app/YouTubeSubscriptionsPage';
import { ChzzkLoginPage } from './pages/auth/ChzzkLoginPage';
import { LandingPage } from './pages/LandingPage';
import { NotFoundPage } from './pages/NotFoundPage';

const subscriptionNavItems = [
  { to: '/subscriptions', label: 'Subscriptions' },
  { to: '/subscriptions/new', label: 'New subscription' },
  { to: '/subscriptions/twitch', label: 'Twitch EventSub' },
  { to: '/subscriptions/youtube', label: 'YouTube' },
];

const discordNavItems = [
  { to: '/discord/webhooks', label: 'Webhooks' },
  { to: '/discord/bot-profiles', label: 'Bot Profiles' },
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
              { path: 'twitch', element: <TwitchSubscriptionsPage /> },
              { path: 'youtube', element: <YouTubeSubscriptionsPage /> },
              { path: ':id', element: <SubscriptionDetailPage /> },
            ],
          },
          {
            path: 'discord',
            element: <SectionLayout title="Discord Resources" navItems={discordNavItems} />,
            children: [
              { index: true, element: <Navigate to="/discord/webhooks" replace /> },
              { path: 'webhooks', element: <WebhooksPage /> },
              { path: 'bot-profiles', element: <BotProfilesPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
];

export const router = createBrowserRouter(routes);
