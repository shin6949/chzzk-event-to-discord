import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError, apiGet, apiPost } from '../../api/client';
import { PagePlaceholder } from '../../components/PagePlaceholder';

type ResourcePage<T> = { content: T[] };
type WebhookOption = { id: number; alias: string; url: string };
type BotProfileOption = { id: number; alias: string; username: string; avatarUrl: string };

type FormState = {
  soopUserId: string;
  soopChannelName: string;
  webhookId: string;
  botProfileId: string;
  enabled: boolean;
  notifyOnline: boolean;
  notifyOffline: boolean;
  content: string;
  colorHex: string;
};

const initialState: FormState = {
  soopUserId: '',
  soopChannelName: '',
  webhookId: '',
  botProfileId: '',
  enabled: true,
  notifyOnline: true,
  notifyOffline: true,
  content: '',
  colorHex: '9146FF',
};

export function NewSoopSubscriptionPage() {
  const navigate = useNavigate();
  const [formState, setFormState] = useState<FormState>(initialState);
  const [webhooks, setWebhooks] = useState<WebhookOption[]>([]);
  const [botProfiles, setBotProfiles] = useState<BotProfileOption[]>([]);
  const [resourcesLoading, setResourcesLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

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
      setError(err instanceof ApiError ? err.message : 'Unable to load Discord resources.');
    } finally {
      setResourcesLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadResources();
  }, [loadResources]);

  const formIsValid = useMemo(
    () => Boolean(formState.soopUserId.trim() && formState.webhookId.trim() && formState.botProfileId.trim()),
    [formState.botProfileId, formState.soopUserId, formState.webhookId],
  );

  function updateField(name: keyof FormState, value: string | boolean) {
    setFormState((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError('');

    const webhookId = Number(formState.webhookId);
    const botProfileId = Number(formState.botProfileId);
    if (!formIsValid || !Number.isFinite(webhookId) || !Number.isFinite(botProfileId)) {
      setError('Please enter a SOOP ID and choose Discord resources.');
      setSaving(false);
      return;
    }

    try {
      await apiPost('/soop/subscriptions', {
        soopUserId: formState.soopUserId.trim(),
        soopChannelName: formState.soopChannelName.trim(),
        webhookId,
        botProfileId,
        enabled: formState.enabled,
        notifyOnline: formState.notifyOnline,
        notifyOffline: formState.notifyOffline,
        content: formState.content,
        colorHex: formState.colorHex,
      });
      navigate('/soop/subscriptions');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to create SOOP subscription.');
    } finally {
      setSaving(false);
    }
  }

  const missingResources = !resourcesLoading && (webhooks.length === 0 || botProfiles.length === 0);

  return (
    <PagePlaceholder title="New SOOP Subscription" description="Use the official SOOP broadcast list API to detect live start and end events.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      {missingResources ? (
        <div className="alert alert-warning d-flex flex-wrap align-items-center gap-2">
          <span className="me-auto">Create at least one webhook and one bot profile before adding a SOOP subscription.</span>
          {webhooks.length === 0 ? <Link to="/discord/webhooks" className="btn btn-sm btn-outline-dark">Webhooks</Link> : null}
          {botProfiles.length === 0 ? <Link to="/discord/bot-profiles" className="btn btn-sm btn-outline-dark">Bot Profiles</Link> : null}
        </div>
      ) : null}
      <form className="row g-3 mt-1" onSubmit={handleSubmit}>
        <div className="col-md-6">
          <label className="form-label" htmlFor="soopUserId">SOOP user ID</label>
          <input id="soopUserId" className="form-control" value={formState.soopUserId} onChange={(e) => updateField('soopUserId', e.target.value)} placeholder="e.g. soop1234" required />
          <div className="form-text">The ID used in SOOP channel URLs, such as ch.sooplive.co.kr/&lt;id&gt;.</div>
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="soopChannelName">Display name (optional)</label>
          <input id="soopChannelName" className="form-control" value={formState.soopChannelName} onChange={(e) => updateField('soopChannelName', e.target.value)} placeholder="Fallback name while offline" />
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="webhookId">Discord webhook</label>
          <select id="webhookId" className="form-select" value={formState.webhookId} onChange={(e) => updateField('webhookId', e.target.value)} disabled={resourcesLoading} required>
            <option value="">Select webhook</option>
            {webhooks.map((webhook) => <option key={webhook.id} value={webhook.id}>{webhook.alias ?? webhook.url}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="botProfileId">Bot profile</label>
          <select id="botProfileId" className="form-select" value={formState.botProfileId} onChange={(e) => updateField('botProfileId', e.target.value)} disabled={resourcesLoading} required>
            <option value="">Select bot profile</option>
            {botProfiles.map((profile) => <option key={profile.id} value={profile.id}>{profile.alias ?? profile.username}</option>)}
          </select>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="colorHex">Embed color</label>
          <input id="colorHex" className="form-control" value={formState.colorHex} onChange={(e) => updateField('colorHex', e.target.value)} maxLength={7} />
        </div>
        <div className="col-12">
          <label className="form-label" htmlFor="content">Discord message content</label>
          <textarea id="content" className="form-control" value={formState.content} onChange={(e) => updateField('content', e.target.value)} rows={3} />
        </div>
        <div className="col-12 d-flex flex-wrap gap-4">
          <label className="form-check"><input className="form-check-input" type="checkbox" checked={formState.enabled} onChange={(e) => updateField('enabled', e.target.checked)} /> Enabled</label>
          <label className="form-check"><input className="form-check-input" type="checkbox" checked={formState.notifyOnline} onChange={(e) => updateField('notifyOnline', e.target.checked)} /> Notify live start</label>
          <label className="form-check"><input className="form-check-input" type="checkbox" checked={formState.notifyOffline} onChange={(e) => updateField('notifyOffline', e.target.checked)} /> Notify live end</label>
        </div>
        <div className="col-12 d-flex justify-content-end gap-2">
          <Link className="btn btn-outline-secondary" to="/soop/subscriptions">Cancel</Link>
          <button type="submit" className="btn btn-primary" disabled={saving || resourcesLoading || !formIsValid}>Create SOOP subscription</button>
        </div>
      </form>
    </PagePlaceholder>
  );
}
