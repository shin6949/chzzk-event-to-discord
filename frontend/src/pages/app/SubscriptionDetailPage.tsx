import type {FormEvent} from 'react';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {ApiError, apiDelete, apiGet, apiPut} from '../../api/client';
import {ConfirmDeleteModal} from '../../components/ConfirmDeleteModal';
import {DiscordMessagePreview} from '../../components/DiscordMessagePreview';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import type {SubscriptionType} from './subscriptionFormOptions';
import {languageOptions, shouldShowStreamOnlineDetails, subscriptionTypeOptions, toApiColorHex, toColorInputValue} from './subscriptionFormOptions';

/**
 * `SubscriptionPayload`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type SubscriptionPayload = {
  channelId: string;
  subscriptionType: SubscriptionType;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  language: string;
  colorHex: string;
  enabled: boolean;
  content: string;
  showDetail?: boolean | null;
  showThumbnail?: boolean | null;
  showViewerCount?: boolean | null;
  showTag?: boolean | null;
};

/**
 * `FormState`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type FormState = {
  channelId: string;
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
};

const emptyForm: FormState = {
  channelId: '',
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

/**
 * `SubscriptionDetailPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function SubscriptionDetailPage() {
  const {t} = useTranslation();
  const {id} = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [webhooks, setWebhooks] = useState<WebhookOption[]>([]);
  const [botProfiles, setBotProfiles] = useState<BotProfileOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const canSubmit = useMemo(() => {
    return Boolean(formState.channelId.trim() && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState]);

  const loadSubscription = useCallback(async () => {
    if (!id) {
      setLoadError(t('subscriptions.missingId'));
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      setLoadError('');
      setError('');
      const subscriptionResponse = await apiGet<SubscriptionPayload>(`/subscriptions/${id}`);
      setFormState({
        channelId: subscriptionResponse.channelId,
        subscriptionType: subscriptionResponse.subscriptionType as SubscriptionType,
        webhookId: String(subscriptionResponse.webhookId),
        botProfileId: String(subscriptionResponse.botProfileId),
        intervalMinute: String(subscriptionResponse.intervalMinute ?? 10),
        language: subscriptionResponse.language ?? 'Korean',
        colorHex: toColorInputValue(subscriptionResponse.colorHex),
        enabled: subscriptionResponse.enabled,
        content: subscriptionResponse.content ?? '',
        showDetail: Boolean(subscriptionResponse.showDetail),
        showThumbnail: subscriptionResponse.showThumbnail ?? true,
        showViewerCount: Boolean(subscriptionResponse.showViewerCount),
        showTag: subscriptionResponse.showTag ?? true,
      });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.loadError');
      setLoadError(message);
      setLoading(false);
      return;
    }

    try {
      const [webhookResponse, botProfileResponse] = await Promise.all([
        apiGet<ResourcePage<WebhookOption>>('/discord/webhooks?size=100&sort=id,desc'),
        apiGet<ResourcePage<BotProfileOption>>('/discord/bot-profiles?size=100&sort=id,desc'),
      ]);
      setWebhooks(webhookResponse.content ?? []);
      setBotProfiles(botProfileResponse.content ?? []);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.unableToLoadResources');
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [id, t]);

  useEffect(() => {
    void loadSubscription();
  }, [loadSubscription]);

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
    if (!id || !canSubmit) {
      return;
    }

    setSaving(true);
    setError('');
    const webhookId = Number(formState.webhookId);
    const botProfileId = Number(formState.botProfileId);
    const intervalMinute = Number(formState.intervalMinute);

    if (!Number.isFinite(webhookId) || !Number.isFinite(botProfileId) || !Number.isFinite(intervalMinute)) {
      setError(t('subscriptions.invalidResources'));
      setSaving(false);
      return;
    }

    const streamDetailsEnabled = shouldShowStreamOnlineDetails(formState.subscriptionType);
    const payload: SubscriptionPayload = {
      channelId: formState.channelId.trim(),
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
      await apiPut(`/subscriptions/${id}`, payload);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.updateError');
      setError(message);
      setSaving(false);
    }
  }

  /**
   * `confirmDelete`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  async function confirmDelete() {
    if (!id) {
      return;
    }

    setDeleting(true);
    try {
      await apiDelete(`/subscriptions/${id}`);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.deleteError');
      setError(message);
      setDeleting(false);
    }
  }

  if (loading) {
    return <div className="py-5 text-center">{t('common.loading')}</div>;
  }

  if (loadError) {
    return (
      <PagePlaceholder title={t('subscriptions.unavailableTitle', {id: id ?? ''})} description={t('subscriptions.unavailableDescription')}>
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mb-3">
          <span>{loadError}</span>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadSubscription()}>
            {t('common.retry')}
          </button>
        </div>
        <dl className="row mb-4">
          <dt className="col-sm-3">{t('subscriptions.idLabel')}</dt>
          <dd className="col-sm-9">{id ?? t('common.unknown')}</dd>
        </dl>
        <Link to="/subscriptions" className="btn btn-primary">
          <i className="bi bi-arrow-left me-1" aria-hidden="true" />
          {t('common.backToSubscriptions')}
        </Link>
      </PagePlaceholder>
    );
  }

  const selectedWebhookIsMissing = formState.webhookId &&
    !webhooks.some((webhook) => String(webhook.id) === formState.webhookId);
  const selectedBotProfileIsMissing = formState.botProfileId &&
    !botProfiles.some((botProfile) => String(botProfile.id) === formState.botProfileId);
  const selectedWebhook = webhooks.find((webhook) => String(webhook.id) === formState.webhookId);
  const selectedBotProfile = botProfiles.find((botProfile) => String(botProfile.id) === formState.botProfileId);
  const showsStreamDetails = shouldShowStreamOnlineDetails(formState.subscriptionType);

  return (
    <PagePlaceholder
      title={t('subscriptions.editTitle', {id: id ?? ''})}
      description={t('subscriptions.editDescription')}
      actions={
        <Link to="/subscriptions" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1" aria-hidden="true" />
          {t('common.backToSubscriptions')}
        </Link>
      }
    >
      {error ? (
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
          <span>{error}</span>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadSubscription()}>
            {t('common.retry')}
          </button>
        </div>
      ) : null}
      <form className="row g-3 mt-1" onSubmit={handleSubmit}>
        <div className="col-12">
          <label htmlFor="channelId" className="form-label">
            {t('subscriptions.targetChannelId')}
          </label>
          <input
            id="channelId"
            className="form-control"
            value={formState.channelId}
            onChange={(event) => updateField('channelId', event.target.value)}
            maxLength={100}
            required
          />
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
            required
          >
            <option value="">{t('subscriptions.chooseWebhook')}</option>
            {selectedWebhookIsMissing ? <option value={formState.webhookId}>{t('resources.webhookFallback', {id: formState.webhookId})}</option> : null}
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
            required
          >
            <option value="">{t('subscriptions.chooseBotProfile')}</option>
            {selectedBotProfileIsMissing ? <option value={formState.botProfileId}>{t('resources.botProfileFallback', {id: formState.botProfileId})}</option> : null}
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
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            {saving ? t('common.saving') : t('subscriptions.saveChanges')}
          </button>
          <button type="button" className="btn btn-outline-danger" onClick={() => setDeleteModalOpen(true)}>
            {t('common.delete')}
          </button>
        </div>
      </form>
      <div className="mt-4">
        <DiscordMessagePreview
          subscriptionType={formState.subscriptionType}
          channelName={formState.channelId}
          webhookName={selectedWebhook?.alias ?? (formState.webhookId ? t('resources.webhookFallback', {id: formState.webhookId}) : '')}
          botProfileName={selectedBotProfile?.alias ?? (formState.botProfileId ? t('resources.botProfileFallback', {id: formState.botProfileId}) : '')}
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
      <ConfirmDeleteModal
        open={deleteModalOpen}
        title={t('subscriptions.deleteModal.title')}
        message={t('subscriptions.deleteModal.detailMessage', {id: id ?? ''})}
        busy={deleting}
        onCancel={() => setDeleteModalOpen(false)}
        onConfirm={() => void confirmDelete()}
      />
    </PagePlaceholder>
  );
}
