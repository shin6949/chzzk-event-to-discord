import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="text-center py-5">
      <h1 className="display-6">Page Not Found</h1>
      <p className="text-secondary">The requested route does not exist in this scaffold.</p>
      <Link to="/" className="btn btn-outline-primary">
        Return Home
      </Link>
    </section>
  );
}
