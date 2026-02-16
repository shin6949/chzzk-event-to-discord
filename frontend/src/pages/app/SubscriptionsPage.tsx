import { Link, useSearchParams } from 'react-router-dom';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { ApiError, apiDelete, apiGet } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type SortKey = 'id,desc' | 'id,asc' | 'createdAt,desc' | 'createdAt,asc';
type SortOptions = { value: SortKey; label: string }[];

type SubscriptionItem = {
  id: number;
  channelId: string;
  subscriptionType: string;
  enabled: boolean;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  createdAt: string;
};

type SubscriptionPage = {
  content: SubscriptionItem[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

const sortOptions: SortOptions = [
  { value: 'id,desc', label: 'ID (newest)' },
  { value: 'id,asc', label: 'ID (oldest)' },
  { value: 'createdAt,desc', label: 'Created at (newest)' },
  { value: 'createdAt,asc', label: 'Created at (oldest)' },
];

function parsePageNumber(value: string | null, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

function parsePageSize(value: string | null, fallback: number): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(100, Math.max(1, parsed));
}

function parseSort(value: string | null): SortKey {
  const next = sortOptions.some((option) => option.value === value) ? value : null;
  return (next ?? 'id,desc') as SortKey;
}

export function SubscriptionsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parsePageNumber(searchParams.get('page'), 0);
  const size = parsePageSize(searchParams.get('size'), 10);
  const sort = parseSort(searchParams.get('sort'));
  const [subscriptions, setSubscriptions] = useState<SubscriptionItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pageState, setPageState] = useState({
    totalPages: 1,
    totalElements: 0,
    first: true,
    last: true,
  });

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', String(size));
    params.set('sort', sort);
    return params.toString();
  }, [page, size, sort]);

  const loadSubscriptions = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const response = await apiGet<SubscriptionPage>(`/subscriptions?${query}`);
      setSubscriptions(response.content ?? []);
      setPageState({
        totalPages: response.totalPages ?? 1,
        totalElements: response.totalElements ?? 0,
        first: response.first ?? page === 0,
        last: response.last ?? response.number + 1 >= (response.totalPages ?? 1),
      });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load subscriptions.';
      setError(message);
      setSubscriptions([]);
      setPageState({ totalPages: 1, totalElements: 0, first: true, last: true });
    } finally {
      setLoading(false);
    }
  }, [query, page, size]);

  useEffect(() => {
    void loadSubscriptions();
  }, [loadSubscriptions]);

  const updateQuery = (next: Partial<{ page: number; size: number; sort: SortKey }>) => {
    const params = new URLSearchParams(searchParams);
    if (next.page !== undefined) {
      params.set('page', String(next.page));
    }
    if (next.size !== undefined) {
      params.set('size', String(next.size));
    }
    if (next.sort !== undefined) {
      params.set('sort', next.sort);
    }
    setSearchParams(params);
  };

  const handleDelete = async (subscriptionId: number) => {
    if (!window.confirm('Delete this subscription?')) {
      return;
    }

    try {
      await apiDelete(`/subscriptions/${subscriptionId}`);
      await loadSubscriptions();
    } catch {
      setError('Failed to delete subscription.');
    }
  };

  return (
    <PagePlaceholder title="Subscriptions" description="Manage your notification subscriptions.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
        <label className="form-label mb-0 small text-secondary d-flex align-items-center gap-2">
          Sort
          <select
            className="form-select form-select-sm"
            value={sort}
            onChange={(event) => updateQuery({ page: 0, sort: event.target.value as SortKey })}
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label className="form-label mb-0 small text-secondary d-flex align-items-center gap-2">
          Size
          <select
            className="form-select form-select-sm"
            value={size}
            onChange={(event) => updateQuery({ page: 0, size: Number(event.target.value) })}
          >
            {[5, 10, 20, 50].map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>
        <span className="small text-secondary ms-auto">{pageState.totalElements} item(s)</span>
      </div>

      {loading ? (
        <div className="text-center py-4">Loading…</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead>
              <tr>
                <th>ID</th>
                <th>Channel</th>
                <th>Type</th>
                <th>Enabled</th>
                <th>Webhook</th>
                <th>Bot Profile</th>
                <th>Interval</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {subscriptions.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center text-secondary">
                    No subscriptions found.
                  </td>
                </tr>
              ) : (
                subscriptions.map((subscription) => (
                  <tr key={subscription.id}>
                    <td>{subscription.id}</td>
                    <td>{subscription.channelId}</td>
                    <td>
                      <code>{subscription.subscriptionType}</code>
                    </td>
                    <td>{subscription.enabled ? 'Yes' : 'No'}</td>
                    <td>{subscription.webhookId}</td>
                    <td>{subscription.botProfileId}</td>
                    <td>{subscription.intervalMinute}</td>
                    <td className="d-flex gap-2">
                      <Link className="btn btn-sm btn-outline-primary" to={`/subscriptions/${subscription.id}`}>
                        Edit
                      </Link>
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

      <div className="d-flex justify-content-between align-items-center">
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() => updateQuery({ page: Math.max(0, page - 1) })}
          disabled={loading || pageState.first}
        >
          Previous
        </button>
        <span className="small text-secondary">
          Page {page + 1} / {Math.max(pageState.totalPages, 1)}
        </span>
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() => updateQuery({ page: page + 1 })}
          disabled={loading || pageState.last}
        >
          Next
        </button>
      </div>
    </PagePlaceholder>
  );
}
