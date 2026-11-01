import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { YouTubeSubscriptionsPage } from '../pages/app/YouTubeSubscriptionsPage';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('YouTubeSubscriptionsPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders YouTube subscription rows from the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = String(input);
      if (url.includes('/youtube/subscriptions')) {
        return jsonResponse(200, { content: [{ id: 1, youtubeChannelId: 'UC123', youtubeChannelTitle: 'Official Channel', type: 'LIVE_STARTED', enabled: true, webhookId: 1, botProfileId: 2, intervalMinute: 10 }] });
      }
      if (url.includes('/discord/webhooks')) return jsonResponse(200, { content: [{ id: 1, name: 'Main webhook' }] });
      if (url.includes('/discord/bot-profiles')) return jsonResponse(200, { content: [{ id: 2, alias: 'Main bot' }] });
      return jsonResponse(404, { message: 'not handled' });
    });

    render(<YouTubeSubscriptionsPage />);

    expect(await screen.findByText('Official Channel')).toBeInTheDocument();
    expect(screen.getByText('LIVE_STARTED')).toBeInTheDocument();
    expect(screen.getByText('UC123')).toBeInTheDocument();
  });

  it('posts a YouTube subscription with selected Discord resources', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = String(input);
      if (url.includes('/youtube/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, { id: 99 });
      }
      if (url.includes('/youtube/subscriptions')) {
        return jsonResponse(200, { content: [] });
      }
      if (url.includes('/discord/webhooks')) return jsonResponse(200, { content: [{ id: 11, name: 'Main webhook' }] });
      if (url.includes('/discord/bot-profiles')) return jsonResponse(200, { content: [{ id: 12, alias: 'Main bot' }] });
      return jsonResponse(404, { message: 'not handled' });
    });

    render(<YouTubeSubscriptionsPage />);

    const user = userEvent.setup();
    await user.type(await screen.findByLabelText('YouTube channel ID'), 'UCabcdef');
    await user.selectOptions(screen.getByLabelText('Event type'), 'VIDEO_UPLOADED');
    await user.click(screen.getByRole('button', { name: 'Create YouTube subscription' }));

    await screen.findByText('YouTube subscription created.');
    const postCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/youtube/subscriptions') && init?.method === 'POST');
    expect(postCall).toBeDefined();
    await waitFor(() => expect(JSON.parse(String(postCall?.[1]?.body))).toEqual(expect.objectContaining({
      youtubeChannelId: 'UCabcdef',
      type: 'VIDEO_UPLOADED',
      webhookId: 11,
      botProfileId: 12,
    })));
  });
});
