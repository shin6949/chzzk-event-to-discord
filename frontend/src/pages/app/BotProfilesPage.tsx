import { ChangeEvent, FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { ApiError, apiDelete, apiGet, apiPostForm, apiPutForm } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type BotProfileItem = {
  id: number;
  alias: string;
  username: string;
  avatarUrl: string;
  ownerChannelId: string;
};

type BotProfilePage = {
  content: BotProfileItem[];
  totalElements: number;
};

type UploadCapability = {
  enabled: boolean;
  reason?: string | null;
  checkedAt?: string | null;
};

type SystemCapabilities = {
  uploads?: {
    botProfileAvatar?: UploadCapability;
  };
};

type FormState = {
  id: number | null;
  alias: string;
  username: string;
  avatar: File | null;
  currentAvatarUrl: string;
};

const emptyForm: FormState = {
  id: null,
  alias: '',
  username: '',
  avatar: null,
  currentAvatarUrl: '',
};

const allowedImageTypes = ['image/png', 'image/jpeg', 'image/webp'];
const maxUploadBytes = 5 * 1024 * 1024;
const defaultUploadCapability: UploadCapability = { enabled: true };

function fileSizeLabel(bytes: number): string {
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function saveErrorMessage(err: unknown): string {
  if (!(err instanceof ApiError)) {
    return 'Unable to save bot profile.';
  }

  if (err.status >= 500) {
    if (err.message.toLowerCase().includes('unavailable')) {
      return 'Image storage is unavailable. Avatar uploads are temporarily disabled.';
    }
    return 'Image storage connection failed. Check object storage and try again.';
  }

  const message = err.message.trim();
  if (err.status === 400 && message.length === 0) {
    return 'The selected image format or size is not supported.';
  }

  return message || 'Unable to save bot profile.';
}

export function BotProfilesPage() {
  const [botProfiles, setBotProfiles] = useState<BotProfileItem[]>([]);
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [previewUrl, setPreviewUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [uploadCapability, setUploadCapability] = useState<UploadCapability>(defaultUploadCapability);
  const [capabilitiesLoading, setCapabilitiesLoading] = useState(false);

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
      const message = err instanceof ApiError ? err.message : 'Failed to load bot profiles.';
      setError(message);
      setBotProfiles([]);
    } finally {
      setLoading(false);
    }
  }, []);

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
      setPreviewUrl(formState.currentAvatarUrl);
      return;
    }

    const objectUrl = URL.createObjectURL(formState.avatar);
    setPreviewUrl(objectUrl);
    return () => {
      URL.revokeObjectURL(objectUrl);
    };
  }, [formState.avatar, formState.currentAvatarUrl]);

  function updateField(name: keyof Pick<FormState, 'alias' | 'username'>, value: string) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

  function updateAvatar(event: ChangeEvent<HTMLInputElement>) {
    if (uploadUnavailable) {
      setError('Image storage is unavailable. Avatar uploads are temporarily disabled.');
      event.target.value = '';
      return;
    }

    const file = event.target.files?.[0] ?? null;
    if (!file) {
      setFormState((current) => ({ ...current, avatar: null }));
      return;
    }

    if (!allowedImageTypes.includes(file.type)) {
      setError('Only PNG, JPEG, and WebP images are supported.');
      event.target.value = '';
      return;
    }
    if (file.size > maxUploadBytes) {
      setError(`Avatar image must be ${fileSizeLabel(maxUploadBytes)} or smaller.`);
      event.target.value = '';
      return;
    }

    setError('');
    setFormState((current) => ({ ...current, avatar: file }));
  }

  function startEdit(botProfile: BotProfileItem) {
    setFormState({
      id: botProfile.id,
      alias: botProfile.alias,
      username: botProfile.username,
      avatar: null,
      currentAvatarUrl: botProfile.avatarUrl,
    });
  }

  function resetForm() {
    setFormState(emptyForm);
    setPreviewUrl('');
    const input = document.getElementById('botProfileAvatar') as HTMLInputElement | null;
    if (input) {
      input.value = '';
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    setSaving(true);
    setError('');

    const body = new FormData();
    body.append('alias', formState.alias.trim());
    body.append('username', formState.username.trim());
    if (formState.avatar) {
      body.append('avatar', formState.avatar);
    }

    try {
      if (formState.id === null) {
        await apiPostForm('/discord/bot-profiles', body);
      } else {
        await apiPutForm(`/discord/bot-profiles/${formState.id}`, body);
      }
      resetForm();
      await loadBotProfiles();
    } catch (err) {
      setError(saveErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(botProfile: BotProfileItem) {
    if (!window.confirm(`Delete bot profile "${botProfile.alias}"?`)) {
      return;
    }

    try {
      await apiDelete(`/discord/bot-profiles/${botProfile.id}`);
      await loadBotProfiles();
      if (formState.id === botProfile.id) {
        resetForm();
      }
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to delete bot profile.';
      setError(message);
    }
  }

  return (
    <PagePlaceholder title="Bot Profiles" description="Manage Discord bot names and profile images.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      {uploadUnavailable ? (
        <div className="alert alert-warning d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2">
          <div>
            <div className="fw-semibold">
              <i className="bi bi-exclamation-triangle me-2" aria-hidden="true" />
              Image storage is unavailable.
            </div>
            <div className="small">
              Avatar uploads are temporarily disabled.
              {uploadCapability.reason ? ` ${uploadCapability.reason}` : ''}
            </div>
          </div>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadCapabilities()} disabled={capabilitiesLoading}>
            {capabilitiesLoading ? (
              <span className="spinner-border spinner-border-sm me-1" aria-hidden="true" />
            ) : (
              <i className="bi bi-arrow-clockwise me-1" aria-hidden="true" />
            )}
            Retry
          </button>
        </div>
      ) : null}
      <form className="row g-3 mb-4" onSubmit={handleSubmit}>
        <div className="col-md-5">
          <label htmlFor="botProfileAlias" className="form-label">
            Alias
          </label>
          <input
            id="botProfileAlias"
            className="form-control"
            value={formState.alias}
            onChange={(event) => updateField('alias', event.target.value)}
            placeholder="Main bot"
            disabled={saving}
            required
          />
        </div>
        <div className="col-md-5">
          <label htmlFor="botProfileUsername" className="form-label">
            Bot name
          </label>
          <input
            id="botProfileUsername"
            className="form-control"
            value={formState.username}
            onChange={(event) => updateField('username', event.target.value)}
            placeholder="CHZZK notifier"
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
            Profile image
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
          <div className="form-text">PNG, JPEG, or WebP up to {fileSizeLabel(maxUploadBytes)}.</div>
        </div>
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            {saving ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
                Saving...
              </>
            ) : (
              <>
                <i className={`bi ${formState.id === null ? 'bi-plus-lg' : 'bi-save'} me-1`} aria-hidden="true" />
                {formState.id === null ? 'Add bot profile' : 'Save bot profile'}
              </>
            )}
          </button>
          {formState.id !== null ? (
            <button type="button" className="btn btn-outline-secondary" onClick={resetForm} disabled={saving}>
              Cancel
            </button>
          ) : null}
        </div>
      </form>

      {loading ? (
        <div className="text-center py-4">Loading bot profiles...</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>Profile</th>
                <th>Alias</th>
                <th>Bot name</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {botProfiles.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center text-secondary">
                    No bot profiles found.
                  </td>
                </tr>
              ) : (
                botProfiles.map((botProfile) => (
                  <tr key={botProfile.id}>
                    <td>
                      <span className="bot-profile-avatar" aria-hidden="true">
                        <i className="bi bi-robot" />
                        {botProfile.avatarUrl ? (
                          <img
                            src={botProfile.avatarUrl}
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
                        <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(botProfile)} disabled={saving} aria-label={`Edit ${botProfile.alias}`}>
                          <i className="bi bi-pencil" aria-hidden="true" />
                        </button>
                        <button type="button" className="btn btn-outline-danger" onClick={() => void handleDelete(botProfile)} disabled={saving} aria-label={`Delete ${botProfile.alias}`}>
                          <i className="bi bi-trash" aria-hidden="true" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </PagePlaceholder>
  );
}
