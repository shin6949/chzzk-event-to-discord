import { FormEvent, useCallback, useEffect, useState } from 'react';
import { ApiError, apiDelete, apiGet, apiPost } from '../../api/client';

type YouTubeSubscriptionType = 'LIVE_STARTED' | 'LIVE_ENDED' | 'VIDEO_UPLOADED';

type DiscordResource = { id: number; name?: string; alias?: string; username?: string };

type YouTubeSubscription = {
  id: number;
  youtubeChannelId: string;
  youtubeChannelTitle: string;
  youtubeChannelThumbnailUrl?: string;
  type: YouTubeSubscriptionType;
  enabled: boolean;
  webhookId: number;
  botProfileId: number;
  intervalMinute: number;
};

type PageResponse<T> = { content: T[] };

const subscriptionTypes: { value: YouTubeSubscriptionType; label: string; description: string }[] = [
  { value: 'LIVE_STARTED', label: 'Live start', description: '지정 채널의 라이브 스트리밍 시작을 알립니다.' },
  { value: 'LIVE_ENDED', label: 'Live end', description: '지정 채널의 라이브 스트리밍 종료를 알립니다.' },
  { value: 'VIDEO_UPLOADED', label: 'New video', description: '지정 채널의 새 영상 업로드를 알립니다.' },
];

export function YouTubeSubscriptionsPage() {
  const [items, setItems] = useState<YouTubeSubscription[]>([]);
  const [webhooks, setWebhooks] = useState<DiscordResource[]>([]);
  const [botProfiles, setBotProfiles] = useState<DiscordResource[]>([]);
  const [youtubeChannelId, setYoutubeChannelId] = useState('');
  const [type, setType] = useState<YouTubeSubscriptionType>('LIVE_STARTED');
  const [webhookId, setWebhookId] = useState('');
  const [botProfileId, setBotProfileId] = useState('');
  const [intervalMinute, setIntervalMinute] = useState('10');
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    const [subscriptionsPage, webhooksPage, botProfilesPage] = await Promise.all([
      apiGet<PageResponse<YouTubeSubscription>>('/youtube/subscriptions?sort=id,desc'),
      apiGet<PageResponse<DiscordResource>>('/discord/webhooks'),
      apiGet<PageResponse<DiscordResource>>('/discord/bot-profiles'),
    ]);
    setItems(subscriptionsPage.content);
    setWebhooks(webhooksPage.content);
    setBotProfiles(botProfilesPage.content);
    if (!webhookId && webhooksPage.content[0]) setWebhookId(String(webhooksPage.content[0].id));
    if (!botProfileId && botProfilesPage.content[0]) setBotProfileId(String(botProfilesPage.content[0].id));
  }, [botProfileId, webhookId]);

  useEffect(() => {
    load().catch((err: unknown) => {
      setError(err instanceof ApiError ? err.message : 'Failed to load YouTube subscriptions.');
    });
  }, [load]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      await apiPost('/youtube/subscriptions', {
        youtubeChannelId,
        type,
        webhookId: Number(webhookId),
        botProfileId: Number(botProfileId),
        intervalMinute: Number(intervalMinute),
        content: content || null,
        colorHex: 'ff0000',
      });
      setYoutubeChannelId('');
      setContent('');
      setMessage('YouTube subscription created.');
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to create YouTube subscription.');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number) {
    setError(null);
    await apiDelete(`/youtube/subscriptions/${id}`);
    setItems((current) => current.filter((item) => item.id !== id));
  }

  return (
    <div className="vstack gap-4">
      <div>
        <h2 className="h4 mb-1">YouTube subscriptions</h2>
        <p className="text-secondary mb-0">
          YouTube Data API v3로 특정 채널의 라이브 시작, 라이브 종료, 새 영상 알림을 Discord로 전송합니다.
        </p>
      </div>
      {error ? <div className="alert alert-danger">{error}</div> : null}
      {message ? <div className="alert alert-success">{message}</div> : null}
      <form className="card card-body row g-3" onSubmit={handleSubmit}>
        <div className="col-md-6">
          <label className="form-label" htmlFor="youtubeChannelId">YouTube channel ID</label>
          <input id="youtubeChannelId" className="form-control" value={youtubeChannelId} onChange={(event) => setYoutubeChannelId(event.target.value)} placeholder="UCxxxxxxxxxxxxxxxxxxxxxx" required />
          <div className="form-text">공식 YouTube Data API의 channel ID를 입력하세요.</div>
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="youtubeType">Event type</label>
          <select id="youtubeType" className="form-select" value={type} onChange={(event) => setType(event.target.value as YouTubeSubscriptionType)}>
            {subscriptionTypes.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
          </select>
          <div className="form-text">{subscriptionTypes.find((option) => option.value === type)?.description}</div>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="youtubeWebhook">Webhook</label>
          <select id="youtubeWebhook" className="form-select" value={webhookId} onChange={(event) => setWebhookId(event.target.value)} required>
            {webhooks.map((webhook) => <option key={webhook.id} value={webhook.id}>{webhook.name ?? `Webhook #${webhook.id}`}</option>)}
          </select>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="youtubeBotProfile">Bot profile</label>
          <select id="youtubeBotProfile" className="form-select" value={botProfileId} onChange={(event) => setBotProfileId(event.target.value)} required>
            {botProfiles.map((profile) => <option key={profile.id} value={profile.id}>{profile.alias ?? profile.username ?? `Bot #${profile.id}`}</option>)}
          </select>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="youtubeInterval">Duplicate guard (minutes)</label>
          <input id="youtubeInterval" className="form-control" type="number" min="1" value={intervalMinute} onChange={(event) => setIntervalMinute(event.target.value)} required />
        </div>
        <div className="col-12">
          <label className="form-label" htmlFor="youtubeContent">Discord message content</label>
          <input id="youtubeContent" className="form-control" value={content} onChange={(event) => setContent(event.target.value)} placeholder="Optional message outside the embed" />
        </div>
        <div className="col-12">
          <button className="btn btn-danger" type="submit" disabled={loading || !webhookId || !botProfileId}>{loading ? 'Creating…' : 'Create YouTube subscription'}</button>
        </div>
      </form>
      <div className="card">
        <div className="table-responsive">
          <table className="table align-middle mb-0">
            <thead><tr><th>Channel</th><th>Event</th><th>Interval</th><th>Status</th><th /></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td><div className="fw-semibold">{item.youtubeChannelTitle}</div><div className="small text-secondary">{item.youtubeChannelId}</div></td>
                  <td>{item.type}</td>
                  <td>{item.intervalMinute} min</td>
                  <td>{item.enabled ? 'Enabled' : 'Disabled'}</td>
                  <td className="text-end"><button className="btn btn-sm btn-outline-danger" type="button" onClick={() => void handleDelete(item.id)}>Delete</button></td>
                </tr>
              ))}
              {items.length === 0 ? <tr><td colSpan={5} className="text-center text-secondary py-4">No YouTube subscriptions yet.</td></tr> : null}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
