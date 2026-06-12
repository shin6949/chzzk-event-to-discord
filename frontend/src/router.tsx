import {Navigate, createBrowserRouter} from 'react-router-dom';
import {RequireAuth} from './auth/RouteGuards';
import {MainLayout} from './layouts/MainLayout';
import {SectionLayout} from './layouts/SectionLayout';
import {BotProfilesPage} from './pages/app/BotProfilesPage';
import {NewSubscriptionPage} from './pages/app/NewSubscriptionPage';
import {SubscriptionDetailPage} from './pages/app/SubscriptionDetailPage';
import {SubscriptionsPage} from './pages/app/SubscriptionsPage';
import {WebhooksPage} from './pages/app/WebhooksPage';
import {ChzzkLoginPage} from './pages/auth/ChzzkLoginPage';
import {LandingPage} from './pages/LandingPage';
import {NotFoundPage} from './pages/NotFoundPage';

const subscriptionNavItems = [
  {to: '/subscriptions', labelKey: 'nav.subscriptions', icon: 'bi-list-check'},
  {to: '/subscriptions/new', labelKey: 'nav.newSubscription', icon: 'bi-plus-circle'},
];

const discordNavItems = [
  {to: '/discord/webhooks', labelKey: 'nav.webhooks', icon: 'bi-link-45deg'},
  {to: '/discord/bot-profiles', labelKey: 'nav.botProfiles', icon: 'bi-robot'},
];

export const routes = [
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {index: true, element: <LandingPage />},
      {path: 'login', element: <ChzzkLoginPage />},
      {path: 'auth/chzzk/login', element: <Navigate to="/login" replace />},
      {
        element: <RequireAuth />,
        children: [
          {
            path: 'subscriptions',
            element: <SectionLayout titleKey="nav.subscriptions" icon="bi-list-check" navItems={subscriptionNavItems} />,
            children: [
              {index: true, element: <SubscriptionsPage />},
              {path: 'new', element: <NewSubscriptionPage />},
              {path: ':id', element: <SubscriptionDetailPage />},
            ],
          },
          {
            path: 'discord',
            element: <SectionLayout titleKey="nav.discordResources" icon="bi-discord" navItems={discordNavItems} />,
            children: [
              {index: true, element: <Navigate to="/discord/webhooks" replace />},
              {path: 'webhooks', element: <WebhooksPage />},
              {path: 'bot-profiles', element: <BotProfilesPage />},
            ],
          },
        ],
      },
      {path: '*', element: <NotFoundPage />},
    ],
  },
];

export const router = createBrowserRouter(routes);
