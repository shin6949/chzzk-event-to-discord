/**
 * `SubscriptionType`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export type SubscriptionType = 'STREAM_ONLINE' | 'STREAM_OFFLINE' | 'CHANNEL_UPDATE' | 'STREAM_ONLINE_AND_OFFLINE';

/**
 * `Translate`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type Translate = (key: string, options?: Record<string, unknown>) => string;

export const subscriptionTypeOptions: { value: SubscriptionType; labelKey: string }[] = [
  {value: 'STREAM_ONLINE', labelKey: 'subscriptionTypes.streamOnline'},
  {value: 'STREAM_OFFLINE', labelKey: 'subscriptionTypes.streamOffline'},
  {value: 'CHANNEL_UPDATE', labelKey: 'subscriptionTypes.channelUpdate'},
  {value: 'STREAM_ONLINE_AND_OFFLINE', labelKey: 'subscriptionTypes.streamOnlineAndOffline'},
];

export const languageOptions = [
  {value: 'Korean', labelKey: 'languages.korean'},
  {value: 'English', labelKey: 'languages.english'},
  {value: 'Japanese', labelKey: 'languages.japanese'},
  {value: 'Chinese', labelKey: 'languages.chinese'},
  {value: 'Spanish', labelKey: 'languages.spanish'},
  {value: 'German', labelKey: 'languages.german'},
  {value: 'Portuguese', labelKey: 'languages.portuguese'},
  {value: 'French', labelKey: 'languages.french'},
  {value: 'Other', labelKey: 'languages.other'},
];

const DEFAULT_COLOR_INPUT_VALUE = '#00FFA3';
const HEX_COLOR_PATTERN = /^[0-9a-fA-F]{6}$/;

/**
 * `toColorInputValue`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function toColorInputValue(value?: string | null): string {
  const normalized = value?.trim().replace(/^#/, '') ?? '';
  return HEX_COLOR_PATTERN.test(normalized) ? `#${normalized}` : DEFAULT_COLOR_INPUT_VALUE;
}

/**
 * `toApiColorHex`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function toApiColorHex(value: string): string {
  const normalized = value.trim().replace(/^#/, '');
  return HEX_COLOR_PATTERN.test(normalized) ? normalized : DEFAULT_COLOR_INPUT_VALUE.slice(1);
}

/**
 * `getSubscriptionTypeLabel`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function getSubscriptionTypeLabel(value: string, t: Translate): string {
  const option = subscriptionTypeOptions.find((candidate) => candidate.value === value);
  return option ? t(option.labelKey) : value;
}

/**
 * `getLanguageLabel`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function getLanguageLabel(value: string, t: Translate): string {
  const option = languageOptions.find((candidate) => candidate.value === value);
  return option ? t(option.labelKey) : value;
}

/**
 * `shouldShowStreamOnlineDetails`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function shouldShowStreamOnlineDetails(value: string): boolean {
  return value === 'STREAM_ONLINE' || value === 'STREAM_ONLINE_AND_OFFLINE';
}
