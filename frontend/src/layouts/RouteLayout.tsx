import {NavLink, Outlet} from 'react-router-dom';

/**
 * `NavItem`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
type NavItem = {
  to: string;
  label: string;
};

/**
 * `RouteLayoutProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
type RouteLayoutProps = {
  title: string;
  subtitle: string;
  navItems: NavItem[];
};

/**
 * `navClassName`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function navClassName(isActive: boolean): string {
  return isActive ? 'nav-link active' : 'nav-link';
}

/**
 * `RouteLayout`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function RouteLayout({title, subtitle, navItems}: RouteLayoutProps) {
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
              className={({isActive}) => navClassName(isActive)}
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
