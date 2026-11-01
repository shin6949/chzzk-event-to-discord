import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function navLinkClass({ isActive }: { isActive: boolean }) {
  return `nav-link${isActive ? ' active fw-semibold' : ''}`;
}

function formatChannelLabel(channelId: string): string {
  const truncated = channelId.trim();
  if (truncated.length <= 20) {
    return truncated;
  }
  return `${truncated.slice(0, 20)}…`;
}

export function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { session, logout } = useAuth();
  const displayName = session?.channelName?.trim() || (session ? formatChannelLabel(session.channelId) : '');
  const discordResourcesActive = location.pathname === '/discord' || location.pathname.startsWith('/discord/');
  const soopActive = location.pathname === '/soop' || location.pathname.startsWith('/soop/');

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <div className="d-flex flex-column min-vh-100">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
        <div className="container">
          <NavLink to="/" className="navbar-brand d-flex align-items-center gap-2">
            <i className="bi bi-broadcast-pin" aria-hidden="true" />
            <span>Chzzk Event to Discord</span>
          </NavLink>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#mainNav"
            aria-controls="mainNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon" />
          </button>
          <div className="collapse navbar-collapse" id="mainNav">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <NavLink to="/subscriptions" className={navLinkClass}>
                  Subscriptions
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/soop/subscriptions" className={({ isActive }) => navLinkClass({ isActive: isActive || soopActive })}>
                  SOOP
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/discord/webhooks" className={({ isActive }) => navLinkClass({ isActive: isActive || discordResourcesActive })}>
                  Discord Resources
                </NavLink>
              </li>
            </ul>
            <div className="d-flex align-items-center gap-2">
              {session ? (
                <>
                  <div className="channel-user-summary d-flex align-items-center gap-2">
                    <span className="channel-avatar" aria-hidden="true">
                      <i className="bi bi-person-fill" aria-hidden="true" />
                      {session.profileUrl ? (
                        <img
                          src={session.profileUrl}
                          alt=""
                          referrerPolicy="no-referrer"
                          onError={(event) => {
                            event.currentTarget.style.display = 'none';
                          }}
                        />
                      ) : null}
                    </span>
                    <span className="channel-name text-light small fw-semibold">{displayName}</span>
                  </div>
                  <button type="button" onClick={handleLogout} className="btn btn-outline-light btn-sm">
                    Logout
                  </button>
                </>
              ) : (
                <NavLink to="/login" className="btn btn-outline-light btn-sm">
                  Login with Chzzk
                </NavLink>
              )}
            </div>
          </div>
        </div>
      </nav>
      <main className="container py-4 flex-grow-1">
        <Outlet />
      </main>
    </div>
  );
}
