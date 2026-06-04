import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
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

type ResourcePage<T> = {
  content: T[];
};

type WebhookOption = {
  id: number;
  alias: string;
};

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
  enabled: true,
  content: '',
};

export function SubscriptionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [formState, setFormState] = useState<FormState>(emptyForm);
  const [webhooks, setWebhooks] = useState<WebhookOption[]>([]);
  const [botProfiles, setBotProfiles] = useState<BotProfileOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');

  const canSubmit = useMemo(() => {
    return Boolean(formState.channelId.trim() && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState]);

  useEffect(() => {
    if (!id) {
      setLoadError('Missing subscription id.');
      setLoading(false);
      return;
    }

    async function loadSubscription() {
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
          enabled: subscriptionResponse.enabled,
          content: subscriptionResponse.content ?? '',
        });
      } catch (err) {
        const message = err instanceof ApiError ? err.message : 'Unable to load subscription.';
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
        const message = err instanceof ApiError ? err.message : 'Unable to load Discord resources.';
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
      setError('Please choose Discord resources and enter a valid interval.');
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

  if (loadError) {
    return (
      <PagePlaceholder title={`Subscription ${id ?? ''}`} description="This subscription cannot be opened.">
        <div className="alert alert-danger mb-3">{loadError}</div>
        <dl className="row mb-4">
          <dt className="col-sm-3">Subscription ID</dt>
          <dd className="col-sm-9">{id ?? 'Unknown'}</dd>
        </dl>
        <Link to="/subscriptions" className="btn btn-primary">
          <i className="bi bi-arrow-left me-1" aria-hidden="true" />
          Back to subscriptions
        </Link>
      </PagePlaceholder>
    );
  }

  const selectedWebhookIsMissing = formState.webhookId && !webhooks.some((webhook) => String(webhook.id) === formState.webhookId);
  const selectedBotProfileIsMissing = formState.botProfileId && !botProfiles.some((botProfile) => String(botProfile.id) === formState.botProfileId);

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
            Webhook
          </label>
          <select
            id="webhookId"
            className="form-select"
            value={formState.webhookId}
            onChange={(event) => updateField('webhookId', event.target.value)}
            required
          >
            <option value="">Choose webhook</option>
            {selectedWebhookIsMissing ? <option value={formState.webhookId}>Webhook #{formState.webhookId}</option> : null}
            {webhooks.map((webhook) => (
              <option key={webhook.id} value={webhook.id}>
                {webhook.alias}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-6">
          <label htmlFor="botProfileId" className="form-label">
            Bot Profile
          </label>
          <select
            id="botProfileId"
            className="form-select"
            value={formState.botProfileId}
            onChange={(event) => updateField('botProfileId', event.target.value)}
            required
          >
            <option value="">Choose bot profile</option>
            {selectedBotProfileIsMissing ? <option value={formState.botProfileId}>Bot Profile #{formState.botProfileId}</option> : null}
            {botProfiles.map((botProfile) => (
              <option key={botProfile.id} value={botProfile.id}>
                {botProfile.alias} ({botProfile.username})
              </option>
            ))}
          </select>
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
