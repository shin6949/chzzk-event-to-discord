import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, apiDelete, apiGet } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type SoopSubscription = {
  id: number;
  soopUserId: string;
  soopChannelName: string;
  live: boolean;
  liveTitle?: string;
  liveUrl?: string;
  webhookId: number;
  botProfileId: number;
  enabled: boolean;
  notifyOnline: boolean;
  notifyOffline: boolean;
};

type SoopSubscriptionPage = {
  content: SoopSubscription[];
  totalElements: number;
};

export function SoopSubscriptionsPage() {
  const [subscriptions, setSubscriptions] = useState<SoopSubscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [totalElements, setTotalElements] = useState(0);

  const loadSubscriptions = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await apiGet<SoopSubscriptionPage>('/soop/subscriptions?size=100&sort=id,desc');
      setSubscriptions(response.content ?? []);
      setTotalElements(response.totalElements ?? 0);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load SOOP subscriptions.');
      setSubscriptions([]);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSubscriptions();
  }, [loadSubscriptions]);

  async function handleDelete(subscriptionId: number) {
    if (!window.confirm('Delete this SOOP subscription?')) {
      return;
    }
    try {
      await apiDelete(`/soop/subscriptions/${subscriptionId}`);
      await loadSubscriptions();
    } catch {
      setError('Failed to delete SOOP subscription.');
    }
  }

  return (
    <PagePlaceholder title="SOOP Subscriptions" description="Notify Discord when selected SOOP channels go live or offline.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="d-flex align-items-center mb-3">
        <span className="small text-secondary">{totalElements} SOOP subscription(s)</span>
        <Link className="btn btn-primary ms-auto" to="/soop/subscriptions/new">
          New SOOP subscription
        </Link>
      </div>
      {loading ? (
        <div className="text-center py-4">Loading…</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>SOOP Channel</th>
                <th>Status</th>
                <th>Notify</th>
                <th>Discord</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {subscriptions.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center text-secondary">
                    No SOOP subscriptions found.
                  </td>
                </tr>
              ) : (
                subscriptions.map((subscription) => (
                  <tr key={subscription.id}>
                    <td>{subscription.id}</td>
                    <td>
                      <div className="fw-semibold">{subscription.soopChannelName}</div>
                      <div className="small text-secondary">{subscription.soopUserId}</div>
                      {subscription.liveUrl ? (
                        <a className="small" href={subscription.liveUrl} target="_blank" rel="noreferrer">
                          Open live page
                        </a>
                      ) : null}
                    </td>
                    <td>
                      <span className={`badge ${subscription.live ? 'text-bg-success' : 'text-bg-secondary'}`}>
                        {subscription.live ? 'Live' : 'Offline'}
                      </span>
                      {subscription.liveTitle ? <div className="small mt-1">{subscription.liveTitle}</div> : null}
                    </td>
                    <td>
                      <div className="small">Start: {subscription.notifyOnline ? 'Yes' : 'No'}</div>
                      <div className="small">End: {subscription.notifyOffline ? 'Yes' : 'No'}</div>
                    </td>
                    <td>
                      <div className="small">Webhook #{subscription.webhookId}</div>
                      <div className="small">Bot #{subscription.botProfileId}</div>
                    </td>
                    <td>
                      <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => void handleDelete(subscription.id)}>
                        Delete
                      </button>
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
