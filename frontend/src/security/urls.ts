const DISCORD_WEBHOOK_PATH = /^\/api(?:\/v\d+)?\/webhooks\/\d+\/[A-Za-z0-9._-]+\/?$/;
const TRUSTED_CHZZK_AUTH_HOST = 'chzzk.naver.com';
const TRUSTED_CHZZK_AUTH_PATH = '/account-interlock';
const SAFE_IMAGE_SCHEMES = new Set(['https:', 'blob:']);
const SAFE_LOOPBACK_HOSTS = new Set(['localhost', '127.0.0.1', '[::1]', '::1']);
const ALLOWED_AVATAR_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.webp'];
const FALLBACK_ORIGIN = 'http://localhost';

/**
 * `currentOrigin`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function currentOrigin(): string {
  if (typeof window !== 'undefined' && window.location?.origin) {
    return window.location.origin;
  }
  return FALLBACK_ORIGIN;
}

/**
 * `hasAsciiControlCharacter`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function hasAsciiControlCharacter(value: string): boolean {
  return Array.from(value).some((character) => {
    const code = character.charCodeAt(0);
    return code <= 0x1F || code === 0x7F;
  });
}

/**
 * `isSameOriginOrRelativeUrl`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isSameOriginOrRelativeUrl(value: string, baseOrigin = currentOrigin()): boolean {
  const trimmed = value.trim();
  if (!trimmed || /^[a-z][a-z0-9+.-]*:/i.test(trimmed) || trimmed.startsWith('//')) {
    try {
      return new URL(trimmed, baseOrigin).origin === baseOrigin;
    } catch {
      return false;
    }
  }
  return trimmed.startsWith('/');
}

/**
 * `isSafeApiPath`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isSafeApiPath(path: string): boolean {
  const normalizedPath = path.trim();
  if (!normalizedPath || normalizedPath.startsWith('//') || /^[a-z][a-z0-9+.-]*:/i.test(normalizedPath)) {
    return false;
  }
  if (
    hasAsciiControlCharacter(normalizedPath) ||
    normalizedPath.includes('\\') ||
    normalizedPath.includes('#')
  ) {
    return false;
  }

  const pathOnly = normalizedPath.split(/[?#]/, 1)[0] ?? '';
  return pathOnly.split('/').every((segment) => {
    try {
      const decodedSegment = decodeURIComponent(segment);
      return decodedSegment !== '.' &&
        decodedSegment !== '..' &&
        !hasAsciiControlCharacter(decodedSegment) &&
        !decodedSegment.includes('/') &&
        !decodedSegment.includes('\\');
    } catch {
      return false;
    }
  });
}

/**
 * `isSafeInternalPath`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isSafeInternalPath(value: string, allowedPrefixes: string[] = ['/']): boolean {
  const trimmed = value.trim();
  if (!trimmed || !trimmed.startsWith('/') || trimmed.startsWith('//') || !isSafeApiPath(trimmed)) {
    return false;
  }

  const pathOnly = trimmed.split(/[?#]/, 1)[0] ?? '';
  return allowedPrefixes.some((prefix) => {
    const normalizedPrefix = prefix.endsWith('/') && prefix.length > 1 ?
      prefix.slice(0, -1) :
      prefix;
    if (normalizedPrefix === '/') {
      return pathOnly.startsWith('/');
    }
    return pathOnly === normalizedPrefix || pathOnly.startsWith(`${normalizedPrefix}/`);
  });
}

/**
 * `isTrustedChzzkAuthorizationUrl`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isTrustedChzzkAuthorizationUrl(value: string): boolean {
  try {
    const url = new URL(value);
    return url.protocol === 'https:' &&
      url.hostname === TRUSTED_CHZZK_AUTH_HOST &&
      url.pathname === TRUSTED_CHZZK_AUTH_PATH &&
      !url.username &&
      !url.password;
  } catch {
    return false;
  }
}

/**
 * `isValidDiscordWebhookUrl`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isValidDiscordWebhookUrl(value: string): boolean {
  try {
    const url = new URL(value.trim());
    return url.protocol === 'https:' &&
      (url.hostname === 'discord.com' || url.hostname === 'discordapp.com') &&
      !url.username &&
      !url.password &&
      !url.search &&
      !url.hash &&
      DISCORD_WEBHOOK_PATH.test(url.pathname);
  } catch {
    return false;
  }
}

/**
 * `safeImageSrc`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function safeImageSrc(value?: string | null): string {
  if (!value) {
    return '';
  }
  const trimmed = value.trim();
  if (!trimmed) {
    return '';
  }
  if (trimmed.startsWith('/') && !trimmed.startsWith('//') && isSafeInternalPath(trimmed)) {
    return trimmed;
  }

  try {
    const url = new URL(trimmed, currentOrigin());
    if (SAFE_IMAGE_SCHEMES.has(url.protocol)) {
      return url.toString();
    }
    if (url.protocol === 'http:' && SAFE_LOOPBACK_HOSTS.has(url.hostname)) {
      return url.toString();
    }
    return '';
  } catch {
    return '';
  }
}

/**
 * `hasAllowedAvatarExtension`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function hasAllowedAvatarExtension(fileName: string): boolean {
  const normalizedName = fileName.trim().toLowerCase();
  return ALLOWED_AVATAR_EXTENSIONS.some((extension) => normalizedName.endsWith(extension));
}
