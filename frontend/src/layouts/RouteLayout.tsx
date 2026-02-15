import { NavLink, Outlet } from 'react-router-dom';

type NavItem = {
  to: string;
  label: string;
};

type RouteLayoutProps = {
  title: string;
  subtitle: string;
  navItems: NavItem[];
};

function navClassName(isActive: boolean): string {
  return isActive ? 'nav-link active' : 'nav-link';
}

export function RouteLayout({ title, subtitle, navItems }: RouteLayoutProps) {
  return (
    <div className="container py-4">
      <header className="mb-4">
        <div className="d-flex align-items-center gap-2 mb-2">
          <i className="bi bi-diagram-3-fill" aria-hidden="true" />
          <h1 className="h3 mb-0">{title}</h1>
        </div>
        <p className="text-secondary mb-3">{subtitle}</p>
        <nav className="nav nav-pills flex-wrap gap-2" aria-label={`${title} navigation`}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => navClassName(isActive)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <Outlet />
    </div>
  );
}
