import { FormEvent, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError, apiPost } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type SubscriptionType = 'STREAM_ONLINE' | 'STREAM_OFFLINE' | 'CHANNEL_UPDATE' | 'STREAM_ONLINE_AND_OFFLINE';

type FormState = {
  channelId: string;
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

function createInitialState(): FormState {
  return {
    channelId: '',
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
  const [formState, setFormState] = useState<FormState>(createInitialState);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const formIsValid = useMemo(() => {
    return Boolean(formState.channelId.trim() && formState.webhookId.trim() && formState.botProfileId.trim());
  }, [formState]);

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

    if (!formIsValid || !Number.isFinite(webhookId) || !Number.isFinite(botProfileId) || !Number.isFinite(intervalMinute)) {
      setError('Please fill required fields with valid numeric values.');
      setLoading(false);
      return;
    }

    const payload: ApiPayload = {
      channelId: formState.channelId.trim(),
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

  return (
    <PagePlaceholder title="New Subscription" description="Create a new subscription configuration.">
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
        <div className="col-12">
          <button type="submit" className="btn btn-primary" disabled={loading || !formIsValid}>
            {loading ? 'Creating...' : 'Create subscription'}
          </button>
        </div>
      </form>
    </PagePlaceholder>
  );
}
