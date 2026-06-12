import type {FormEvent} from 'react';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Link, useNavigate} from 'react-router-dom';
import {ApiError, apiGet, apiPost} from '../../api/client';
import {useAuth} from '../../auth/AuthContext';
import {DiscordMessagePreview} from '../../components/DiscordMessagePreview';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import {SetupChecklist} from '../../components/SetupChecklist';
import {safeImageSrc} from '../../security/urls';
import type {SubscriptionType} from './subscriptionFormOptions';
import {languageOptions, shouldShowStreamOnlineDetails, subscriptionTypeOptions, toApiColorHex, toColorInputValue} from './subscriptionFormOptions';

/**
 * `FormState`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type FormState = {
  subscriptionType: SubscriptionType;
  webhookId: string;
  botProfileId: string;
  intervalMinute: string;
  language: string;
  colorHex: string;
  enabled: boolean;
  content: string;
  showDetail: boolean;
  showThumbnail: boolean;
  showViewerCount: boolean;
  showTag: boolean;
};

/**
 * `ApiPayload`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type ApiPayload = {
  channelId: string;
  subscriptionType: SubscriptionType;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  language: string;
  colorHex: string;
  enabled: boolean;
  content: string;
  showDetail: boolean;
  showThumbnail: boolean;
  showViewerCount: boolean;
  showTag: boolean;
};

/**
 * `ResourcePage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type ResourcePage<T> = {
  content: T[];
};

/**
 * `WebhookOption`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type WebhookOption = {
  id: number;
  alias: string;
};

/**
 * `BotProfileOption`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type BotProfileOption = {
  id: number;
  alias: string;
  username: string;
  avatarUrl: string;
};

/**
 * `createInitialState`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function createInitialState(): FormState {
  return {
    subscriptionType: 'STREAM_ONLINE',
    webhookId: '',
    botProfileId: '',
    intervalMinute: '10',
    language: 'Korean',
    colorHex: toColorInputValue(),
    enabled: true,
    content: '',
    showDetail: false,
    showThumbnail: true,
    showViewerCount: false,
    showTag: true,
  };
}

/**
 * `NewSubscriptionPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function NewSubscriptionPage() {
  const {t} = useTranslation();
  const navigate = useNavigate();
  const {session, loading: sessionLoading} = useAuth();
  const [formState, setFormState] = useState<FormState>(createInitialState);
  const [webhooks, setWebhooks] = useState<WebhookOption[]>([]);
  const [botProfiles, setBotProfiles] = useState<BotProfileOption[]>([]);
  const [resourcesLoading, setResourcesLoading] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const sessionChannelId = session?.channelId?.trim() ?? '';
  const sessionChannelName = session?.channelName?.trim() ?? '';
  const channelDisplayName = sessionLoading ? t('subscriptions.loadingChannel') : sessionChannelName || t('subscriptions.signedInChannel');
  const safeProfileUrl = safeImageSrc(session?.profileUrl);

  const loadResources = useCallback(async () => {
    setResourcesLoading(true);
    setError('');

    try {
      const [webhookResponse, botProfileResponse] = await Promise.all([
        apiGet<ResourcePage<WebhookOption>>('/discord/webhooks?size=100&sort=id,desc'),
        apiGet<ResourcePage<BotProfileOption>>('/discord/bot-profiles?size=100&sort=id,desc'),
      ]);
      const nextWebhooks = webhookResponse.content ?? [];
      const nextBotProfiles = botProfileResponse.content ?? [];
      setWebhooks(nextWebhooks);
      setBotProfiles(nextBotProfiles);
      setFormState((current) => ({
        ...current,
        webhookId: current.webhookId || (nextWebhooks.length === 1 ? String(nextWebhooks[0].id) : ''),
        botProfileId: current.botProfileId || (nextBotProfiles.length === 1 ? String(nextBotProfiles[0].id) : ''),
      }));
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.unableToLoadResources');
      setError(message);
      setWebhooks([]);
      setBotProfiles([]);
    } finally {
      setResourcesLoading(false);
    }
  }, [t]);

  useEffect(() => {
    void loadResources();
  }, [loadResources]);

  const formIsValid = useMemo(() => {
    return Boolean(sessionChannelId && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState.botProfileId, formState.webhookId, sessionChannelId]);

  /**
   * `updateField`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  function updateField(name: keyof FormState, value: string | boolean) {
    setFormState((current) => ({...current, [name]: value}));
  }

  /**
   * `handleSubmit`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError('');

    const webhookId = Number(formState.webhookId);
    const botProfileId = Number(formState.botProfileId);
    const intervalMinute = Number(formState.intervalMinute);

    if (!sessionChannelId) {
      setError(t('subscriptions.unableToResolveChannel'));
      setLoading(false);
      return;
    }

    if (
      !formIsValid ||
      !Number.isFinite(webhookId) ||
      !Number.isFinite(botProfileId) ||
      !Number.isFinite(intervalMinute)
    ) {
      setError(t('subscriptions.invalidResources'));
      setLoading(false);
      return;
    }

    const streamDetailsEnabled = shouldShowStreamOnlineDetails(formState.subscriptionType);
    const payload: ApiPayload = {
      channelId: sessionChannelId,
      subscriptionType: formState.subscriptionType,
      webhookId,
      botProfileId,
      intervalMinute,
      language: formState.language,
      colorHex: toApiColorHex(formState.colorHex),
      enabled: formState.enabled,
      content: formState.content,
      showDetail: streamDetailsEnabled ? formState.showDetail : false,
      showThumbnail: streamDetailsEnabled ? formState.showThumbnail : false,
      showViewerCount: streamDetailsEnabled ? formState.showViewerCount : false,
      showTag: streamDetailsEnabled ? formState.showTag : false,
    };

    try {
      await apiPost('/subscriptions', payload);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.createError');
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  const missingResources = !resourcesLoading && (webhooks.length === 0 || botProfiles.length === 0);
  const selectedWebhook = webhooks.find((webhook) => String(webhook.id) === formState.webhookId);
  const selectedBotProfile = botProfiles.find((botProfile) => String(botProfile.id) === formState.botProfileId);
  const showsStreamDetails = shouldShowStreamOnlineDetails(formState.subscriptionType);

  return (
    <PagePlaceholder
      title={t('subscriptions.newTitle')}
      description={t('subscriptions.newDescription')}
      actions={
        <Link to="/subscriptions" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1" aria-hidden="true" />
          {t('common.backToSubscriptions')}
        </Link>
      }
    >
      <SetupChecklist
        currentStep="subscription"
        completed={{webhook: webhooks.length > 0, botProfile: botProfiles.length > 0}}
        returnTo="/subscriptions/new"
      />
      {error ? (
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{error}</span>
          <div className="d-flex flex-wrap gap-2">
            <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadResources()}>
              {t('common.retryResources')}
            </button>
            <Link to="/login" className="btn btn-sm btn-outline-dark">
              {t('tutorial.goToLogin')}
            </Link>
          </div>
        </div>
      ) : null}
      {missingResources ? (
        <div className="alert alert-warning mt-3">
          <div className="d-flex flex-column flex-lg-row align-items-lg-center justify-content-lg-between gap-3">
            <div>
              <div className="fw-semibold">
                <i className="bi bi-exclamation-triangle me-2" aria-hidden="true" />
                {t('subscriptions.missingResourcesTitle')}
              </div>
              <div className="small">{t('subscriptions.missingResourcesBody')}</div>
            </div>
            <div className="d-flex flex-wrap gap-2">
              {webhooks.length === 0 ? (
                <Link to="/discord/webhooks?returnTo=/subscriptions/new" className="btn btn-sm btn-outline-dark">
                  {t('subscriptions.createWebhookStep')}
                </Link>
              ) : null}
              {botProfiles.length === 0 ? (
                <Link to="/discord/bot-profiles?returnTo=/subscriptions/new" className="btn btn-sm btn-outline-dark">
                  {t('subscriptions.createBotProfileStep')}
                </Link>
              ) : null}
            </div>
          </div>
        </div>
      ) : null}
      <div className="row g-4 align-items-start mt-1">
        <div className="col-12 col-xl-7">
          <form className="row g-3" onSubmit={handleSubmit}>
            <div className="col-12">
              <div className="form-label">{t('subscriptions.channel')}</div>
              <div className="subscription-channel-summary d-flex align-items-center gap-2">
                <span className="channel-avatar subscription-channel-avatar" aria-hidden="true">
                  <i className="bi bi-person-fill" aria-hidden="true" />
                  {safeProfileUrl ? (
                    <img
                      src={safeProfileUrl}
                      alt=""
                      referrerPolicy="no-referrer"
                      onError={(event) => {
                        event.currentTarget.style.display = 'none';
                      }}
                    />
                  ) : null}
                </span>
                <span className="subscription-channel-name fw-semibold">{channelDisplayName}</span>
              </div>
            </div>
            <div className="col-12">
              <label htmlFor="subscriptionType" className="form-label">
                {t('subscriptions.subscriptionType')}
              </label>
              <select
                id="subscriptionType"
                className="form-select"
                value={formState.subscriptionType}
                onChange={(event) => updateField('subscriptionType', event.target.value as SubscriptionType)}
                required
              >
                {subscriptionTypeOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {t(option.labelKey)}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-6">
              <label htmlFor="webhookId" className="form-label">
                {t('subscriptions.webhook')}
              </label>
              <select
                id="webhookId"
                className="form-select"
                value={formState.webhookId}
                onChange={(event) => updateField('webhookId', event.target.value)}
                disabled={resourcesLoading}
                required
              >
                <option value="">{resourcesLoading ? t('subscriptions.loadingWebhooks') : t('subscriptions.chooseWebhook')}</option>
                {webhooks.map((webhook) => (
                  <option key={webhook.id} value={webhook.id}>
                    {webhook.alias}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-6">
              <label htmlFor="botProfileId" className="form-label">
                {t('subscriptions.botProfile')}
              </label>
              <select
                id="botProfileId"
                className="form-select"
                value={formState.botProfileId}
                onChange={(event) => updateField('botProfileId', event.target.value)}
                disabled={resourcesLoading}
                required
              >
                <option value="">{resourcesLoading ? t('subscriptions.loadingBotProfiles') : t('subscriptions.chooseBotProfile')}</option>
                {botProfiles.map((botProfile) => (
                  <option key={botProfile.id} value={botProfile.id}>
                    {botProfile.alias} ({botProfile.username})
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label htmlFor="intervalMinute" className="form-label">
                {t('subscriptions.intervalMinutes')}
              </label>
              <input
                id="intervalMinute"
                className="form-control"
                type="number"
                inputMode="numeric"
                min={1}
                max={1440}
                value={formState.intervalMinute}
                onChange={(event) => updateField('intervalMinute', event.target.value)}
                required
              />
            </div>
            <div className="col-md-4">
              <label htmlFor="language" className="form-label">
                {t('subscriptions.language')}
              </label>
              <select
                id="language"
                className="form-select"
                value={formState.language}
                onChange={(event) => updateField('language', event.target.value)}
                required
              >
                {languageOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {t(option.labelKey)}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label htmlFor="colorHex" className="form-label">
                {t('subscriptions.embedColor')}
              </label>
              <input
                id="colorHex"
                className="form-control form-control-color"
                type="color"
                value={formState.colorHex}
                onChange={(event) => updateField('colorHex', event.target.value)}
                title={t('subscriptions.chooseEmbedColor')}
              />
            </div>
            <div className="col-md-4 d-flex align-items-end">
              <div className="form-check">
                <input
                  id="enabled"
                  className="form-check-input"
                  type="checkbox"
                  checked={formState.enabled}
                  onChange={(event) => updateField('enabled', event.target.checked)}
                />
                <label className="form-check-label" htmlFor="enabled">
                  {t('common.enabled')}
                </label>
              </div>
            </div>
            {showsStreamDetails ? (
              <div className="col-12">
                <fieldset className="border rounded-3 p-3">
                  <legend className="float-none w-auto px-1 h6">{t('subscriptions.streamOnlineDetails')}</legend>
                  <div className="row g-2">
                    <div className="col-sm-6 col-lg-3">
                      <div className="form-check">
                        <input
                          id="showDetail"
                          className="form-check-input"
                          type="checkbox"
                          checked={formState.showDetail}
                          onChange={(event) => updateField('showDetail', event.target.checked)}
                        />
                        <label className="form-check-label" htmlFor="showDetail">
                          {t('subscriptions.showDetails')}
                        </label>
                      </div>
                    </div>
                    <div className="col-sm-6 col-lg-3">
                      <div className="form-check">
                        <input
                          id="showThumbnail"
                          className="form-check-input"
                          type="checkbox"
                          checked={formState.showThumbnail}
                          onChange={(event) => updateField('showThumbnail', event.target.checked)}
                        />
                        <label className="form-check-label" htmlFor="showThumbnail">
                          {t('subscriptions.showThumbnail')}
                        </label>
                      </div>
                    </div>
                    <div className="col-sm-6 col-lg-3">
                      <div className="form-check">
                        <input
                          id="showViewerCount"
                          className="form-check-input"
                          type="checkbox"
                          checked={formState.showViewerCount}
                          onChange={(event) => updateField('showViewerCount', event.target.checked)}
                        />
                        <label className="form-check-label" htmlFor="showViewerCount">
                          {t('subscriptions.showViewerCount')}
                        </label>
                      </div>
                    </div>
                    <div className="col-sm-6 col-lg-3">
                      <div className="form-check">
                        <input
                          id="showTag"
                          className="form-check-input"
                          type="checkbox"
                          checked={formState.showTag}
                          onChange={(event) => updateField('showTag', event.target.checked)}
                        />
                        <label className="form-check-label" htmlFor="showTag">
                          {t('subscriptions.showTags')}
                        </label>
                      </div>
                    </div>
                  </div>
                </fieldset>
              </div>
            ) : null}
            <div className="col-12">
              <label htmlFor="content" className="form-label">
                {t('subscriptions.content')}
              </label>
              <textarea
                id="content"
                className="form-control"
                value={formState.content}
                onChange={(event) => updateField('content', event.target.value)}
                maxLength={2000}
                rows={3}
              />
            </div>
            <div className="col-12">
              <button type="submit" className="btn btn-primary" disabled={loading || sessionLoading || resourcesLoading || !formIsValid}>
                {loading ? t('subscriptions.creating') : t('subscriptions.create')}
              </button>
            </div>
          </form>
        </div>
        <div className="col-12 col-xl-5">
          <div className="sticky-xl-top subscription-preview-sticky">
            <DiscordMessagePreview
              subscriptionType={formState.subscriptionType}
              channelName={channelDisplayName}
              webhookName={selectedWebhook?.alias ?? ''}
              botProfileName={selectedBotProfile?.alias ?? ''}
              botUsername={selectedBotProfile?.username ?? ''}
              colorHex={formState.colorHex}
              language={formState.language}
              enabled={formState.enabled}
              content={formState.content}
              showDetail={formState.showDetail}
              showThumbnail={formState.showThumbnail}
              showViewerCount={formState.showViewerCount}
              showTag={formState.showTag}
            />
          </div>
        </div>
      </div>
    </PagePlaceholder>
  );
}
