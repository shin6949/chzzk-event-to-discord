import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
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

  it('validates create form before calling API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(201, { id: 1 }));

    render(
      <MemoryRouter initialEntries={['/subscriptions/new']}>
        <Routes>
          <Route path="/subscriptions/new" element={<NewSubscriptionPage />} />
          <Route path="/subscriptions" element={<h1>Subscriptions destination</h1>} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    const submitButton = screen.getByRole('button', { name: 'Create subscription' });
    expect(submitButton).toBeDisabled();

    await user.type(screen.getByLabelText('Channel ID (target channel)'), 'target-channel');
    await user.type(screen.getByLabelText('Webhook ID'), 'abc');
    await user.type(screen.getByLabelText('Bot Profile ID'), '3');

    expect(submitButton).toBeEnabled();
    await user.click(submitButton);

    expect(await screen.findByText('Please fill required fields with valid numeric values.')).toBeInTheDocument();
    expect(fetchSpy).not.toHaveBeenCalled();
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
    expect(screen.getByDisplayValue('11')).toBeInTheDocument();
    expect(screen.getByDisplayValue('payload content')).toBeInTheDocument();
  });

  it('shows not found error on subscription detail page when API returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(404, { message: 'subscription not found' }));

    render(
      <MemoryRouter initialEntries={['/subscriptions/999']}>
        <Routes>
          <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('subscription not found')).toBeInTheDocument();
  });
});
