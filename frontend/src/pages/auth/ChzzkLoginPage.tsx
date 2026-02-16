import { useState } from 'react';
import { Link } from 'react-router-dom';
import { PagePlaceholder } from '../../components/PagePlaceholder';
import { useAuth } from '../../auth/AuthContext';
import { apiGet, ApiError } from '../../api/client';

export function ChzzkLoginPage() {
  const { session, loading, reloadSession } = useAuth();
  const [isSubmitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (session) {
    return (
      <PagePlaceholder title="Already logged in" description="You are already authenticated.">
        <div className="d-flex flex-wrap gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => void reloadSession()}>
            Refresh session
          </button>
          <Link className="btn btn-primary" to="/subscriptions">
            Go to subscriptions
          </Link>
        </div>
      </PagePlaceholder>
    );
  }

  if (loading) {
    return <div className="text-center py-5">Checking authentication state...</div>;
  }

  async function handleStartLogin() {
    setSubmitting(true);
    setError('');

    try {
      const response = await apiGet<{ authorizationUrl: string }>('/auth/chzzk/login');
      window.location.href = response.authorizationUrl;
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to start login.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PagePlaceholder title="Login" description="Sign in with Chzzk to continue.">
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="d-flex flex-wrap gap-2">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void handleStartLogin()}
          disabled={isSubmitting}
        >
          Login with Chzzk
        </button>
      </div>
    </PagePlaceholder>
  );
}
