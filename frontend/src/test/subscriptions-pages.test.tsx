import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext';
import { NewSubscriptionPage } from '../pages/app/NewSubscriptionPage';
import { SubscriptionDetailPage } from '../pages/app/SubscriptionDetailPage';
import { SubscriptionsPage } from '../pages/app/SubscriptionsPage';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('subscriptions UI', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders list state from API response', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/subscriptions?')) {
        return jsonResponse(200, {
          content: [
            {
              id: 101,
              channelId: 'target-channel',
              subscriptionType: 'STREAM_OFFLINE',
              enabled: true,
              webhookId: 1,
              botProfileId: 2,
              intervalMinute: 10,
              createdAt: '2026-02-16T00:00:00Z',
            },
          ],
          number: 0,
          size: 10,
          totalElements: 1,
          totalPages: 1,
          first: true,
          last: true,
        });
      }

      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions']}>
        <Routes>
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('target-channel')).toBeInTheDocument();
    expect(screen.getByText('STREAM_OFFLINE')).toBeInTheDocument();
    expect(screen.getByText('1 item(s)')).toBeInTheDocument();
  });

  it('renders empty list message when API returns no subscriptions', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [],
        number: 0,
        size: 10,
        totalElements: 0,
        totalPages: 1,
        first: true,
        last: true,
      }),
    );

    render(
      <MemoryRouter initialEntries={['/subscriptions']}>
        <Routes>
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('No subscriptions found.')).toBeInTheDocument();
  });

  it('renders API error state when list request fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(500, { message: 'failed to load subscriptions' }));

    render(
      <MemoryRouter initialEntries={['/subscriptions']}>
        <Routes>
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('failed to load subscriptions')).toBeInTheDocument();
  });

  it('requires Discord resources before calling API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(200, {
          channelId: 'owner-channel',
          role: 'USER',
          channelName: 'Owner Channel',
          profileUrl: 'https://example.test/owner.png',
        });
      }
      if (url.includes('/discord/webhooks') || url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [] });
      }
      if (url.includes('/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, { id: 1 });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/new']}>
        <AuthProvider>
          <Routes>
            <Route path="/subscriptions/new" element={<NewSubscriptionPage />} />
            <Route path="/subscriptions" element={<h1>Subscriptions destination</h1>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    const submitButton = screen.getByRole('button', { name: 'Create subscription' });
    expect(submitButton).toBeDisabled();

    expect(await screen.findByText('Owner Channel')).toBeInTheDocument();
    expect(screen.queryByDisplayValue('owner-channel')).not.toBeInTheDocument();
    expect(document.querySelector('.subscription-channel-summary img')).toHaveAttribute('src', 'https://example.test/owner.png');

    expect(await screen.findByText('Create at least one webhook and one bot profile before adding a subscription.')).toBeInTheDocument();
    expect(submitButton).toBeDisabled();
    await user.click(submitButton);

    expect(fetchSpy.mock.calls.some(([input, init]) => String(input).includes('/subscriptions') && init?.method === 'POST')).toBe(false);
  });

  it('submits the authenticated channel id when creating a subscription', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(200, {
          channelId: 'owner-channel',
          role: 'USER',
          channelName: 'Owner Channel',
          profileUrl: 'https://example.test/owner.png',
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, { content: [{ id: 11, alias: 'Main webhook', url: 'https://example.test/webhook' }] });
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {
          content: [{ id: 12, alias: 'Main bot', username: 'Notifier', avatarUrl: 'https://example.test/avatar.png' }],
        });
      }
      if (url.includes('/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, { id: 1 });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/new']}>
        <AuthProvider>
          <Routes>
            <Route path="/subscriptions/new" element={<NewSubscriptionPage />} />
            <Route path="/subscriptions" element={<h1>Subscriptions destination</h1>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('Owner Channel');
    expect(await screen.findByRole('option', { name: 'Main webhook' })).toBeInTheDocument();
    expect(await screen.findByRole('option', { name: 'Main bot (Notifier)' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Create subscription' }));

    await screen.findByRole('heading', { name: 'Subscriptions destination' });
    const postCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/subscriptions') && init?.method === 'POST');
    expect(postCall).toBeDefined();
    expect(JSON.parse(String(postCall?.[1]?.body))).toEqual(
      expect.objectContaining({
        channelId: 'owner-channel',
        webhookId: 11,
        botProfileId: 12,
      }),
    );
  });

  it('loads subscription detail payload for an existing subscription', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/subscriptions/42')) {
        return jsonResponse(200, {
          channelId: 'target-channel',
          subscriptionType: 'STREAM_OFFLINE',
          webhookId: 11,
          botProfileId: 12,
          intervalMinute: 15,
          enabled: true,
          content: 'payload content',
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, { content: [{ id: 11, alias: 'Main webhook' }] });
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [{ id: 12, alias: 'Main bot', username: 'Notifier' }] });
      }

      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/42']}>
        <Routes>
          <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const channelInput = (await screen.findByLabelText('Channel ID (target channel)')) as HTMLInputElement;
    expect(channelInput.value).toBe('target-channel');
    expect((screen.getByLabelText('Webhook') as HTMLSelectElement).value).toBe('11');
    expect((screen.getByLabelText('Bot Profile') as HTMLSelectElement).value).toBe('12');
    expect(screen.getByDisplayValue('payload content')).toBeInTheDocument();
  });

  it('shows not found error on subscription detail page when API returns 404', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/subscriptions/999')) {
        return jsonResponse(404, { message: 'subscription not found' });
      }
      if (url.includes('/discord/webhooks') || url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [] });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/999']}>
        <Routes>
          <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('subscription not found')).toBeInTheDocument();
    expect(screen.getByText('Subscription ID')).toBeInTheDocument();
    expect(screen.getByText('999')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to subscriptions' })).toHaveAttribute('href', '/subscriptions');
    expect(screen.queryByLabelText('Channel ID (target channel)')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument();
    expect(fetchSpy.mock.calls.some(([input]) => String(input).includes('/discord/webhooks'))).toBe(false);
    expect(fetchSpy.mock.calls.some(([input]) => String(input).includes('/discord/bot-profiles'))).toBe(false);
  });
});
