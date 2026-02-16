import { Link } from 'react-router-dom';
import { env } from '../../config/env';
import { PagePlaceholder } from '../../components/PagePlaceholder';

export function ChzzkLoginPage() {
  const backendLoginUrl = `${env.apiBaseUrl}/auth/chzzk/login`;

  return (
    <PagePlaceholder
      title="Chzzk Login"
      description="This route will trigger backend OAuth start at /api/v1/auth/chzzk/login."
    >
      <div className="d-flex flex-wrap gap-2">
        <a className="btn btn-primary" href={backendLoginUrl}>
          Login with Chzzk (Backend)
        </a>
        <Link className="btn btn-outline-secondary" to="/auth/chzzk/callback?mockRole=USER">
          Simulate USER callback
        </Link>
        <Link className="btn btn-outline-secondary" to="/auth/chzzk/callback?mockRole=ADMIN">
          Simulate ADMIN callback
        </Link>
      </div>
    </PagePlaceholder>
  );
}
