import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ApiError, apiDelete, apiGet, apiPut } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type SubscriptionType = 'STREAM_ONLINE' | 'STREAM_OFFLINE' | 'CHANNEL_UPDATE' | 'STREAM_ONLINE_AND_OFFLINE';

type SubscriptionPayload = {
  channelId: string;
  subscriptionType: SubscriptionType;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  enabled: boolean;
  content: string;
};

type FormState = {
  channelId: string;
  subscriptionType: SubscriptionType;
  webhookId: string;
  botProfileId: string;
  intervalMinute: string;
  enabled: boolean;
  content: string;
};

const emptyForm: FormState = {
  channelId: '',
  subscriptionType: 'STREAM_ONLINE',
  webhookId: '',
  botProfileId: '',
  intervalMinute: '10',
  enabled: true,
  content: '',
};

export function SubscriptionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const canSubmit = useMemo(() => {
    return Boolean(formState.channelId.trim() && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState]);

  useEffect(() => {
    if (!id) {
      setError('Missing subscription id.');
      setLoading(false);
      return;
    }

    async function loadSubscription() {
      try {
        const response = await apiGet<SubscriptionPayload>(`/subscriptions/${id}`);
        setFormState({
          channelId: response.channelId,
          subscriptionType: response.subscriptionType as SubscriptionType,
          webhookId: String(response.webhookId),
          botProfileId: String(response.botProfileId),
          intervalMinute: String(response.intervalMinute ?? 10),
          enabled: response.enabled,
          content: response.content ?? '',
        });
      } catch (err) {
        const message = err instanceof ApiError ? err.message : 'Unable to load subscription.';
        setError(message);
      } finally {
        setLoading(false);
      }
    }

    void loadSubscription();
  }, [id]);

  function updateField(name: keyof FormState, value: string | boolean) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

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
      setError('Please enter valid numeric values.');
      setSaving(false);
      return;
    }

    const payload: SubscriptionPayload = {
      channelId: formState.channelId.trim(),
      subscriptionType: formState.subscriptionType,
      webhookId,
      botProfileId,
      intervalMinute,
      enabled: formState.enabled,
      content: formState.content,
    };

    try {
      await apiPut(`/subscriptions/${id}`, payload);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to update subscription.';
      setError(message);
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!id || !window.confirm('Delete this subscription?')) {
      return;
    }

    try {
      await apiDelete(`/subscriptions/${id}`);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to delete subscription.';
      setError(message);
    }
  }

  if (loading) {
    return <div className="py-5 text-center">Loading subscription...</div>;
  }

  return (
    <PagePlaceholder title={`Edit subscription ${id ?? ''}`} description="Update an existing subscription configuration.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <form className="row g-3 mt-1" onSubmit={handleSubmit}>
        <div className="col-12">
          <label htmlFor="channelId" className="form-label">
            Channel ID (target channel)
          </label>
          <input
            id="channelId"
            className="form-control"
            value={formState.channelId}
            onChange={(event) => updateField('channelId', event.target.value)}
            required
          />
        </div>
        <div className="col-12">
          <label htmlFor="subscriptionType" className="form-label">
            Subscription type
          </label>
          <select
            id="subscriptionType"
            className="form-select"
            value={formState.subscriptionType}
            onChange={(event) => updateField('subscriptionType', event.target.value as SubscriptionType)}
            required
          >
            <option value="STREAM_ONLINE">STREAM_ONLINE</option>
            <option value="STREAM_OFFLINE">STREAM_OFFLINE</option>
            <option value="CHANNEL_UPDATE">CHANNEL_UPDATE</option>
            <option value="STREAM_ONLINE_AND_OFFLINE">STREAM_ONLINE_AND_OFFLINE</option>
          </select>
        </div>
        <div className="col-md-6">
          <label htmlFor="webhookId" className="form-label">
            Webhook ID
          </label>
          <input
            id="webhookId"
            className="form-control"
            inputMode="numeric"
            value={formState.webhookId}
            onChange={(event) => updateField('webhookId', event.target.value)}
            required
          />
        </div>
        <div className="col-md-6">
          <label htmlFor="botProfileId" className="form-label">
            Bot Profile ID
          </label>
          <input
            id="botProfileId"
            className="form-control"
            inputMode="numeric"
            value={formState.botProfileId}
            onChange={(event) => updateField('botProfileId', event.target.value)}
            required
          />
        </div>
        <div className="col-md-4">
          <label htmlFor="intervalMinute" className="form-label">
            Interval (minutes)
          </label>
          <input
            id="intervalMinute"
            className="form-control"
            inputMode="numeric"
            value={formState.intervalMinute}
            onChange={(event) => updateField('intervalMinute', event.target.value)}
            required
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
              Enabled
            </label>
          </div>
        </div>
        <div className="col-12">
          <label htmlFor="content" className="form-label">
            Content
          </label>
          <textarea
            id="content"
            className="form-control"
            value={formState.content}
            onChange={(event) => updateField('content', event.target.value)}
            rows={3}
          />
        </div>
        <div className="col-12 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={saving || !canSubmit}>
            {saving ? 'Saving...' : 'Save changes'}
          </button>
          <button type="button" className="btn btn-outline-danger" onClick={() => void handleDelete()}>
            Delete
          </button>
        </div>
      </form>
    </PagePlaceholder>
  );
}
