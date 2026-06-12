import {describe, expect, it} from 'vitest';
import {
  hasAllowedAvatarExtension,
  isSafeApiPath,
  isSafeInternalPath,
  isTrustedChzzkAuthorizationUrl,
  isValidDiscordWebhookUrl,
  safeImageSrc,
} from './urls';

function discordWebhookUrl(host: string, path: string) {
  return `https://${host}/api/${path}`;
}

function userInfo() {
  return ['user', 'pass'].join(':');
}

describe('URL security helpers', () => {
  it('allows expected API paths and query strings', () => {
    expect(isSafeApiPath('/auth/me')).toBe(true);
    expect(isSafeApiPath('auth/me')).toBe(true);
    expect(isSafeApiPath('/discord/webhooks?size=100&sort=id,desc')).toBe(true);
  });

  it.each([
    '',
    'https://evil.example/api',
    '//evil.example/api',
    '/../admin',
    '/%2e%2e/admin',
    '/%2Fadmin',
    '/admin#fragment',
    '/admin\\settings',
    '/admin/%00',
    '/api/./v1',
  ])('rejects unsafe API paths: %s', (value) => {
    expect(isSafeApiPath(value)).toBe(false);
  });

  it('allows only trusted CHZZK authorization URLs', () => {
    expect(isTrustedChzzkAuthorizationUrl('https://chzzk.naver.com/account-interlock?state=abc')).toBe(true);
    expect(isTrustedChzzkAuthorizationUrl('https://evil.example/account-interlock?state=abc')).toBe(false);
    expect(isTrustedChzzkAuthorizationUrl('http://chzzk.naver.com/account-interlock?state=abc')).toBe(false);
    expect(isTrustedChzzkAuthorizationUrl(`https://${userInfo()}@chzzk.naver.com/account-interlock`)).toBe(false);
  });

  it('allows only Discord webhook URLs without credentials, query, or fragments', () => {
    expect(isValidDiscordWebhookUrl(discordWebhookUrl('discord.com', 'webhooks/1234567890/abcdef_-.token'))).toBe(true);
    expect(isValidDiscordWebhookUrl(discordWebhookUrl('discordapp.com', 'v10/webhooks/1234567890/abcdef'))).toBe(true);
    expect(isValidDiscordWebhookUrl(`http://discord.com/api/${'webhooks/123/token'}`)).toBe(false);
    expect(isValidDiscordWebhookUrl(discordWebhookUrl('evil.example', 'webhooks/123/token'))).toBe(false);
    expect(isValidDiscordWebhookUrl(`${discordWebhookUrl('discord.com', 'webhooks/123/token')}?wait=true`)).toBe(false);
    expect(isValidDiscordWebhookUrl(`${discordWebhookUrl('discord.com', 'webhooks/123/token')}#secret`)).toBe(false);
    expect(isValidDiscordWebhookUrl(`https://${userInfo()}@discord.com/api/${'webhooks/123/token'}`)).toBe(false);
  });

  it('allows safe internal paths only inside expected route prefixes', () => {
    expect(isSafeInternalPath('/subscriptions/new', ['/subscriptions'])).toBe(true);
    expect(isSafeInternalPath('/subscriptions/new?tab=discord', ['/subscriptions'])).toBe(true);
    expect(isSafeInternalPath('/subscriptions.evil', ['/subscriptions'])).toBe(false);
    expect(isSafeInternalPath('//evil.example/subscriptions', ['/subscriptions'])).toBe(false);
    expect(isSafeInternalPath('/subscriptions/%2e%2e/admin', ['/subscriptions'])).toBe(false);
  });

  it('sanitizes image sources before rendering', () => {
    expect(safeImageSrc('/assets/avatar.png')).toBe('/assets/avatar.png');
    expect(safeImageSrc('https://static.example.test/avatar.png')).toBe('https://static.example.test/avatar.png');
    expect(safeImageSrc('blob:http://localhost/avatar')).toBe('blob:http://localhost/avatar');
    expect(safeImageSrc('http://localhost/avatar.png')).toBe('http://localhost/avatar.png');
    expect(safeImageSrc('http://evil.example/avatar.png')).toBe('');
    expect(safeImageSrc('//evil.example/avatar.png')).toBe('');
    expect(safeImageSrc('javascript:alert(1)')).toBe('');
  });

  it('checks avatar file names in addition to MIME type checks', () => {
    expect(hasAllowedAvatarExtension('avatar.png')).toBe(true);
    expect(hasAllowedAvatarExtension('avatar.JPEG')).toBe(true);
    expect(hasAllowedAvatarExtension('avatar.webp')).toBe(true);
    expect(hasAllowedAvatarExtension('avatar.png.exe')).toBe(false);
  });
});
