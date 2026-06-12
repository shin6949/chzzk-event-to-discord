import {render, screen, waitFor} from '@testing-library/react';
import {RouterProvider, createMemoryRouter} from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {clearSession} from '../auth/session';
import i18n, {LANGUAGE_STORAGE_KEY} from '../i18n';
import {routes} from '../router';
import {FIRST_VISIT_TUTORIAL_DISMISSED_KEY} from '../tutorial/firstVisitTutorial';

describe('router scaffold', () => {
  beforeEach(() => {
    clearSession();
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /**
   * `renderWithSession`는 관련 프론트엔드 기능을 수행합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  function renderWithSession(initialEntry: string) {
    const router = createMemoryRouter(routes, {
      initialEntries: [initialEntry],
    });

    const view = render(
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>,
    );

    return {view, router};
  }

  /**
   * `jsonResponse`는 관련 프론트엔드 기능을 수행합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
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
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  function mockAuthMe(
    status: number,
    body: object = {},
    authorizationUrl = 'https://chzzk.naver.com/account-interlock?state=test-state',
  ) {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(status, body);
      }
      if (url.includes('/auth/chzzk/login')) {
        return jsonResponse(200, {authorizationUrl});
      }
      if (url.includes('/auth/refresh')) {
        return jsonResponse(status, body);
      }
      if (url.includes('/subscriptions')) {
        return jsonResponse(200, {
          content: [],
          first: true,
          last: true,
          number: 0,
          size: 10,
          totalElements: 0,
          totalPages: 1,
        });
      }
      return jsonResponse(200, {});
    });
  }

  it('redirects unauthenticated users from protected /subscriptions routes', async () => {
    mockAuthMe(401, {message: 'Unauthorized'});

    const {router} = renderWithSession('/subscriptions');
    expect(await screen.findByRole('heading', {name: '로그인'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/login');
    });
    expect(globalThis.fetch).toHaveBeenCalled();
    const firstCall = vi.mocked(globalThis.fetch).mock.calls[0]?.[0];
    expect(String(firstCall)).toContain(
      '/auth/me',
    );
  });

  it('shows the first visit tutorial and stores dismissal state', async () => {
    mockAuthMe(401, {message: 'Unauthorized'});

    const {router} = renderWithSession('/');
    expect(await screen.findByRole('dialog', {name: 'Discord용 스트리밍 알림 설정'})).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', {name: '로그인으로 이동'}));
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/login');
    });
    expect(window.localStorage.getItem(FIRST_VISIT_TUTORIAL_DISMISSED_KEY)).toBe('true');
  });

  it('keeps the tutorial dismissed until the navbar button reopens it', async () => {
    window.localStorage.setItem(FIRST_VISIT_TUTORIAL_DISMISSED_KEY, 'true');
    mockAuthMe(401, {message: 'Unauthorized'});

    renderWithSession('/');
    await screen.findByRole('heading', {name: '방송 이벤트를 Discord로 자동 전송하세요'});
    expect(screen.queryByRole('dialog', {name: 'Discord용 스트리밍 알림 설정'})).not.toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', {name: '튜토리얼'}));
    expect(await screen.findByRole('dialog', {name: 'Discord용 스트리밍 알림 설정'})).toBeInTheDocument();
  });

  it('switches UI language from the navbar and persists the choice', async () => {
    window.localStorage.setItem(FIRST_VISIT_TUTORIAL_DISMISSED_KEY, 'true');
    mockAuthMe(401, {message: 'Unauthorized'});

    const {view} = renderWithSession('/');
    expect(await screen.findByRole('heading', {name: '방송 이벤트를 Discord로 자동 전송하세요'})).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', {name: 'English'}));
    expect(await screen.findByRole('heading', {name: 'Send stream events to Discord automatically'})).toBeInTheDocument();
    expect(window.localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('en');
    expect(i18n.language).toBe('en');

    view.unmount();
    renderWithSession('/');
    expect(await screen.findByRole('heading', {name: 'Send stream events to Discord automatically'})).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', {name: '한국어'}));
    expect(await screen.findByRole('heading', {name: '방송 이벤트를 Discord로 자동 전송하세요'})).toBeInTheDocument();
    expect(window.localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('ko');
  });

  it('sends authenticated users from the tutorial to Discord setup', async () => {
    mockAuthMe(200, {
      channelId: 'channel-user',
      role: 'USER',
      channelName: 'Channel User',
      profileUrl: 'https://example.test/channel-user.png',
    });

    const {router} = renderWithSession('/');
    expect(await screen.findByRole('dialog', {name: 'Discord용 스트리밍 알림 설정'})).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', {name: 'Discord 설정 시작'}));
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/discord/webhooks');
    });
  });

  it('allows authenticated users to open protected /subscriptions routes', async () => {
    mockAuthMe(200, {
      channelId: 'channel-user',
      role: 'USER',
      channelName: 'Channel User',
      profileUrl: 'https://example.test/channel-user.png',
    });

    const {router} = renderWithSession('/subscriptions');
    expect(await screen.findByText('알림 구독을 관리합니다.')).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/subscriptions');
    });
    expect(screen.getByText('Channel User')).toBeInTheDocument();
    expect(screen.queryByText('USER')).not.toBeInTheDocument();
    expect(screen.getByText('구독이 없습니다.')).toBeInTheDocument();
  });

  it('marks Discord Resources active for bot profile routes', async () => {
    mockAuthMe(200, {
      channelId: 'channel-user',
      role: 'USER',
      channelName: 'Channel User',
      profileUrl: 'https://example.test/channel-user.png',
    });

    const {router} = renderWithSession('/discord/bot-profiles');
    expect(await screen.findByRole('heading', {name: '봇 프로필'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/discord/bot-profiles');
    });

    expect(screen.getByRole('link', {name: 'Discord 리소스'})).toHaveClass('active', 'fw-semibold');
  });

  it('marks only the exact subscription sidebar item active', async () => {
    mockAuthMe(200, {
      channelId: 'channel-user',
      role: 'USER',
      channelName: 'Channel User',
      profileUrl: 'https://example.test/channel-user.png',
    });

    const {router} = renderWithSession('/subscriptions/new');
    expect(await screen.findByRole('heading', {name: '새 구독'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/subscriptions/new');
    });

    const subscriptionSidebarLink = screen
      .getAllByRole('link', {name: '구독'})
      .find((link) => link.classList.contains('list-group-item'));
    expect(subscriptionSidebarLink).toBeDefined();
    expect(subscriptionSidebarLink).not.toHaveClass('active');
    expect(screen.getByRole('link', {name: '새 구독'})).toHaveClass('active');
  });

  it('marks only the exact Discord resource sidebar item active', async () => {
    mockAuthMe(200, {
      channelId: 'channel-user',
      role: 'USER',
      channelName: 'Channel User',
      profileUrl: 'https://example.test/channel-user.png',
    });

    const {router} = renderWithSession('/discord/bot-profiles');
    expect(await screen.findByRole('heading', {name: '봇 프로필'})).toBeInTheDocument();
    await waitFor(() => {
      expect(router.state.location.pathname).toBe('/discord/bot-profiles');
    });

    expect(screen.getByRole('link', {name: '웹훅'})).not.toHaveClass('active');
    expect(screen.getByRole('link', {name: '봇 프로필'})).toHaveClass('active');
  });

  it('redirects to authorization URL from /login', async () => {
    mockAuthMe(401, {message: 'Unauthorized'});

    const originalLocation = window.location;
    const hrefSpy = vi.fn();
    const mockLocation = Object.create(originalLocation) as Location;

    Object.defineProperty(mockLocation, 'href', {
      configurable: true,
      /**
       * `get`는 데이터를 요청하거나 조회합니다.
       *
       * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
       */
      get() {
        return hrefSpy.mock.lastCall?.[0] ?? originalLocation.href;
      },
      /**
       * `set`는 사용자 동작 또는 상태 변경을 처리합니다.
       *
       * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
       */
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
      const button = await screen.findByRole('button', {name: 'CHZZK로 로그인'});
      await userEvent.click(button);
      await waitFor(() => {
        expect(hrefSpy).toHaveBeenCalledWith('https://chzzk.naver.com/account-interlock?state=test-state');
      });
    } finally {
      Object.defineProperty(window, 'location', {
        configurable: true,
        value: originalLocation,
      });
    }
  });

  it('blocks untrusted authorization URLs from /login', async () => {
    mockAuthMe(401, {message: 'Unauthorized'}, 'https://evil.example/login?state=test-state');

    const originalLocation = window.location;
    const hrefSpy = vi.fn();
    const mockLocation = Object.create(originalLocation) as Location;

    Object.defineProperty(mockLocation, 'href', {
      configurable: true,
      /**
       * `get`는 데이터를 요청하거나 조회합니다.
       *
       * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
       */
      get() {
        return hrefSpy.mock.lastCall?.[0] ?? originalLocation.href;
      },
      /**
       * `set`는 사용자 동작 또는 상태 변경을 처리합니다.
       *
       * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
       */
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
      const button = await screen.findByRole('button', {name: 'CHZZK로 로그인'});
      await userEvent.click(button);
      expect(await screen.findByText('신뢰할 수 없는 로그인 URL입니다. 다시 시도하세요.')).toBeInTheDocument();
      expect(hrefSpy).not.toHaveBeenCalled();
    } finally {
      Object.defineProperty(window, 'location', {
        configurable: true,
        value: originalLocation,
      });
    }
  });
});
