import { Link } from 'react-router-dom';

export function LandingPage() {
  return (
    <section className="p-4 p-md-5 mb-4 bg-white rounded-3 border shadow-sm">
      <div className="container-fluid py-2">
        <h1 className="display-6 fw-bold">Chzzk Event to Discord</h1>
        <p className="col-md-9 fs-5 text-secondary">
          Public landing page scaffold. Use this entry point to begin Chzzk OAuth and navigate to app/admin
          sections.
        </p>
        <div className="d-flex flex-wrap gap-2">
          <Link to="/auth/chzzk/login" className="btn btn-primary">
            Login with Chzzk
          </Link>
          <Link to="/app/dashboard" className="btn btn-outline-secondary">
            Go to Dashboard
          </Link>
        </div>
      </div>
    </section>
  );
}
