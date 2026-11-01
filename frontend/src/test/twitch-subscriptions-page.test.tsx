import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TwitchSubscriptionsPage } from '../pages/app/TwitchSubscriptionsPage';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('TwitchSubscriptionsPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders configured Twitch channels from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, [
        {
          id: 1,
          broadcasterUserId: '1234',
          broadcasterLogin: 'streamer',
          broadcasterDisplayName: 'Streamer',
          enabled: true,
          eventsubOnlineId: 'online-id',
          eventsubOfflineId: 'offline-id',
        },
      ]),
    );

    render(
      <MemoryRouter initialEntries={['/subscriptions/twitch']}>
        <Routes>
          <Route path="/subscriptions/twitch" element={<TwitchSubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('Streamer')).toBeInTheDocument();
    expect(screen.getByText('1234')).toBeInTheDocument();
    expect(screen.getByText('Enabled')).toBeInTheDocument();
    expect(screen.getByText(/online: online-id/)).toBeInTheDocument();
  });

  it('posts Twitch EventSub form data and refreshes the list', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (_input: string | URL | Request, init?: RequestInit) => {
      if (init?.method === 'POST') {
        return jsonResponse(201, { id: 2 });
      }
      return jsonResponse(200, []);
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/twitch']}>
        <Routes>
          <Route path="/subscriptions/twitch" element={<TwitchSubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Twitch login'), 'streamer');
    await user.type(screen.getByLabelText('Discord webhook URL'), 'https://discord.com/api/webhooks/test');
    await user.click(screen.getByRole('button', { name: 'Create EventSub subscriptions' }));

    await waitFor(() => expect(fetchSpy.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(true));
    const postCall = fetchSpy.mock.calls.find(([, init]) => init?.method === 'POST');
    expect(String(postCall?.[0])).toContain('/twitch/subscriptions');
    expect(JSON.parse(String(postCall?.[1]?.body))).toEqual({
      broadcasterLogin: 'streamer',
      discordWebhookUrl: 'https://discord.com/api/webhooks/test',
      enabled: true,
    });
  });
});
