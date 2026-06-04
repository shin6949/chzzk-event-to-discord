import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError, apiGet, apiPost } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type SubscriptionType = 'STREAM_ONLINE' | 'STREAM_OFFLINE' | 'CHANNEL_UPDATE' | 'STREAM_ONLINE_AND_OFFLINE';

type FormState = {
  subscriptionType: SubscriptionType;
  webhookId: string;
  botProfileId: string;
  intervalMinute: string;
  enabled: boolean;
  content: string;
};

type ApiPayload = {
  channelId: string;
  subscriptionType: SubscriptionType;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  enabled: boolean;
  content: string;
};

type ResourcePage<T> = {
  content: T[];
};

type WebhookOption = {
  id: number;
  alias: string;
  url: string;
};

type BotProfileOption = {
  id: number;
  alias: string;
  username: string;
  avatarUrl: string;
};

function createInitialState(): FormState {
  return {
    subscriptionType: 'STREAM_ONLINE',
    webhookId: '',
    botProfileId: '',
    intervalMinute: '10',
    enabled: true,
    content: '',
  };
}

export function NewSubscriptionPage() {
  const navigate = useNavigate();
  const { session, loading: sessionLoading } = useAuth();
  const [formState, setFormState] = useState<FormState>(createInitialState);
  const [webhooks, setWebhooks] = useState<WebhookOption[]>([]);
  const [botProfiles, setBotProfiles] = useState<BotProfileOption[]>([]);
  const [resourcesLoading, setResourcesLoading] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const sessionChannelId = session?.channelId.trim() ?? '';
  const sessionChannelName = session?.channelName?.trim() ?? '';
  const channelDisplayName = sessionLoading ? 'Loading channel...' : sessionChannelName || 'Signed-in channel';

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
      const message = err instanceof ApiError ? err.message : 'Unable to load Discord resources.';
      setError(message);
      setWebhooks([]);
      setBotProfiles([]);
    } finally {
      setResourcesLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadResources();
  }, [loadResources]);

  const formIsValid = useMemo(() => {
    return Boolean(sessionChannelId && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState.botProfileId, formState.webhookId, sessionChannelId]);

  function updateField(name: keyof FormState, value: string | boolean) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError('');

    const webhookId = Number(formState.webhookId);
    const botProfileId = Number(formState.botProfileId);
    const intervalMinute = Number(formState.intervalMinute);

    if (!sessionChannelId) {
      setError('Unable to resolve your channel. Please sign in again.');
      setLoading(false);
      return;
    }

    if (!formIsValid || !Number.isFinite(webhookId) || !Number.isFinite(botProfileId) || !Number.isFinite(intervalMinute)) {
      setError('Please choose Discord resources and enter a valid interval.');
      setLoading(false);
      return;
    }

    const payload: ApiPayload = {
      channelId: sessionChannelId,
      subscriptionType: formState.subscriptionType,
      webhookId,
      botProfileId,
      intervalMinute,
      enabled: formState.enabled,
      content: formState.content,
    };

    try {
      await apiPost('/subscriptions', payload);
      navigate('/subscriptions');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to create subscription.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  const missingResources = !resourcesLoading && (webhooks.length === 0 || botProfiles.length === 0);

  return (
    <PagePlaceholder title="New Subscription" description="Create a new subscription configuration.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      {missingResources ? (
        <div className="alert alert-warning d-flex flex-wrap align-items-center gap-2">
          <i className="bi bi-exclamation-triangle" aria-hidden="true" />
          <span className="me-auto">Create at least one webhook and one bot profile before adding a subscription.</span>
          {webhooks.length === 0 ? (
            <Link to="/discord/webhooks" className="btn btn-sm btn-outline-dark">
              Webhooks
            </Link>
          ) : null}
          {botProfiles.length === 0 ? (
            <Link to="/discord/bot-profiles" className="btn btn-sm btn-outline-dark">
              Bot Profiles
            </Link>
          ) : null}
        </div>
      ) : null}
      <form className="row g-3 mt-1" onSubmit={handleSubmit}>
        <div className="col-12">
          <div className="form-label">Channel</div>
          <div className="subscription-channel-summary d-flex align-items-center gap-2">
            <span className="channel-avatar subscription-channel-avatar" aria-hidden="true">
              <i className="bi bi-person-fill" aria-hidden="true" />
              {session?.profileUrl ? (
                <img
                  src={session.profileUrl}
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
            disabled={resourcesLoading}
            required
          >
            <option value="">{resourcesLoading ? 'Loading webhooks...' : 'Choose webhook'}</option>
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
            disabled={resourcesLoading}
            required
          >
            <option value="">{resourcesLoading ? 'Loading bot profiles...' : 'Choose bot profile'}</option>
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
        <div className="col-12">
          <button type="submit" className="btn btn-primary" disabled={loading || sessionLoading || resourcesLoading || !formIsValid}>
            {loading ? 'Creating...' : 'Create subscription'}
          </button>
        </div>
      </form>
    </PagePlaceholder>
  );
}
