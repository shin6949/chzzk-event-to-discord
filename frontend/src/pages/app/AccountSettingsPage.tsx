import { env } from '../../config/env';
import { PagePlaceholder } from '../../components/PagePlaceholder';

export function AccountSettingsPage() {
  return (
    <PagePlaceholder
      title="Account Settings"
      description="Account linkage and disconnect UX placeholder for Chzzk OAuth profile state."
    >
      <div className="small text-muted">
        API base URL from environment: <code>{env.apiBaseUrl}</code>
      </div>
    </PagePlaceholder>
  );
}
