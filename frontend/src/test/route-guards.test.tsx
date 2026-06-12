import {render, screen, waitFor} from '@testing-library/react';
import {RouterProvider, createMemoryRouter} from 'react-router-dom';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {RequireAdmin} from '../auth/RouteGuards';
import {clearSession, setSession} from '../auth/session';

/**
 * `jsonResponse`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 d252252.
 */
function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {'Content-Type': 'application/json'},
  });
}

/**
 * `mockAuthMe`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 d252252.
 */
function mockAuthMe(status: number, body: object = {}) {
  vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
    const url = typeof input === 'string' ? input : input.toString();
    if (url.includes('/auth/me')) {
      return jsonResponse(status, body);
    }
    if (url.includes('/auth/refresh')) {
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

  /**
   * `renderGuard`는 관련 프론트엔드 기능을 수행합니다.
   *
   * Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 d252252.
   */
  function renderGuard(initialEntry: string) {
    const router = createMemoryRouter(
      [
        {
          path: '/',
          children: [
            {path: 'login', element: <h1>Login page</h1>},
            {path: 'subscriptions', element: <h1>Subscriptions page</h1>},
            {
              element: <RequireAdmin />,
              children: [{path: 'admin', element: <h1>Admin page</h1>}],
            },
          ],
        },
      ],
      {initialEntries: [initialEntry]},
    );

    render(
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>,
    );

    return router;
  }

  it('redirects unauthenticated users away from admin routes', async () => {
    mockAuthMe(401, {message: 'Unauthorized'});

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', {name: 'Login page'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/login');
    });
  });

  it('redirects non-admin users to subscriptions from admin routes', async () => {
    mockAuthMe(200, {channelId: 'channel-user', role: 'USER'});

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', {name: 'Subscriptions page'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/subscriptions');
    });
  });

  it('allows admin users to stay on admin routes', async () => {
    mockAuthMe(200, {channelId: 'channel-admin', role: 'ADMIN'});

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', {name: 'Admin page'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/admin');
    });
  });

  it('keeps cached session when auth check fails transiently', async () => {
    setSession({channelId: 'channel-admin', role: 'ADMIN'});
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));

    const router = renderGuard('/admin');

    expect(await screen.findByRole('heading', {name: 'Admin page'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/admin');
    });
  });
});
