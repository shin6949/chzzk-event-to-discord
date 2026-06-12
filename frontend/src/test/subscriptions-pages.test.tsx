import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter, Route, Routes} from 'react-router-dom';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {NewSubscriptionPage} from '../pages/app/NewSubscriptionPage';
import {SubscriptionDetailPage} from '../pages/app/SubscriptionDetailPage';
import {SubscriptionsPage} from '../pages/app/SubscriptionsPage';

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

describe('subscriptions UI', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders list state from API response', async () => {
    const createdAt = '2026-02-16T00:00:00Z';
    const lastNotificationSentAt = '2026-02-16T00:30:00Z';
    const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
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
              createdAt,
              lastNotificationSentAt,
            },
            {
              id: 102,
              channelId: 'quiet-channel',
              subscriptionType: 'STREAM_ONLINE',
              enabled: false,
              webhookId: 3,
              botProfileId: 4,
              intervalMinute: 20,
              createdAt: '2026-02-15T00:00:00Z',
              lastNotificationSentAt: null,
            },
          ],
          number: 0,
          size: 10,
          totalElements: 2,
          totalPages: 1,
          first: true,
          last: true,
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {
          content: [
            {id: 1, alias: 'Main webhook'},
            {id: 3, alias: 'Backup webhook'},
          ],
        });
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {
          content: [
            {id: 2, alias: 'Main bot'},
            {id: 4, alias: 'Quiet bot'},
          ],
        });
      }

      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions?sort=id,desc']}>
        <Routes>
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect((await screen.findAllByText('방송 종료')).length).toBeGreaterThan(0);
    expect(screen.getAllByText('Main webhook').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Main bot').length).toBeGreaterThan(0);
    expect(screen.getAllByRole('link', {name: '구독 만들기'}).some((link) => link.getAttribute('href') === '/subscriptions/new')).toBe(true);
    expect(screen.getByRole('columnheader', {name: '생성일'})).toBeInTheDocument();
    expect(screen.getByRole('columnheader', {name: '마지막 알림'})).toBeInTheDocument();
    expect(screen.queryByRole('columnheader', {name: '채널'})).not.toBeInTheDocument();
    expect(screen.queryByRole('columnheader', {name: 'ID'})).not.toBeInTheDocument();
    expect(screen.queryByRole('option', {name: /ID/})).not.toBeInTheDocument();
    expect(screen.queryByText('101')).not.toBeInTheDocument();
    expect(screen.queryByText('102')).not.toBeInTheDocument();
    expect(screen.queryByText('target-channel')).not.toBeInTheDocument();
    expect(screen.queryByText('quiet-channel')).not.toBeInTheDocument();
    expect(screen.getByText(dateTimeFormatter.format(new Date(createdAt)))).toBeInTheDocument();
    expect(screen.getAllByText(dateTimeFormatter.format(new Date(lastNotificationSentAt))).length).toBeGreaterThan(0);
    expect(screen.getAllByText('없음').length).toBeGreaterThan(0);
    expect(screen.getByText('총 2개')).toBeInTheDocument();
    const listCall = fetchSpy.mock.calls.find(([input]) => String(input).includes('/subscriptions?'));
    expect(String(listCall?.[0])).toContain('sort=createdAt%2Cdesc');
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

    expect(await screen.findByText('구독이 없습니다.')).toBeInTheDocument();
  });

  it('renders API error state when list request fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(500, {message: 'failed to load subscriptions'}));

    render(
      <MemoryRouter initialEntries={['/subscriptions']}>
        <Routes>
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('failed to load subscriptions')).toBeInTheDocument();
  });

  it('requires Discord resources before calling API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(200, {
          channelId: 'owner-channel',
          role: 'USER',
          channelName: 'Owner Channel',
          profileUrl: 'https://example.test/owner.png',
        });
      }
      if (url.includes('/discord/webhooks') || url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {content: []});
      }
      if (url.includes('/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, {id: 1});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/new']}>
        <AuthProvider>
          <Routes>
            <Route path="/subscriptions/new" element={<NewSubscriptionPage />} />
            <Route path="/subscriptions" element={<h1>Subscriptions destination</h1>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    const submitButton = screen.getByRole('button', {name: '구독 만들기'});
    expect(submitButton).toBeDisabled();

    expect(await screen.findByText('Owner Channel')).toBeInTheDocument();
    expect(screen.queryByDisplayValue('owner-channel')).not.toBeInTheDocument();
    expect(document.querySelector('.subscription-channel-summary img')).toHaveAttribute('src', 'https://example.test/owner.png');

    expect(await screen.findByText('먼저 Discord 리소스를 만드세요.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: '1. 웹훅 만들기'})).toHaveAttribute('href', '/discord/webhooks?returnTo=/subscriptions/new');
    expect(screen.getByRole('link', {name: '2. 봇 프로필 만들기'})).toHaveAttribute('href', '/discord/bot-profiles?returnTo=/subscriptions/new');
    expect(submitButton).toBeDisabled();
    await user.click(submitButton);

    expect(fetchSpy.mock.calls.some(([input, init]) => String(input).includes('/subscriptions') && init?.method === 'POST')).toBe(false);
  });

  it('submits the authenticated channel id when creating a subscription', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/auth/me')) {
        return jsonResponse(200, {
          channelId: 'owner-channel',
          role: 'USER',
          channelName: 'Owner Channel',
          profileUrl: 'https://example.test/owner.png',
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {content: [{id: 11, alias: 'Main webhook', url: 'https://example.test/webhook'}]});
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {
          content: [{id: 12, alias: 'Main bot', username: 'Notifier', avatarUrl: 'https://example.test/avatar.png'}],
        });
      }
      if (url.includes('/subscriptions') && init?.method === 'POST') {
        return jsonResponse(201, {id: 1});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/new']}>
        <AuthProvider>
          <Routes>
            <Route path="/subscriptions/new" element={<NewSubscriptionPage />} />
            <Route path="/subscriptions" element={<h1>Subscriptions destination</h1>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('Owner Channel');
    expect(await screen.findByRole('option', {name: 'Main webhook'})).toBeInTheDocument();
    expect(await screen.findByRole('option', {name: 'Main bot (Notifier)'})).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: '구독 만들기'}));

    await screen.findByRole('heading', {name: 'Subscriptions destination'});
    const postCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/subscriptions') && init?.method === 'POST');
    expect(postCall).toBeDefined();
    expect(JSON.parse(String(postCall?.[1]?.body))).toEqual(
      expect.objectContaining({
        channelId: 'owner-channel',
        webhookId: 11,
        botProfileId: 12,
        language: 'Korean',
        colorHex: '00FFA3',
        showDetail: false,
        showThumbnail: true,
        showViewerCount: false,
        showTag: true,
      }),
    );
  });

  it('loads subscription detail payload for an existing subscription', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/subscriptions/42')) {
        return jsonResponse(200, {
          channelId: 'target-channel',
          subscriptionType: 'STREAM_ONLINE',
          webhookId: 11,
          botProfileId: 12,
          intervalMinute: 15,
          language: 'English',
          colorHex: '336699',
          enabled: true,
          content: 'payload content',
          showDetail: true,
          showThumbnail: false,
          showViewerCount: true,
          showTag: false,
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {content: [{id: 11, alias: 'Main webhook'}]});
      }
      if (url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {content: [{id: 12, alias: 'Main bot', username: 'Notifier'}]});
      }

      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/42']}>
        <Routes>
          <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const channelInput = (await screen.findByLabelText('대상 채널 ID')) as HTMLInputElement;
    expect(channelInput.value).toBe('target-channel');
    expect((screen.getByLabelText('웹훅') as HTMLSelectElement).value).toBe('11');
    expect((screen.getByLabelText('봇 프로필') as HTMLSelectElement).value).toBe('12');
    expect((screen.getByLabelText('언어') as HTMLSelectElement).value).toBe('English');
    expect((screen.getByLabelText('임베드 색상') as HTMLInputElement).value).toBe('#336699');
    expect(screen.getByLabelText('세부 정보 표시')).toBeChecked();
    expect(screen.getByLabelText('썸네일 표시')).not.toBeChecked();
    expect(screen.getByLabelText('시청자 수 표시')).toBeChecked();
    expect(screen.getByLabelText('태그 표시')).not.toBeChecked();
    expect(screen.getByDisplayValue('payload content')).toBeInTheDocument();
  });

  it('shows not found error on subscription detail page when API returns 404', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/subscriptions/999')) {
        return jsonResponse(404, {message: 'subscription not found'});
      }
      if (url.includes('/discord/webhooks') || url.includes('/discord/bot-profiles')) {
        return jsonResponse(200, {content: []});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/subscriptions/999']}>
        <Routes>
          <Route path="/subscriptions/:id" element={<SubscriptionDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('subscription not found')).toBeInTheDocument();
    expect(screen.getByText('구독 ID')).toBeInTheDocument();
    expect(screen.getByText('999')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: '구독으로 돌아가기'})).toHaveAttribute('href', '/subscriptions');
    expect(screen.queryByLabelText('대상 채널 ID')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', {name: '삭제'})).not.toBeInTheDocument();
    expect(fetchSpy.mock.calls.some(([input]) => String(input).includes('/discord/webhooks'))).toBe(false);
    expect(fetchSpy.mock.calls.some(([input]) => String(input).includes('/discord/bot-profiles'))).toBe(false);
  });
});
