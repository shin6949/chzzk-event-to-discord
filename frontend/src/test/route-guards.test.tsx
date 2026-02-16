import { render, screen, waitFor } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../auth/AuthContext';
import { RequireAdmin } from '../auth/RouteGuards';
import { clearSession } from '../auth/session';

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
    return jsonResponse(200, {});
  });
}

describe('route guards', () => {
  beforeEach(() => {
    clearSession();
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function renderGuard(initialEntry: string) {
    const router = createMemoryRouter(
      [
        {
          path: '/',
          children: [
            { path: 'login', element: <h1>Login page</h1> },
            { path: 'subscriptions', element: <h1>Subscriptions page</h1> },
            {
              element: <RequireAdmin />,
              children: [{ path: 'admin', element: <h1>Admin page</h1> }],
            },
          ],
        },
      ],
      { initialEntries: [initialEntry] },
    );

    render(
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>,
    );

    return router;
  }

  it('redirects unauthenticated users away from admin routes', async () => {
    mockAuthMe(401, { message: 'Unauthorized' });

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', { name: 'Login page' })).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/login');
    });
  });

  it('redirects non-admin users to subscriptions from admin routes', async () => {
    mockAuthMe(200, { channelId: 'channel-user', role: 'USER' });

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', { name: 'Subscriptions page' })).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/subscriptions');
    });
  });

  it('allows admin users to stay on admin routes', async () => {
    mockAuthMe(200, { channelId: 'channel-admin', role: 'ADMIN' });

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', { name: 'Admin page' })).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/admin');
    });
  });
});
