import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { NewSoopSubscriptionPage } from '../pages/app/NewSoopSubscriptionPage';
import { SoopSubscriptionsPage } from '../pages/app/SoopSubscriptionsPage';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('SOOP subscriptions UI', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders SOOP subscription list state from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [
          {
            id: 7,
            soopUserId: 'soop1234',
            soopChannelName: 'SOOP Creator',
            live: true,
            liveTitle: 'Live now',
            liveUrl: 'https://play.sooplive.co.kr/soop1234/123',
            webhookId: 11,
            botProfileId: 12,
            enabled: true,
            notifyOnline: true,
            notifyOffline: true,
          },
        ],
        totalElements: 1,
      }),
    );

    render(
      <MemoryRouter initialEntries={['/soop/subscriptions']}>
        <Routes>
          <Route path="/soop/subscriptions" element={<SoopSubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('SOOP Creator')).toBeInTheDocument();
    expect(screen.getByText('soop1234')).toBeInTheDocument();
    expect(screen.getByText('Live')).toBeInTheDocument();
    expect(screen.getByText('1 SOOP subscription(s)')).toBeInTheDocument();
  });

  it('submits a SOOP subscription payload', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, { content: [{ id: 11, alias: 'Main webhook', url: 'https://example.test/webhook' }] });
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [{ id: 12, alias: 'Main bot', username: 'Notifier', avatarUrl: 'https://example.test/avatar.png' }] });
      }
      if (url.includes('/soop/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, { id: 7 });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/soop/subscriptions/new']}>
        <Routes>
          <Route path="/soop/subscriptions/new" element={<NewSoopSubscriptionPage />} />
          <Route path="/soop/subscriptions" element={<h1>SOOP destination</h1>} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('SOOP user ID'), 'soop1234');
    expect(await screen.findByRole('option', { name: 'Main webhook' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Create SOOP subscription' }));

    await screen.findByRole('heading', { name: 'SOOP destination' });
    const postCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/soop/subscriptions') && init?.method === 'POST');
    expect(postCall).toBeDefined();
    expect(JSON.parse(String(postCall?.[1]?.body))).toEqual(
      expect.objectContaining({
        soopUserId: 'soop1234',
        webhookId: 11,
        botProfileId: 12,
        notifyOnline: true,
        notifyOffline: true,
      }),
    );
  });
});
