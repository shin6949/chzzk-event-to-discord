import type {FormEvent} from 'react';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Link, useSearchParams} from 'react-router-dom';
import {ApiError, apiDelete, apiGet, apiPost, apiPut} from '../../api/client';
import {ConfirmDeleteModal} from '../../components/ConfirmDeleteModal';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import {SetupChecklist} from '../../components/SetupChecklist';
import {isSafeInternalPath, isValidDiscordWebhookUrl} from '../../security/urls';

/**
 * `WebhookItem`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type WebhookItem = {
  id: number;
  alias: string;
  maskedUrl: string;
  hasUrl: boolean;
  ownerChannelId: string;
};

/**
 * `WebhookPage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type WebhookPage = {
  content: WebhookItem[];
  totalElements: number;
};

/**
 * `FormState`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type FormState = {
  id: number | null;
  alias: string;
  url: string;
};

const emptyForm: FormState = {
  id: null,
  alias: '',
  url: '',
};

/**
 * `safeReturnTo`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function safeReturnTo(value: string | null): string {
  if (!value || !isSafeInternalPath(value, ['/subscriptions'])) {
    return '';
  }
  return value.trim();
}

/**
 * `WebhooksPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
export function WebhooksPage() {
  const {t} = useTranslation();
  const [searchParams] = useSearchParams();
  const [webhooks, setWebhooks] = useState<WebhookItem[]>([]);
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const [savedMessage, setSavedMessage] = useState('');
  const [pendingDelete, setPendingDelete] = useState<WebhookItem | null>(null);
  const returnTo = safeReturnTo(searchParams.get('returnTo'));

  const nextUrl = formState.url.trim();
  const urlRequired = formState.id === null;
  const urlIsValid = !nextUrl && !urlRequired ? true : isValidDiscordWebhookUrl(nextUrl);
  const urlInvalid = Boolean(nextUrl) && !urlIsValid;
  const canSubmit = useMemo(
    () => Boolean(formState.alias.trim() && urlIsValid && (formState.id !== null || nextUrl)),
    [formState.alias, formState.id, nextUrl, urlIsValid],
  );

  const loadWebhooks = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await apiGet<WebhookPage>('/discord/webhooks?size=100&sort=id,desc');
      setWebhooks(response.content ?? []);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('webhooks.loadError');
      setError(message);
      setWebhooks([]);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    void loadWebhooks();
  }, [loadWebhooks]);

  /**
   * `updateField`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function updateField(name: keyof Pick<FormState, 'alias' | 'url'>, value: string) {
    setSavedMessage('');
    setFormState((current) => ({...current, [name]: value}));
  }

  /**
   * `startEdit`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function startEdit(webhook: WebhookItem) {
    setSavedMessage('');
    setFormState({id: webhook.id, alias: webhook.alias, url: ''});
  }

  /**
   * `resetForm`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function resetForm() {
    setSavedMessage('');
    setFormState(emptyForm);
  }

  /**
   * `handleSubmit`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    setSaving(true);
    setError('');

    if (!urlIsValid) {
      setError(t('webhooks.invalidUrl'));
      return;
    }

    const payload = {
      alias: formState.alias.trim(),
      ...(nextUrl ? {url: nextUrl} : {}),
    };
    const wasCreate = formState.id === null;

    try {
      if (wasCreate) {
        await apiPost('/discord/webhooks', payload);
      } else {
        await apiPut(`/discord/webhooks/${formState.id}`, payload);
      }
      resetForm();
      await loadWebhooks();
      setSavedMessage(wasCreate ? t('webhooks.added', {alias: payload.alias}) : t('webhooks.saved', {alias: payload.alias}));
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('webhooks.saveError');
      setError(message);
    } finally {
      setSaving(false);
    }
  }

  /**
   * `confirmDelete`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  async function confirmDelete() {
    if (!pendingDelete) {
      return;
    }

    setDeleting(true);
    try {
      await apiDelete(`/discord/webhooks/${pendingDelete.id}`);
      await loadWebhooks();
      if (formState.id === pendingDelete.id) {
        resetForm();
      }
      setPendingDelete(null);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('webhooks.deleteError');
      setError(message);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <PagePlaceholder
      title={t('webhooks.title')}
      description={t('webhooks.description')}
      actions={
        <Link to={returnTo || '/discord/bot-profiles'} className="btn btn-outline-primary">
          {returnTo ? t('common.continueSetup') : t('webhooks.botProfiles')}
        </Link>
      }
    >
      <SetupChecklist currentStep="webhook" completed={{webhook: webhooks.length > 0}} returnTo={returnTo || '/subscriptions/new'} />
      {savedMessage ? (
        <div className="alert alert-success d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{savedMessage}</span>
          {returnTo ? (
            <Link to={returnTo} className="btn btn-sm btn-success">
              {t('webhooks.continueToSubscription')}
            </Link>
          ) : (
            <Link to="/discord/bot-profiles" className="btn btn-sm btn-outline-success">
              {t('webhooks.nextBotProfiles')}
            </Link>
          )}
        </div>
      ) : null}
      {error ? (
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{error}</span>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadWebhooks()}>
            {t('common.retry')}
          </button>
        </div>
      ) : null}
      <form className="row g-3 mb-4" onSubmit={handleSubmit}>
        <div className="col-12">
          <h2 className="h6 mb-0">{formState.id === null ? t('webhooks.addTitle') : t('webhooks.editTitle')}</h2>
        </div>
        <div className="col-md-4">
          <label htmlFor="webhookAlias" className="form-label">
            {t('webhooks.alias')}
          </label>
          <input
            id="webhookAlias"
            className="form-control"
            value={formState.alias}
            onChange={(event) => updateField('alias', event.target.value)}
            placeholder={t('webhooks.aliasPlaceholder')}
            maxLength={100}
            required
          />
        </div>
        <div className="col-md-8">
          <label htmlFor="webhookUrl" className="form-label">
            {t('webhooks.url')}
          </label>
          <div className="input-group">
            <span className="input-group-text">
              <i className="bi bi-link-45deg" aria-hidden="true" />
            </span>
            <input
              id="webhookUrl"
              className="form-control"
              value={formState.url}
              onChange={(event) => updateField('url', event.target.value)}
              placeholder={formState.id === null ? t('webhooks.createUrlPlaceholder') : t('webhooks.editUrlPlaceholder')}
              type="url"
              maxLength={2000}
              required={formState.id === null}
              aria-invalid={urlInvalid}
            />
          </div>
          {urlInvalid ? <div className="form-text text-danger">{t('webhooks.invalidUrl')}</div> : null}
        </div>
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            <i className={`bi ${formState.id === null ? 'bi-plus-lg' : 'bi-save'} me-1`} aria-hidden="true" />
            {saving ? t('common.saving') : formState.id === null ? t('webhooks.add') : t('webhooks.save')}
          </button>
          {formState.id !== null ? (
            <button type="button" className="btn btn-outline-secondary" onClick={resetForm}>
              {t('common.cancel')}
            </button>
          ) : null}
        </div>
      </form>

      {loading ? (
        <div className="text-center py-4">{t('webhooks.loading')}</div>
      ) : webhooks.length === 0 ? (
        <div className="empty-state py-4 text-center text-secondary">
          <p className="mb-2">{t('webhooks.empty')}</p>
          <p className="small mb-2">{t('webhooks.emptyBody')}</p>
          <Link to="/discord/bot-profiles" className="btn btn-sm btn-outline-primary">
            {t('webhooks.goToBotProfiles')}
          </Link>
        </div>
      ) : (
        <>
        <div className="table-responsive d-none d-md-block">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>{t('webhooks.table.alias')}</th>
                <th>{t('webhooks.table.url')}</th>
                <th className="text-end">{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {webhooks.map((webhook) => (
                  <tr key={webhook.id}>
                    <td className="fw-semibold">{webhook.alias}</td>
                    <td>
                      <code>{webhook.maskedUrl}</code>
                    </td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(webhook)} aria-label={t('webhooks.editAria', {alias: webhook.alias})}>
                          <i className="bi bi-pencil" aria-hidden="true" />
                        </button>
                        <button type="button" className="btn btn-outline-danger" onClick={() => setPendingDelete(webhook)} aria-label={t('webhooks.deleteAria', {alias: webhook.alias})}>
                          <i className="bi bi-trash" aria-hidden="true" />
                        </button>
                      </div>
                    </td>
                  </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="mobile-resource-list d-md-none">
          {webhooks.map((webhook) => (
              <div className="mobile-resource-item" key={webhook.id}>
                <div className="d-flex justify-content-between gap-3">
                  <div className="min-w-0">
                    <div className="fw-semibold">{webhook.alias}</div>
                    <code className="small">{webhook.maskedUrl}</code>
                  </div>
                  <div className="btn-group btn-group-sm">
                    <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(webhook)} aria-label={t('webhooks.editAria', {alias: webhook.alias})}>
                      <i className="bi bi-pencil" aria-hidden="true" />
                    </button>
                    <button type="button" className="btn btn-outline-danger" onClick={() => setPendingDelete(webhook)} aria-label={t('webhooks.deleteAria', {alias: webhook.alias})}>
                      <i className="bi bi-trash" aria-hidden="true" />
                    </button>
                  </div>
                </div>
              </div>
          ))}
        </div>
        </>
      )}
      <ConfirmDeleteModal
        open={Boolean(pendingDelete)}
        title={t('webhooks.deleteTitle')}
        message={t('webhooks.deleteMessage', {alias: pendingDelete?.alias ?? ''})}
        busy={deleting}
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => void confirmDelete()}
      />
    </PagePlaceholder>
  );
}
