import { FormEvent, useEffect, useMemo, useState } from 'react';
import { ApiError, apiDelete, apiGet, apiPost } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type TwitchSubscription = {
  id: number;
  broadcasterUserId: string;
  broadcasterLogin: string;
  broadcasterDisplayName: string;
  enabled: boolean;
  eventsubOnlineId?: string | null;
  eventsubOfflineId?: string | null;
  createdAt?: string | null;
};

type TwitchSubscriptionPayload = {
  broadcasterLogin?: string;
  broadcasterUserId?: string;
  discordWebhookUrl: string;
  enabled: boolean;
};

const initialForm = {
  broadcasterLogin: '',
  broadcasterUserId: '',
  discordWebhookUrl: '',
  enabled: true,
};

export function TwitchSubscriptionsPage() {
  const [subscriptions, setSubscriptions] = useState<TwitchSubscription[]>([]);
  const [formState, setFormState] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadSubscriptions() {
    setLoading(true);
    setError('');
    try {
      setSubscriptions(await apiGet<TwitchSubscription[]>('/twitch/subscriptions'));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to load Twitch subscriptions.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadSubscriptions();
  }, []);

  const formIsValid = useMemo(() => {
    const hasBroadcaster = formState.broadcasterLogin.trim() || formState.broadcasterUserId.trim();
    return Boolean(hasBroadcaster && formState.discordWebhookUrl.trim());
  }, [formState]);

  function updateField(name: keyof typeof formState, value: string | boolean) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!formIsValid) {
      setError('Enter a Twitch login or user ID and a Discord webhook URL.');
      return;
    }

    setSaving(true);
    setError('');
    const payload: TwitchSubscriptionPayload = {
      discordWebhookUrl: formState.discordWebhookUrl.trim(),
      enabled: formState.enabled,
    };
    if (formState.broadcasterLogin.trim()) {
      payload.broadcasterLogin = formState.broadcasterLogin.trim();
    }
    if (formState.broadcasterUserId.trim()) {
      payload.broadcasterUserId = formState.broadcasterUserId.trim();
    }

    try {
      await apiPost('/twitch/subscriptions', payload);
      setFormState(initialForm);
      await loadSubscriptions();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to create Twitch subscription.');
    } finally {
      setSaving(false);
    }
  }

  async function deleteSubscription(id: number) {
    setError('');
    try {
      await apiDelete(`/twitch/subscriptions/${id}`);
      setSubscriptions((current) => current.filter((subscription) => subscription.id !== id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to delete Twitch subscription.');
    }
  }

  return (
    <PagePlaceholder
      title="Twitch EventSub"
      description="Use Twitch EventSub stream.online and stream.offline webhooks to notify Discord when a selected channel starts or ends a live stream."
    >
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="card mb-4">
        <div className="card-body">
          <h2 className="h5">Create Twitch live alert</h2>
          <form className="row g-3" onSubmit={handleSubmit}>
            <div className="col-md-6">
              <label htmlFor="broadcasterLogin" className="form-label">Twitch login</label>
              <input
                id="broadcasterLogin"
                className="form-control"
                placeholder="example_streamer"
                value={formState.broadcasterLogin}
                onChange={(event) => updateField('broadcasterLogin', event.target.value)}
              />
            </div>
            <div className="col-md-6">
              <label htmlFor="broadcasterUserId" className="form-label">Twitch user ID</label>
              <input
                id="broadcasterUserId"
                className="form-control"
                placeholder="Optional if login is entered"
                value={formState.broadcasterUserId}
                onChange={(event) => updateField('broadcasterUserId', event.target.value)}
              />
            </div>
            <div className="col-12">
              <label htmlFor="discordWebhookUrl" className="form-label">Discord webhook URL</label>
              <input
                id="discordWebhookUrl"
                className="form-control"
                type="url"
                placeholder="https://discord.com/api/webhooks/..."
                value={formState.discordWebhookUrl}
                onChange={(event) => updateField('discordWebhookUrl', event.target.value)}
                required
              />
            </div>
            <div className="col-12 form-check ms-2">
              <input
                id="enabled"
                className="form-check-input"
                type="checkbox"
                checked={formState.enabled}
                onChange={(event) => updateField('enabled', event.target.checked)}
              />
              <label htmlFor="enabled" className="form-check-label">Enable Discord notifications</label>
            </div>
            <div className="col-12">
              <button type="submit" className="btn btn-primary" disabled={saving || !formIsValid}>
                {saving ? 'Creating...' : 'Create EventSub subscriptions'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="card">
        <div className="card-body">
          <h2 className="h5">Configured Twitch channels</h2>
          {loading ? <p className="text-secondary">Loading Twitch subscriptions...</p> : null}
          {!loading && subscriptions.length === 0 ? <p className="text-secondary">No Twitch subscriptions yet.</p> : null}
          {subscriptions.length > 0 ? (
            <div className="table-responsive">
              <table className="table align-middle">
                <thead>
                  <tr>
                    <th>Channel</th>
                    <th>User ID</th>
                    <th>Status</th>
                    <th>EventSub IDs</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {subscriptions.map((subscription) => (
                    <tr key={subscription.id}>
                      <td>
                        <a href={`https://www.twitch.tv/${subscription.broadcasterLogin}`} target="_blank" rel="noreferrer">
                          {subscription.broadcasterDisplayName}
                        </a>
                      </td>
                      <td>{subscription.broadcasterUserId}</td>
                      <td>{subscription.enabled ? 'Enabled' : 'Disabled'}</td>
                      <td>
                        <small className="text-secondary">
                          online: {subscription.eventsubOnlineId ?? '-'}<br />
                          offline: {subscription.eventsubOfflineId ?? '-'}
                        </small>
                      </td>
                      <td className="text-end">
                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => void deleteSubscription(subscription.id)}>
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </div>
      </div>
    </PagePlaceholder>
  );
}
