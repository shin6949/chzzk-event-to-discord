import {Link, useSearchParams} from 'react-router-dom';
import {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {ApiError, apiDelete, apiGet} from '../../api/client';
import {ConfirmDeleteModal} from '../../components/ConfirmDeleteModal';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import {SetupChecklist} from '../../components/SetupChecklist';
import {getSubscriptionTypeLabel} from './subscriptionFormOptions';

/**
 * `SortKey`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type SortKey = 'createdAt,desc' | 'createdAt,asc';
/**
 * `SortOptions`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type SortOptions = { value: SortKey; labelKey: string }[];

/**
 * `SubscriptionItem`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type SubscriptionItem = {
  id: number;
  channelId: string;
  subscriptionType: string;
  enabled: boolean;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
  createdAt: string;
  lastNotificationSentAt?: string | null;
};

/**
 * `SubscriptionPage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type SubscriptionPage = {
  content: SubscriptionItem[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

/**
 * `ResourcePage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
type ResourcePage<T> = {
  content: T[];
};

/**
 * `ResourceOption`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
type ResourceOption = {
  id: number;
  alias: string;
};

const sortOptions: SortOptions = [
  {value: 'createdAt,desc', labelKey: 'subscriptions.sortOptions.newest'},
  {value: 'createdAt,asc', labelKey: 'subscriptions.sortOptions.oldest'},
];

/**
 * `parsePageNumber`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function parsePageNumber(value: string | null, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

/**
 * `parsePageSize`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function parsePageSize(value: string | null, fallback: number): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.min(100, Math.max(1, parsed));
}

/**
 * `parseSort`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function parseSort(value: string | null): SortKey {
  const next = sortOptions.some((option) => option.value === value) ? value : null;
  return (next ?? 'createdAt,desc') as SortKey;
}

/**
 * `localeForLanguage`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function localeForLanguage(language: string): string {
  return language === 'ko' || language.startsWith('ko-') ? 'ko-KR' : 'en-US';
}

/**
 * `formatDateTime`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function formatDateTime(value: string | null | undefined, fallback: string, formatter: Intl.DateTimeFormat): string {
  if (!value) {
    return fallback;
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : formatter.format(date);
}

/**
 * `createResourceMap`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function createResourceMap(resources: ResourceOption[]): Map<number, string> {
  return new Map(resources.map((resource) => [resource.id, resource.alias]));
}

/**
 * `SubscriptionsPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function SubscriptionsPage() {
  const {t, i18n} = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parsePageNumber(searchParams.get('page'), 0);
  const size = parsePageSize(searchParams.get('size'), 10);
  const sort = parseSort(searchParams.get('sort'));
  const [subscriptions, setSubscriptions] = useState<SubscriptionItem[]>([]);
  const [webhookNames, setWebhookNames] = useState<Map<number, string>>(new Map());
  const [botProfileNames, setBotProfileNames] = useState<Map<number, string>>(new Map());
  const [loading, setLoading] = useState(true);
  const [resourceNamesLoading, setResourceNamesLoading] = useState(false);
  const [error, setError] = useState('');
  const [resourceError, setResourceError] = useState('');
  const [pendingDelete, setPendingDelete] = useState<SubscriptionItem | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [pageState, setPageState] = useState({
    totalPages: 1,
    totalElements: 0,
    first: true,
    last: true,
  });
  const dateTimeFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat(localeForLanguage(i18n.resolvedLanguage ?? i18n.language), {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      }),
    [i18n.language, i18n.resolvedLanguage],
  );

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', String(size));
    params.set('sort', sort);
    return params.toString();
  }, [page, size, sort]);

  const loadSubscriptions = useCallback(async () => {
    setLoading(true);
    setResourceNamesLoading(false);
    setError('');
    setResourceError('');

    try {
      const response = await apiGet<SubscriptionPage>(`/subscriptions?${query}`);
      const nextSubscriptions = response.content ?? [];
      setSubscriptions(nextSubscriptions);
      setPageState({
        totalPages: response.totalPages ?? 1,
        totalElements: response.totalElements ?? 0,
        first: response.first ?? page === 0,
        last: response.last ?? response.number + 1 >= (response.totalPages ?? 1),
      });

      if (nextSubscriptions.length === 0) {
        setWebhookNames(new Map());
        setBotProfileNames(new Map());
        setResourceNamesLoading(false);
        return;
      }

      setResourceNamesLoading(true);
      try {
        const [webhookResponse, botProfileResponse] = await Promise.all([
          apiGet<ResourcePage<ResourceOption>>('/discord/webhooks?size=100&sort=id,desc'),
          apiGet<ResourcePage<ResourceOption>>('/discord/bot-profiles?size=100&sort=id,desc'),
        ]);
        setWebhookNames(createResourceMap(webhookResponse.content ?? []));
        setBotProfileNames(createResourceMap(botProfileResponse.content ?? []));
      } catch {
        setWebhookNames(new Map());
        setBotProfileNames(new Map());
        setResourceError(t('subscriptions.resourceNamesError'));
      } finally {
        setResourceNamesLoading(false);
      }
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('subscriptions.loadError');
      setError(message);
      setSubscriptions([]);
      setWebhookNames(new Map());
      setBotProfileNames(new Map());
      setResourceNamesLoading(false);
      setPageState({totalPages: 1, totalElements: 0, first: true, last: true});
    } finally {
      setLoading(false);
    }
  }, [query, page, t]);

  useEffect(() => {
    void loadSubscriptions();
  }, [loadSubscriptions]);

  /**
   * `updateQuery`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
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

  /**
   * `webhookLabel`는 관련 프론트엔드 기능을 수행합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function webhookLabel(webhookId: number): string {
    return webhookNames.get(webhookId) ?? t('resources.webhookFallback', {id: webhookId});
  }

  /**
   * `botProfileLabel`는 관련 프론트엔드 기능을 수행합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function botProfileLabel(botProfileId: number): string {
    return botProfileNames.get(botProfileId) ?? t('resources.botProfileFallback', {id: botProfileId});
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
      await apiDelete(`/subscriptions/${pendingDelete.id}`);
      setPendingDelete(null);
      await loadSubscriptions();
    } catch {
      setError(t('subscriptions.deleteError'));
    } finally {
      setDeleting(false);
    }
  }

  return (
    <PagePlaceholder
      title={t('subscriptions.title')}
      description={t('subscriptions.description')}
      actions={
        <Link to="/subscriptions/new" className="btn btn-primary">
          <i className="bi bi-plus-lg me-1" aria-hidden="true" />
          {t('subscriptions.create')}
        </Link>
      }
    >
      <SetupChecklist
        currentStep="subscription"
        completed={{
          webhook: webhookNames.size > 0,
          botProfile: botProfileNames.size > 0,
          subscription: subscriptions.length > 0,
        }}
        returnTo="/subscriptions/new"
      />
      {error ? (
        <div className="alert alert-danger d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{error}</span>
          <div className="d-flex flex-wrap gap-2">
            <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadSubscriptions()}>
              {t('common.retry')}
            </button>
            <Link to="/discord/webhooks" className="btn btn-sm btn-outline-dark">
              {t('subscriptions.checkDiscordResources')}
            </Link>
          </div>
        </div>
      ) : null}
      {resourceError ? (
        <div className="alert alert-warning d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mt-3">
          <span>{resourceError}</span>
          <button type="button" className="btn btn-sm btn-outline-dark" onClick={() => void loadSubscriptions()}>
            {t('common.retryNames')}
          </button>
        </div>
      ) : null}
      <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
        <label className="form-label mb-0 small text-secondary d-flex align-items-center gap-2">
          {t('subscriptions.sort')}
          <select
            className="form-select form-select-sm"
            value={sort}
            onChange={(event) => updateQuery({page: 0, sort: event.target.value as SortKey})}
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {t(option.labelKey)}
              </option>
            ))}
          </select>
        </label>
        <label className="form-label mb-0 small text-secondary d-flex align-items-center gap-2">
          {t('subscriptions.size')}
          <select
            className="form-select form-select-sm"
            value={size}
            onChange={(event) => updateQuery({page: 0, size: Number(event.target.value)})}
          >
            {[5, 10, 20, 50].map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>
        <span className="small text-secondary ms-auto">
          {resourceNamesLoading ? t('subscriptions.loadingNames') : t('subscriptions.itemCount', {count: pageState.totalElements})}
        </span>
      </div>

      {loading ? (
        <div className="text-center py-4">{t('common.loading')}</div>
      ) : subscriptions.length === 0 ? (
        <div className="empty-state py-4 text-center text-secondary">
          <p className="mb-2">{t('subscriptions.empty')}</p>
          <Link to="/subscriptions/new" className="btn btn-sm btn-primary">
            {t('subscriptions.create')}
          </Link>
        </div>
      ) : (
        <>
        <div className="table-responsive d-none d-md-block">
          <table className="table table-hover align-middle text-nowrap">
            <thead>
              <tr>
                <th>{t('subscriptions.type')}</th>
                <th>{t('subscriptions.table.enabled')}</th>
                <th>{t('subscriptions.webhook')}</th>
                <th>{t('subscriptions.botProfile')}</th>
                <th>{t('subscriptions.interval')}</th>
                <th>{t('subscriptions.table.createdAt')}</th>
                <th>{t('subscriptions.table.lastNotification')}</th>
                <th className="text-end">{t('common.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {subscriptions.map((subscription) => (
                  <tr key={subscription.id}>
                    <td>
                      {getSubscriptionTypeLabel(subscription.subscriptionType, t)}
                    </td>
                    <td>{subscription.enabled ? t('common.yes') : t('common.no')}</td>
                    <td>{webhookLabel(subscription.webhookId)}</td>
                    <td>{botProfileLabel(subscription.botProfileId)}</td>
                    <td>{subscription.intervalMinute}</td>
                    <td>{formatDateTime(subscription.createdAt, t('common.unknown'), dateTimeFormatter)}</td>
                    <td>{formatDateTime(subscription.lastNotificationSentAt, t('common.never'), dateTimeFormatter)}</td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <Link
                          className="btn btn-outline-primary"
                          to={`/subscriptions/${subscription.id}`}
                          aria-label={t('subscriptions.editAria', {id: subscription.id})}
                        >
                          <i className="bi bi-pencil" aria-hidden="true" />
                        </Link>
                        <button
                          type="button"
                          className="btn btn-outline-danger"
                          onClick={() => setPendingDelete(subscription)}
                          aria-label={t('subscriptions.deleteAria', {id: subscription.id})}
                        >
                          <i className="bi bi-trash" aria-hidden="true" />
                        </button>
                      </div>
                    </td>
                  </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="mobile-resource-list d-md-none">
          {subscriptions.map((subscription) => (
              <div className="mobile-resource-item" key={subscription.id}>
                <div className="d-flex justify-content-between gap-3">
                  <div>
                    <div className="fw-semibold">{getSubscriptionTypeLabel(subscription.subscriptionType, t)}</div>
                    <div className="small text-secondary">{subscription.enabled ? t('common.enabled') : t('common.paused')}</div>
                  </div>
                  <div className="btn-group btn-group-sm">
                    <Link className="btn btn-outline-primary" to={`/subscriptions/${subscription.id}`}>
                      {t('common.edit')}
                    </Link>
                    <button type="button" className="btn btn-outline-danger" onClick={() => setPendingDelete(subscription)}>
                      {t('common.delete')}
                    </button>
                  </div>
                </div>
                <dl className="row small mb-0 mt-3">
                  <dt className="col-5">{t('subscriptions.webhook')}</dt>
                  <dd className="col-7">{webhookLabel(subscription.webhookId)}</dd>
                  <dt className="col-5">{t('subscriptions.botProfile')}</dt>
                  <dd className="col-7">{botProfileLabel(subscription.botProfileId)}</dd>
                  <dt className="col-5">{t('subscriptions.interval')}</dt>
                  <dd className="col-7">{t('subscriptions.intervalMinuteValue', {count: subscription.intervalMinute})}</dd>
                  <dt className="col-5">{t('subscriptions.table.lastSent')}</dt>
                  <dd className="col-7">{formatDateTime(subscription.lastNotificationSentAt, t('common.never'), dateTimeFormatter)}</dd>
                </dl>
              </div>
          ))}
        </div>
        </>
      )}

      <div className="d-flex justify-content-between align-items-center">
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() => updateQuery({page: Math.max(0, page - 1)})}
          disabled={loading || pageState.first}
        >
          {t('subscriptions.pagination.previous')}
        </button>
        <span className="small text-secondary">
          {t('subscriptions.pagination.page', {current: page + 1, total: Math.max(pageState.totalPages, 1)})}
        </span>
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() => updateQuery({page: page + 1})}
          disabled={loading || pageState.last}
        >
          {t('subscriptions.pagination.next')}
        </button>
      </div>
      <ConfirmDeleteModal
        open={Boolean(pendingDelete)}
        title={t('subscriptions.deleteModal.title')}
        message={t('subscriptions.deleteModal.listMessage', {
          name: pendingDelete ? getSubscriptionTypeLabel(pendingDelete.subscriptionType, t) : t('subscriptions.deleteModal.fallbackName'),
        })}
        busy={deleting}
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => void confirmDelete()}
      />
    </PagePlaceholder>
  );
}
