import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter, Route, Routes} from 'react-router-dom';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {BotProfilesPage} from '../pages/app/BotProfilesPage';
import {WebhooksPage} from '../pages/app/WebhooksPage';

/**
 * `jsonResponse`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {'Content-Type': 'application/json'},
  });
}

function discordWebhookUrl(host: string, path: string) {
  return `https://${host}/api/${path}`;
}

describe('discord resources UI', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('renders webhook resources from API response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [{id: 1, alias: 'Main webhook', maskedUrl: 'https://discord.com/api/webhooks/1234567890/****', hasUrl: true}],
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

    expect((await screen.findAllByText('Main webhook')).length).toBeGreaterThan(0);
    expect(screen.getAllByText('https://discord.com/api/webhooks/1234567890/****').length).toBeGreaterThan(0);
    expect(screen.queryByText('abcdefghijklmnopqrstuvwxyz')).not.toBeInTheDocument();
  });

  it('updates an existing webhook from the edit form', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/discord/webhooks/7') && init?.method === 'PUT') {
        return jsonResponse(200, {
          id: 7,
          alias: 'Edited webhook',
          maskedUrl: 'https://discord.com/api/webhooks/123/****',
          hasUrl: true,
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {
          content: [
            {
              id: 7,
              alias: init?.method === 'PUT' ? 'Edited webhook' : 'Main webhook',
              maskedUrl: 'https://discord.com/api/webhooks/123/****',
              hasUrl: true,
            },
          ],
          totalElements: 1,
        });
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/discord/webhooks']}>
        <Routes>
          <Route path="/discord/webhooks" element={<WebhooksPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    expect((await screen.findAllByText('Main webhook')).length).toBeGreaterThan(0);
    await user.click(screen.getAllByRole('button', {name: 'Main webhook 수정'})[0]);
    await user.clear(screen.getByLabelText('별칭'));
    await user.type(screen.getByLabelText('별칭'), 'Edited webhook');
    await user.clear(screen.getByLabelText('웹훅 URL'));
    const editedUrl = discordWebhookUrl('discord.com', 'webhooks/123/edited-token');
    await user.type(screen.getByLabelText('웹훅 URL'), editedUrl);
    await user.click(screen.getByRole('button', {name: '웹훅 저장'}));

    const putCall = fetchSpy.mock.calls.find(([input, init]) => String(input).includes('/discord/webhooks/7') && init?.method === 'PUT');
    expect(putCall).toBeDefined();
    expect(JSON.parse(String(putCall?.[1]?.body))).toEqual({
      alias: 'Edited webhook',
      url: editedUrl,
    });
  });

  it('blocks invalid Discord webhook URLs before submitting them', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {content: [], totalElements: 0}),
    );

    render(
      <MemoryRouter initialEntries={['/discord/webhooks']}>
        <Routes>
          <Route path="/discord/webhooks" element={<WebhooksPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('웹훅이 없습니다.');
    await user.type(screen.getByLabelText('별칭'), 'Unsafe webhook');
    await user.type(screen.getByLabelText('웹훅 URL'), `${discordWebhookUrl('discord.com', 'webhooks/123/token')}?wait=true`);

    expect(await screen.findByText('Discord 웹훅 URL 형식이 올바르지 않습니다.')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: '웹훅 추가'})).toBeDisabled();
    expect(fetchSpy.mock.calls.some(([input, init]) => (
      String(input).includes('/discord/webhooks') && init?.method === 'POST'
    ))).toBe(false);
  });

  it('submits valid discordapp.com webhook URLs', async () => {
    document.cookie = 'XSRF-TOKEN=csrf-webhook-token; path=/';
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request, init?: RequestInit) => {
      const url = typeof input === 'string' ? input : input.toString();
      if (url.includes('/discord/webhooks') && init?.method === 'POST') {
        return jsonResponse(201, {
          id: 9,
          alias: 'Legacy host webhook',
          maskedUrl: 'https://discordapp.com/api/webhooks/123/****',
          hasUrl: true,
        });
      }
      if (url.includes('/discord/webhooks')) {
        return jsonResponse(200, {content: [], totalElements: 0});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/discord/webhooks']}>
        <Routes>
          <Route path="/discord/webhooks" element={<WebhooksPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('웹훅이 없습니다.');
    await user.type(screen.getByLabelText('별칭'), 'Legacy host webhook');
    const legacyUrl = discordWebhookUrl('discordapp.com', 'v10/webhooks/123/valid-token');
    await user.type(screen.getByLabelText('웹훅 URL'), legacyUrl);
    await user.click(screen.getByRole('button', {name: '웹훅 추가'}));

    await waitFor(() => {
      const postCall = fetchSpy.mock.calls.find(([input, init]) => (
        String(input).includes('/discord/webhooks') && init?.method === 'POST'
      ));
      expect(postCall).toBeDefined();
      expect(JSON.parse(String(postCall?.[1]?.body))).toEqual({
        alias: 'Legacy host webhook',
        url: legacyUrl,
      });
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

    expect((await screen.findAllByText('Main bot')).length).toBeGreaterThan(0);
    expect(screen.getAllByText('Notifier').length).toBeGreaterThan(0);
    expect(document.querySelector('.bot-profile-avatar img')).toHaveAttribute(
      'src',
      'https://static.example.test/bot-profiles/avatar.png',
    );
  });

  it('does not render bot profile avatar URLs that are unsafe for image src', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        content: [
          {
            id: 2,
            alias: 'Unsafe bot',
            username: 'Notifier',
            avatarUrl: '//evil.example/avatar.png',
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

    expect((await screen.findAllByText('Unsafe bot')).length).toBeGreaterThan(0);
    expect(document.querySelector('.bot-profile-avatar img')).not.toBeInTheDocument();
  });

  it('rejects empty or spoofed bot profile avatar uploads before submit', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {content: [], totalElements: 0}),
    );

    render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('봇 프로필이 없습니다.');
    const avatarInput = screen.getByLabelText('프로필 이미지');

    await user.upload(avatarInput, new File([], 'avatar.png', {type: 'image/png'}));
    expect(await screen.findByText('선택한 이미지 형식 또는 크기를 지원하지 않습니다.')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: '봇 프로필 추가'})).toBeDisabled();

    await user.upload(avatarInput, new File(['avatar'], 'avatar.png.exe', {type: 'image/png'}));
    expect(await screen.findByText('PNG, JPEG, WebP 이미지만 지원합니다.')).toBeInTheDocument();
    expect(fetchSpy.mock.calls.some(([input, init]) => (
      String(input).includes('/discord/bot-profiles') && init?.method === 'POST'
    ))).toBe(false);
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
        return jsonResponse(200, {content: [], totalElements: 0});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    expect(await screen.findByText('이미지 스토리지를 사용할 수 없습니다.')).toBeInTheDocument();
    await user.type(screen.getByLabelText('별칭'), 'Main bot');
    await user.type(screen.getByLabelText('봇 이름'), 'Notifier');

    expect(screen.getByLabelText('프로필 이미지')).toBeDisabled();
    expect(screen.getByRole('button', {name: '봇 프로필 추가'})).toBeDisabled();
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

    /**
     * `resolvePost`는 관련 프론트엔드 기능을 수행합니다.
     *
     * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
     */
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
        return jsonResponse(200, {content: [], totalElements: 0});
      }
      return jsonResponse(404, {message: 'not handled'});
    });

    const {container} = render(
      <MemoryRouter initialEntries={['/discord/bot-profiles']}>
        <Routes>
          <Route path="/discord/bot-profiles" element={<BotProfilesPage />} />
        </Routes>
      </MemoryRouter>,
    );

    const user = userEvent.setup();
    await screen.findByText('봇 프로필이 없습니다.');
    await user.type(screen.getByLabelText('별칭'), 'Main bot');
    await user.type(screen.getByLabelText('봇 이름'), 'Notifier');
    await user.upload(screen.getByLabelText('프로필 이미지'), new File(['avatar'], 'avatar.png', {type: 'image/png'}));
    await waitFor(() => {
      expect(screen.getByRole('button', {name: '봇 프로필 추가'})).not.toBeDisabled();
    });
    fireEvent.submit(container.querySelector('form') as HTMLFormElement);

    expect(await screen.findByRole('button', {name: '저장 중...'})).toBeDisabled();
    expect(screen.getByLabelText('프로필 이미지')).toBeDisabled();

    resolvePost(jsonResponse(500, {message: 'minio unavailable'}));
    expect(await screen.findByText('이미지 스토리지를 사용할 수 없습니다. 아바타 업로드가 일시적으로 비활성화되었습니다.')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByRole('button', {name: '봇 프로필 추가'})).not.toBeDisabled();
    });
  });
});
