import { useNavigate, useSearchParams } from 'react-router-dom';
import { AppRole, setMockSession } from '../../auth/session';
import { PagePlaceholder } from '../../components/PagePlaceholder';

function parseMockRole(value: string | null): AppRole | null {
  if (value === 'USER' || value === 'ADMIN') {
    return value;
  }
  return null;
}

export function ChzzkCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const mockRole = parseMockRole(searchParams.get('mockRole'));

  function handleCompleteSignIn() {
    if (!mockRole) {
      return;
    }
    setMockSession(mockRole);
    navigate('/app/dashboard');
  }

  return (
    <PagePlaceholder
      title="Chzzk OAuth Callback"
      description="This page will process OAuth callback status and finalize app session bootstrap."
    >
      {mockRole ? (
        <div className="d-flex align-items-center gap-2">
          <span className="badge text-bg-info">Mock role: {mockRole}</span>
          <button type="button" className="btn btn-primary" onClick={handleCompleteSignIn}>
            Complete sign in
          </button>
        </div>
      ) : (
        <p className="mb-0 text-muted">
          Callback parameters are missing. Retry login from <code>/auth/chzzk/login</code>.
        </p>
      )}
    </PagePlaceholder>
  );
}
