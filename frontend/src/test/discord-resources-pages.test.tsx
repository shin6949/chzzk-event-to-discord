import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BotProfilesPage } from '../pages/app/BotProfilesPage';
import { WebhooksPage } from '../pages/app/WebhooksPage';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('discord resources UI', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders webhook resources from API response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [{ id: 1, alias: 'Main webhook', url: 'https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz' }],
        totalElements: 1,
      }),
    );

    render(
      <MemoryRouter initialEntries={['/discord/webhooks']}>
        <Routes>
          <Route path="/discord/webhooks" element={<WebhooksPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('Main webhook')).toBeInTheDocument();
    expect(screen.getByText(/https:\/\/discord.com\/api\/web/)).toBeInTheDocument();
  });

  it('updates an existing webhook from the edit form', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/discord/webhooks/7') && init?.method === 'PUT') {
        return jsonResponse(200, {
          id: 7,
          alias: 'Edited webhook',
          url: 'https://discord.com/api/webhooks/123/edited-token',
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {
          content: [
            {
              id: 7,
              alias: init?.method === 'PUT' ? 'Edited webhook' : 'Main webhook',
              url: init?.method === 'PUT'
                ? 'https://discord.com/api/webhooks/123/edited-token'
                : 'https://discord.com/api/webhooks/123/original-token',
            },
          ],
          totalElements: 1,
        });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/discord/webhooks']}>
        <Routes>
          <Route path="/discord/webhooks" element={<WebhooksPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    expect(await screen.findByText('Main webhook')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Edit Main webhook' }));
    await user.clear(screen.getByLabelText('Alias'));
    await user.type(screen.getByLabelText('Alias'), 'Edited webhook');
    await user.clear(screen.getByLabelText('Webhook URL'));
    await user.type(screen.getByLabelText('Webhook URL'), 'https://discord.com/api/webhooks/123/edited-token');
    await user.click(screen.getByRole('button', { name: 'Save webhook' }));

    const putCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/discord/webhooks/7') && init?.method === 'PUT');
    expect(putCall).toBeDefined();
    expect(JSON.parse(String(putCall?.[1]?.body))).toEqual({
      alias: 'Edited webhook',
      url: 'https://discord.com/api/webhooks/123/edited-token',
    });
  });

  it('renders bot profile resources from API response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [
          {
            id: 2,
            alias: 'Main bot',
            username: 'Notifier',
            avatarUrl: 'https://static.example.test/bot-profiles/avatar.png',
          },
        ],
        totalElements: 1,
      }),
    );

    render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('Main bot')).toBeInTheDocument();
    expect(screen.getByText('Notifier')).toBeInTheDocument();
    expect(document.querySelector('.bot-profile-avatar img')).toHaveAttribute(
      'src',
      'https://static.example.test/bot-profiles/avatar.png',
    );
  });

  it('disables bot profile image upload when backend reports storage unavailable', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/system/capabilities')) {
        return jsonResponse(200, {
          uploads: {
            botProfileAvatar: {
              enabled: false,
              reason: 'ApiCallTimeoutException: timeout',
              checkedAt: '2026-05-21T02:20:00Z',
            },
          },
        });
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [], totalElements: 0 });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    expect(await screen.findByText('Image storage is unavailable.')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Alias'), 'Main bot');
    await user.type(screen.getByLabelText('Bot name'), 'Notifier');

    expect(screen.getByLabelText('Profile image')).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Add bot profile' })).toBeDisabled();
  });

  it('disables bot profile upload controls while saving and shows storage errors clearly', async () => {
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:avatar-preview'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });

    let resolvePost: (response: Response) => void = () => undefined;
    const postResponse = new Promise<Response>((resolve) => {
      resolvePost = resolve;
    });
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/discord/bot-profiles') && init?.method === 'POST') {
        return postResponse;
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, { content: [], totalElements: 0 });
      }
      return jsonResponse(404, { message: 'not handled' });
    });

    const { container } = render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('No bot profiles found.');
    await user.type(screen.getByLabelText('Alias'), 'Main bot');
    await user.type(screen.getByLabelText('Bot name'), 'Notifier');
    await user.upload(screen.getByLabelText('Profile image'), new File(['avatar'], 'avatar.png', { type: 'image/png' }));
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Add bot profile' })).not.toBeDisabled();
    });
    fireEvent.submit(container.querySelector('form') as HTMLFormElement);

    expect(await screen.findByRole('button', { name: 'Saving...' })).toBeDisabled();
    expect(screen.getByLabelText('Profile image')).toBeDisabled();

    resolvePost(jsonResponse(500, { message: 'minio unavailable' }));
    expect(await screen.findByText('Image storage is unavailable. Avatar uploads are temporarily disabled.')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Add bot profile' })).not.toBeDisabled();
    });
  });
});
