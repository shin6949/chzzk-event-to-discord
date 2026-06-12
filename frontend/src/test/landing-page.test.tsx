import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter, Outlet, Route, Routes} from 'react-router-dom';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {LandingPage} from '../pages/LandingPage';

/**
 * `jsonResponse`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {'Content-Type': 'application/json'},
  });
}

/**
 * `renderLanding`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function renderLanding() {
  const showTutorial = vi.fn();

  render(
    <MemoryRouter initialEntries={['/']}>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Outlet context={{showTutorial}} />}>
            <Route index element={<LandingPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );

  return showTutorial;
}

describe('landing page UX', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows a no-code setup flow and tutorial action for guests', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/refresh')) {
        return jsonResponse(401, {message: 'refresh required'});
      }
      return jsonResponse(401, {message: 'login required'});
    });

    const showTutorial = renderLanding();
    const user = userEvent.setup();

    expect(await screen.findByText('먼저 CHZZK 계정으로 로그인하세요.')).toBeInTheDocument();
    expect(screen.getByText('처음 설정 순서')).toBeInTheDocument();
    expect(screen.getByText('1. 웹훅')).toBeInTheDocument();
    expect(screen.getByText('2. 봇 프로필')).toBeInTheDocument();
    expect(screen.getByText('3. 구독')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'CHZZK로 로그인'})).toHaveAttribute('href', '/login');

    await user.click(screen.getByRole('button', {name: '튜토리얼 보기'}));
    expect(showTutorial).toHaveBeenCalledTimes(1);
  });

  it('points signed-in users directly to subscription creation', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        channelId: 'owner-channel',
        role: 'USER',
        channelName: 'Owner Channel',
        profileUrl: 'https://example.test/owner.png',
      }),
    );

    renderLanding();

    expect(await screen.findByText('로그인 상태가 확인되었습니다.')).toBeInTheDocument();
    expect(screen.getAllByRole('link', {name: '구독 만들기'}).some((link) => link.getAttribute('href') === '/subscriptions/new')).toBe(true);
    expect(screen.getByRole('link', {name: 'Discord 리소스'})).toHaveAttribute('href', '/discord/webhooks');
  });
});
