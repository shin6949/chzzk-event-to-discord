import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { ApiError, apiDelete, apiGet, apiPost, apiPut } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type WebhookItem = {
  id: number;
  alias: string;
  url: string;
  ownerChannelId: string;
};

type WebhookPage = {
  content: WebhookItem[];
  totalElements: number;
};

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

function maskWebhookUrl(url: string): string {
  if (url.length <= 36) {
    return url;
  }
  return `${url.slice(0, 28)}...${url.slice(-8)}`;
}

export function WebhooksPage() {
  const [webhooks, setWebhooks] = useState<WebhookItem[]>([]);
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const canSubmit = useMemo(() => Boolean(formState.alias.trim() && formState.url.trim()), [formState]);

  const loadWebhooks = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await apiGet<WebhookPage>('/discord/webhooks?size=100&sort=id,desc');
      setWebhooks(response.content ?? []);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load webhooks.';
      setError(message);
      setWebhooks([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadWebhooks();
  }, [loadWebhooks]);

  function updateField(name: keyof Pick<FormState, 'alias' | 'url'>, value: string) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

  function startEdit(webhook: WebhookItem) {
    setFormState({ id: webhook.id, alias: webhook.alias, url: webhook.url });
  }

  function resetForm() {
    setFormState(emptyForm);
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    setSaving(true);
    setError('');

    const payload = {
      alias: formState.alias.trim(),
      url: formState.url.trim(),
    };

    try {
      if (formState.id === null) {
        await apiPost('/discord/webhooks', payload);
      } else {
        await apiPut(`/discord/webhooks/${formState.id}`, payload);
      }
      resetForm();
      await loadWebhooks();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to save webhook.';
      setError(message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(webhook: WebhookItem) {
    if (!window.confirm(`Delete webhook "${webhook.alias}"?`)) {
      return;
    }

    try {
      await apiDelete(`/discord/webhooks/${webhook.id}`);
      await loadWebhooks();
      if (formState.id === webhook.id) {
        resetForm();
      }
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to delete webhook.';
      setError(message);
    }
  }

  return (
    <PagePlaceholder title="Webhooks" description="Manage Discord webhook destinations.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <form className="row g-3 mb-4" onSubmit={handleSubmit}>
        <div className="col-md-4">
          <label htmlFor="webhookAlias" className="form-label">
            Alias
          </label>
          <input
            id="webhookAlias"
            className="form-control"
            value={formState.alias}
            onChange={(event) => updateField('alias', event.target.value)}
            placeholder="Main Discord channel"
            required
          />
        </div>
        <div className="col-md-8">
          <label htmlFor="webhookUrl" className="form-label">
            Webhook URL
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
              placeholder="https://discord.com/api/webhooks/..."
              required
            />
          </div>
        </div>
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            <i className={`bi ${formState.id === null ? 'bi-plus-lg' : 'bi-save'} me-1`} aria-hidden="true" />
            {saving ? 'Saving...' : formState.id === null ? 'Add webhook' : 'Save webhook'}
          </button>
          {formState.id !== null ? (
            <button type="button" className="btn btn-outline-secondary" onClick={resetForm}>
              Cancel
            </button>
          ) : null}
        </div>
      </form>

      {loading ? (
        <div className="text-center py-4">Loading webhooks...</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>Alias</th>
                <th>URL</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {webhooks.length === 0 ? (
                <tr>
                  <td colSpan={3} className="text-center text-secondary">
                    No webhooks found.
                  </td>
                </tr>
              ) : (
                webhooks.map((webhook) => (
                  <tr key={webhook.id}>
                    <td className="fw-semibold">{webhook.alias}</td>
                    <td>
                      <code>{maskWebhookUrl(webhook.url)}</code>
                    </td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button type="button" className="btn btn-outline-primary" onClick={() => startEdit(webhook)} aria-label={`Edit ${webhook.alias}`}>
                          <i className="bi bi-pencil" aria-hidden="true" />
                        </button>
                        <button type="button" className="btn btn-outline-danger" onClick={() => void handleDelete(webhook)} aria-label={`Delete ${webhook.alias}`}>
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
