import { NavLink, Outlet } from 'react-router-dom';

type NavItem = {
  to: string;
  label: string;
};

type SectionLayoutProps = {
  title: string;
  navItems: NavItem[];
};

function sidebarClass({ isActive }: { isActive: boolean }): string {
  return `list-group-item list-group-item-action${isActive ? ' active' : ''}`;
}

export function SectionLayout({ title, navItems }: SectionLayoutProps) {
  return (
    <div className="row g-4">
      <aside className="col-12 col-lg-3">
        <h2 className="h5 mb-3">{title}</h2>
        <div className="list-group shadow-sm">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} className={sidebarClass}>
              {item.label}
            </NavLink>
          ))}
        </div>
      </aside>
      <div className="col-12 col-lg-9">
        <Outlet />
      </div>
    </div>
  );
}
