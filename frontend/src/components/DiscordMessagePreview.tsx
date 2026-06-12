import {useTranslation} from 'react-i18next';
import {getLanguageLabel, getSubscriptionTypeLabel, shouldShowStreamOnlineDetails} from '../pages/app/subscriptionFormOptions';

/**
 * `DiscordMessagePreviewProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type DiscordMessagePreviewProps = {
  subscriptionType: string;
  channelName: string;
  webhookName: string;
  botProfileName: string;
  botUsername: string;
  colorHex: string;
  language: string;
  enabled: boolean;
  content: string;
  showDetail: boolean;
  showThumbnail: boolean;
  showViewerCount: boolean;
  showTag: boolean;
};

/**
 * `normalizedColor`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function normalizedColor(value: string): string {
  return /^#[0-9a-fA-F]{6}$/.test(value) ? value : '#000000';
}

/**
 * `DiscordMessagePreview`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function DiscordMessagePreview({
  subscriptionType,
  channelName,
  webhookName,
  botProfileName,
  botUsername,
  colorHex,
  language,
  enabled,
  content,
  showDetail,
  showThumbnail,
  showViewerCount,
  showTag,
}: DiscordMessagePreviewProps) {
  const {t} = useTranslation();
  const showsStreamDetails = shouldShowStreamOnlineDetails(subscriptionType);
  const displayBotName = botUsername || botProfileName || t('discordPreview.defaultBotName');
  const displayWebhook = webhookName || t('discordPreview.defaultWebhook');
  const displayChannel = channelName || t('discordPreview.defaultChannel');
  const displayLanguage = getLanguageLabel(language, t);
  const previewColor = normalizedColor(colorHex);

  return (
    <section className="discord-preview" aria-label={t('discordPreview.ariaLabel')}>
      <div className="d-flex justify-content-between align-items-center gap-2 mb-3">
        <div>
          <h2 className="h6 mb-1">{t('discordPreview.title')}</h2>
          <p className="small text-secondary mb-0">{displayWebhook}</p>
        </div>
        <span className={`badge ${enabled ? 'text-bg-success' : 'text-bg-secondary'}`}>{enabled ? t('common.enabled') : t('common.paused')}</span>
      </div>
      <div className="discord-message">
        <div className="discord-message-avatar" aria-hidden="true">
          <i className="bi bi-robot" />
        </div>
        <div className="min-w-0 flex-grow-1">
          <div className="d-flex flex-wrap align-items-center gap-2 mb-1">
            <strong>{displayBotName}</strong>
            <span className="badge text-bg-primary">{t('discordPreview.botBadge')}</span>
          </div>
          {content.trim() ? (
            <p className="discord-message-content">{content}</p>
          ) : (
            <p className="discord-message-content text-secondary">{t('discordPreview.optionalContent')}</p>
          )}
          <div className="discord-embed" style={{borderLeftColor: previewColor}}>
            <div className="fw-semibold">{getSubscriptionTypeLabel(subscriptionType, t)}</div>
            <div className="small text-secondary mb-2">
              {t('discordPreview.alertLanguage', {channel: displayChannel, language: displayLanguage})}
            </div>
            {showsStreamDetails && showDetail ? <div className="small">{t('discordPreview.streamDetailsIncluded')}</div> : null}
            {showsStreamDetails && showViewerCount ? <div className="small">{t('discordPreview.viewerCountIncluded')}</div> : null}
            {showsStreamDetails && showTag ? <div className="small">{t('discordPreview.tagsIncluded')}</div> : null}
            {showsStreamDetails && showThumbnail ? <div className="discord-thumbnail mt-2">{t('discordPreview.thumbnail')}</div> : null}
            {!showsStreamDetails ? <div className="small text-secondary">{t('discordPreview.relevantFieldsOnly')}</div> : null}
          </div>
        </div>
      </div>
    </section>
  );
}
