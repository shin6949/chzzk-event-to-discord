import { render, screen, waitFor } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext';
import { clearSession } from '../auth/session';
import { routes } from '../router';

describe('router scaffold', () => {
  beforeEach(() => {
    clearSession();
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function renderWithSession(initialEntry: string) {
    const router = createMemoryRouter(routes, {
      initialEntries: [initialEntry],
    });

    const view = render(
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>,
    );

    return { view, router };
  }

  function jsonResponse(status: number, body: unknown) {
    return new Response(JSON.stringify(body), {
      status,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  function mockAuthMe(status: number, body: object = {}) {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(status, body);
      }
      if (url.includes('/auth/chzzk/login')) {
        return jsonResponse(200, { authorizationUrl: 'https://auth.example/login' });
      }
      if (url.includes('/subscriptions')) {
        return jsonResponse(200, { content: [], first: true, last: true, number: 0, size: 10, totalElements: 0, totalPages: 1 });
      }
      return jsonResponse(200, {});
    });
  }

  it('redirects unauthenticated users from protected /subscriptions routes', async () => {
    mockAuthMe(401, { message: 'Unauthorized' });

    const { router } = renderWithSession('/subscriptions');
    expect(await screen.findByRole('heading', { name: 'Login' })).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/login');
    });
    expect(globalThis.fetch).toHaveBeenCalled();
    const firstCall = vi.mocked(globalThis.fetch).mock.calls[0]?.[0];
    expect(String(firstCall)).toContain(
      '/auth/me',
    );
  });

  it('allows authenticated users to open protected /subscriptions routes', async () => {
    mockAuthMe(200, { channelId: 'channel-user', role: 'USER' });

    const { router } = renderWithSession('/subscriptions');
    expect(await screen.findByText('Manage your notification subscriptions.')).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/subscriptions');
    });
    expect(screen.getByText('No subscriptions found.')).toBeInTheDocument();
  });

  it('redirects to authorization URL from /login', async () => {
    mockAuthMe(401, { message: 'Unauthorized' });

    const originalLocation = window.location;
    const hrefSpy = vi.fn();
    const mockLocation = Object.create(originalLocation) as Location;

    Object.defineProperty(mockLocation, 'href', {
      configurable: true,
      get() {
        return hrefSpy.mock.lastCall?.[0] ?? originalLocation.href;
      },
      set(value: string) {
        hrefSpy(String(value));
      },
    });

    Object.defineProperty(window, 'location', {
      configurable: true,
      value: mockLocation,
    });

    try {
      renderWithSession('/login');
      const button = await screen.findByRole('button', { name: 'Login with Chzzk' });
      await userEvent.click(button);
      await waitFor(() => {
        expect(hrefSpy).toHaveBeenCalledWith('https://auth.example/login');
      });
    } finally {
      Object.defineProperty(window, 'location', {
        configurable: true,
        value: originalLocation,
      });
    }
  });
});
