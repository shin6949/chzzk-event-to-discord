import type {ChangeEvent, FormEvent} from 'react';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Link, useSearchParams} from 'react-router-dom';
import {ApiError, apiDelete, apiGet, apiPostForm, apiPutForm} from '../../api/client';
import {ConfirmDeleteModal} from '../../components/ConfirmDeleteModal';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import {SetupChecklist} from '../../components/SetupChecklist';
import {hasAllowedAvatarExtension, isSafeInternalPath, safeImageSrc} from '../../security/urls';

/**
 * `BotProfileItem`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type BotProfileItem = {
  id: number;
  alias: string;
  username: string;
  avatarUrl: string;
  ownerChannelId: string;
};

/**
 * `BotProfilePage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type BotProfilePage = {
  content: BotProfileItem[];
  totalElements: number;
};

/**
 * `UploadCapability`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type UploadCapability = {
  enabled: boolean;
  reason?: string | null;
  checkedAt?: string | null;
};

/**
 * `SystemCapabilities`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type SystemCapabilities = {
  uploads?: {
    botProfileAvatar?: UploadCapability;
  };
};

/**
 * `FormState`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
type FormState = {
  id: number | null;
  alias: string;
  username: string;
  avatar: File | null;
  currentAvatarUrl: string;
};

/**
 * `Translate`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
type Translate = (key: string, options?: Record<string, unknown>) => string;

const emptyForm: FormState = {
  id: null,
  alias: '',
  username: '',
  avatar: null,
  currentAvatarUrl: '',
};

const allowedImageTypes = ['image/png', 'image/jpeg', 'image/webp'];
const maxUploadBytes = 5 * 1024 * 1024;
const defaultUploadCapability: UploadCapability = {enabled: true};

/**
 * `fileSizeLabel`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
function fileSizeLabel(bytes: number): string {
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

/**
 * `saveErrorMessage`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
function saveErrorMessage(err: unknown, t: Translate): string {
  if (!(err instanceof ApiError)) {
    return t('botProfiles.saveError');
  }

  if (err.status >= 500) {
    if (err.message.toLowerCase().includes('unavailable')) {
      return t('botProfiles.storageUnavailableUploadDisabled');
    }
    return t('botProfiles.storageConnectionFailed');
  }

  const message = err.message.trim();
  if (err.status === 400 && message.length === 0) {
    return t('botProfiles.unsupportedImage');
  }

  return message || t('botProfiles.saveError');
}

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
 * `BotProfilesPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
export function BotProfilesPage() {
  const {t} = useTranslation();
  const [searchParams] = useSearchParams();
  const [botProfiles, setBotProfiles] = useState<BotProfileItem[]>([]);
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const [savedMessage, setSavedMessage] = useState('');
  const [uploadCapability, setUploadCapability] = useState<UploadCapability>(defaultUploadCapability);
  const [capabilitiesLoading, setCapabilitiesLoading] = useState(false);
  const [pendingDelete, setPendingDelete] = useState<BotProfileItem | null>(null);
  const returnTo = safeReturnTo(searchParams.get('returnTo'));

  const canSubmit = useMemo(() => {
    const hasRequiredFields = Boolean(
      formState.alias.trim() && formState.username.trim() && (formState.id !== null || formState.avatar),
    );
    if (!hasRequiredFields) {
      return false;
    }
    if (!uploadCapability.enabled && (formState.id === null || formState.avatar)) {
      return false;
    }
    return true;
  }, [formState, uploadCapability.enabled]);

  const uploadUnavailable = !uploadCapability.enabled;

  const loadBotProfiles = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await apiGet<BotProfilePage>('/discord/bot-profiles?size=100&sort=id,desc');
      setBotProfiles(response.content ?? []);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('botProfiles.loadError');
      setError(message);
      setBotProfiles([]);
    } finally {
      setLoading(false);
    }
  }, [t]);

  const loadCapabilities = useCallback(async () => {
    setCapabilitiesLoading(true);

    try {
      const response = await apiGet<SystemCapabilities>('/system/capabilities');
      setUploadCapability(response.uploads?.botProfileAvatar ?? defaultUploadCapability);
    } catch {
      setUploadCapability(defaultUploadCapability);
    } finally {
      setCapabilitiesLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadBotProfiles();
    void loadCapabilities();
  }, [loadBotProfiles, loadCapabilities]);

  useEffect(() => {
    if (!formState.avatar) {
      setPreviewUrl(safeImageSrc(formState.currentAvatarUrl));
      return;
    }

    const objectUrl = URL.createObjectURL(formState.avatar);
    setPreviewUrl(objectUrl);
    return () => {
      URL.revokeObjectURL(objectUrl);
    };
  }, [formState.avatar, formState.currentAvatarUrl]);

  /**
   * `updateField`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function updateField(name: keyof Pick<FormState, 'alias' | 'username'>, value: string) {
    setSavedMessage('');
    setFormState((current) => ({...current, [name]: value}));
  }

  /**
   * `updateAvatar`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function updateAvatar(event: ChangeEvent<HTMLInputElement>) {
    if (uploadUnavailable) {
      setError(t('botProfiles.storageUnavailableUploadDisabled'));
      event.target.value = '';
      return;
    }

    const file = event.target.files?.[0] ?? null;
    if (!file) {
      setFormState((current) => ({...current, avatar: null}));
      return;
    }

    if (!allowedImageTypes.includes(file.type) || !hasAllowedAvatarExtension(file.name)) {
      setError(t('botProfiles.supportedImages'));
      event.target.value = '';
      return;
    }
    if (file.size <= 0) {
      setError(t('botProfiles.unsupportedImage'));
      event.target.value = '';
      return;
    }
    if (file.size > maxUploadBytes) {
      setError(t('botProfiles.maxImageSize', {size: fileSizeLabel(maxUploadBytes)}));
      event.target.value = '';
      return;
    }

    setError('');
    setSavedMessage('');
    setFormState((current) => ({...current, avatar: file}));
  }

  /**
   * `startEdit`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function startEdit(botProfile: BotProfileItem) {
    setSavedMessage('');
    setFormState({
      id: botProfile.id,
      alias: botProfile.alias,
      username: botProfile.username,
      avatar: null,
      currentAvatarUrl: safeImageSrc(botProfile.avatarUrl),
    });
  }

  /**
   * `resetForm`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
   */
  function resetForm() {
    setSavedMessage('');
    setFormState(emptyForm);
    setPreviewUrl('');
    const input = document.getElementById('botProfileAvatar') as HTMLInputElement | null;
    if (input) {
      input.value = '';
    }
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

    const body = new FormData();
    const nextAlias = formState.alias.trim();
    body.append('alias', nextAlias);
    body.append('username', formState.username.trim());
    if (formState.avatar) {
      body.append('avatar', formState.avatar);
    }
    const wasCreate = formState.id === null;

    try {
      if (wasCreate) {
        await apiPostForm('/discord/bot-profiles', body);
      } else {
        await apiPutForm(`/discord/bot-profiles/${formState.id}`, body);
      }
      resetForm();
      await loadBotProfiles();
      setSavedMessage(wasCreate ? t('botProfiles.added', {alias: nextAlias}) : t('botProfiles.saved', {alias: nextAlias}));
    } catch (err) {
      setError(saveErrorMessage(err, t));
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
      await apiDelete(`/discord/bot-profiles/${pendingDelete.id}`);
      await loadBotProfiles();
      if (formState.id === pendingDelete.id) {
        resetForm();
      }
      setPendingDelete(null);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('botProfiles.deleteError');
      setError(message);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <PagePlaceholder
      title={t('botProfiles.title')}
      description={t('botProfiles.description')}
      actions={
        <Link to={returnTo || '/subscriptions/new'} className="btn btn-outline-primary">
          {returnTo ? t('common.continueSetup') : t('botProfiles.createSubscription')}
        </Link>
      }
    >
      <SetupChecklist
        currentStep="botProfile"
        completed={{botProfile: botProfiles.length > 0}}
        returnTo={returnTo || '/subscriptions/new'}
      />
      {savedMessage ? (
        <div className="alert alert-success d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{savedMessage}</span>
          <Link to={returnTo || '/subscriptions/new'} className="btn btn-sm btn-success">
            {t('common.continueToSubscription')}
          </Link>
        </div>
      ) : null}
      {error ? (
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{error}</span>
          <div className="d-flex flex-wrap gap-2">
            <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadBotProfiles()}>
              {t('common.retryList')}
            </button>
            <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadCapabilities()} disabled={capabilitiesLoading}>
              {t('common.retryStorage')}
            </button>
          </div>
        </div>
      ) : null}
      {uploadUnavailable ? (
        <div className="alert alert-warning d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
          <div>
            <div className="fw-semibold">
              <i className="bi bi-exclamation-triangle me-2" aria-hidden="true" />
              {t('botProfiles.storageUnavailable')}
            </div>
            <div className="small">
              {t('botProfiles.avatarUploadsDisabled')}
              {uploadCapability.reason ? ` ${uploadCapability.reason}` : ''}
            </div>
          </div>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadCapabilities()} disabled={capabilitiesLoading}>
            {capabilitiesLoading ? (
              <span className="spinner-border spinner-border-sm me-1" aria-hidden="true" />
            ) : (
              <i className="bi bi-arrow-clockwise me-1" aria-hidden="true" />
            )}
            {t('common.retry')}
          </button>
        </div>
      ) : null}
      <form className="row g-3 mb-4" onSubmit={handleSubmit}>
        <div className="col-12">
          <h2 className="h6 mb-0">{formState.id === null ? t('botProfiles.addTitle') : t('botProfiles.editTitle')}</h2>
        </div>
        <div className="col-md-5">
          <label htmlFor="botProfileAlias" className="form-label">
            {t('botProfiles.alias')}
          </label>
          <input
            id="botProfileAlias"
            className="form-control"
            value={formState.alias}
            onChange={(event) => updateField('alias', event.target.value)}
            placeholder={t('botProfiles.aliasPlaceholder')}
            maxLength={100}
            disabled={saving}
            required
          />
        </div>
        <div className="col-md-5">
          <label htmlFor="botProfileUsername" className="form-label">
            {t('botProfiles.botName')}
          </label>
          <input
            id="botProfileUsername"
            className="form-control"
            value={formState.username}
            onChange={(event) => updateField('username', event.target.value)}
            placeholder={t('botProfiles.botNamePlaceholder')}
            maxLength={100}
            disabled={saving}
            required
          />
        </div>
        <div className="col-md-2 d-flex align-items-end justify-content-md-end">
          <span className="bot-profile-preview" aria-hidden="true">
            <i className="bi bi-image" />
            {previewUrl ? (
              <img
                src={previewUrl}
                alt=""
                onError={(event) => {
                  event.currentTarget.style.display = 'none';
                }}
              />
            ) : null}
          </span>
        </div>
        <div className="col-12">
          <label htmlFor="botProfileAvatar" className="form-label">
            {t('botProfiles.profileImage')}
          </label>
          <div className="input-group">
            <span className="input-group-text">
              <i className="bi bi-upload" aria-hidden="true" />
            </span>
            <input
              id="botProfileAvatar"
              className="form-control"
              type="file"
              accept={allowedImageTypes.join(',')}
              onChange={updateAvatar}
              disabled={saving || uploadUnavailable}
              required={formState.id === null}
            />
          </div>
          <div className="form-text">{t('botProfiles.imageHelp', {size: fileSizeLabel(maxUploadBytes)})}</div>
        </div>
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            {saving ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
                {t('common.saving')}
              </>
            ) : (
              <>
                <i className={`bi ${formState.id === null ? 'bi-plus-lg' : 'bi-save'} me-1`} aria-hidden="true" />
                {formState.id === null ? t('botProfiles.add') : t('botProfiles.save')}
              </>
            )}
          </button>
          {formState.id !== null ? (
            <button type="button" className="btn btn-outline-secondary" onClick={resetForm} disabled={saving}>
              {t('common.cancel')}
            </button>
          ) : null}
        </div>
      </form>

      {loading ? (
        <div className="text-center py-4">{t('botProfiles.loading')}</div>
      ) : botProfiles.length === 0 ? (
        <div className="empty-state py-4 text-center text-secondary">
          <p className="mb-2">{t('botProfiles.empty')}</p>
          <p className="small mb-2">{t('botProfiles.emptyBody')}</p>
          <Link to="/subscriptions/new" className="btn btn-sm btn-outline-primary">
            {t('botProfiles.createSubscription')}
          </Link>
        </div>
      ) : (
        <>
        <div className="table-responsive d-none d-md-block">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>{t('botProfiles.table.profile')}</th>
                <th>{t('botProfiles.table.alias')}</th>
                <th>{t('botProfiles.table.botName')}</th>
                <th className="text-end">{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {botProfiles.map((botProfile) => {
                const avatarSrc = safeImageSrc(botProfile.avatarUrl);
                return (
                  <tr key={botProfile.id}>
                    <td>
                      <span className="bot-profile-avatar" aria-hidden="true">
                        <i className="bi bi-robot" />
                      {avatarSrc ? (
                        <img
                            src={avatarSrc}
                            alt=""
                            onError={(event) => {
                              event.currentTarget.style.display = 'none';
                            }}
                          />
                        ) : null}
                      </span>
                    </td>
                    <td className="fw-semibold">{botProfile.alias}</td>
                    <td>{botProfile.username}</td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(botProfile)} disabled={saving} aria-label={t('botProfiles.editAria', {alias: botProfile.alias})}>
                          <i className="bi bi-pencil" aria-hidden="true" />
                        </button>
                        <button type="button" className="btn btn-outline-danger" onClick={() => setPendingDelete(botProfile)} disabled={saving} aria-label={t('botProfiles.deleteAria', {alias: botProfile.alias})}>
                          <i className="bi bi-trash" aria-hidden="true" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <div className="mobile-resource-list d-md-none">
          {botProfiles.map((botProfile) => {
            const avatarSrc = safeImageSrc(botProfile.avatarUrl);
            return (
              <div className="mobile-resource-item" key={botProfile.id}>
                <div className="d-flex align-items-center justify-content-between gap-3">
                  <div className="d-flex align-items-center gap-3 min-w-0">
                    <span className="bot-profile-avatar" aria-hidden="true">
                      <i className="bi bi-robot" />
                      {avatarSrc ? (
                        <img
                          src={avatarSrc}
                          alt=""
                          onError={(event) => {
                            event.currentTarget.style.display = 'none';
                          }}
                        />
                      ) : null}
                    </span>
                    <div className="min-w-0">
                      <div className="fw-semibold">{botProfile.alias}</div>
                      <div className="small text-secondary">{botProfile.username}</div>
                    </div>
                  </div>
                  <div className="btn-group btn-group-sm">
                    <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(botProfile)} disabled={saving} aria-label={t('botProfiles.editAria', {alias: botProfile.alias})}>
                      <i className="bi bi-pencil" aria-hidden="true" />
                    </button>
                    <button type="button" className="btn btn-outline-danger" onClick={() => setPendingDelete(botProfile)} disabled={saving} aria-label={t('botProfiles.deleteAria', {alias: botProfile.alias})}>
                      <i className="bi bi-trash" aria-hidden="true" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
        </>
      )}
      <ConfirmDeleteModal
        open={Boolean(pendingDelete)}
        title={t('botProfiles.deleteTitle')}
        message={t('botProfiles.deleteMessage', {alias: pendingDelete?.alias ?? ''})}
        busy={deleting}
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => void confirmDelete()}
      />
    </PagePlaceholder>
  );
}
