import {useTranslation} from 'react-i18next';
import {NavLink, Outlet} from 'react-router-dom';

/**
 * `NavItem`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
type NavItem = {
  to: string;
  labelKey: string;
  icon?: string;
  end?: boolean;
};

/**
 * `SectionLayoutProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
type SectionLayoutProps = {
  titleKey: string;
  icon?: string;
  navItems: NavItem[];
};

/**
 * `sidebarClass`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function sidebarClass({isActive}: { isActive: boolean }): string {
  return `list-group-item list-group-item-action${isActive ? ' active' : ''}`;
}

/**
 * `SectionLayout`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function SectionLayout({titleKey, icon, navItems}: SectionLayoutProps) {
  const {t} = useTranslation();

  return (
    <div className="row g-4">
      <aside className="col-12 col-lg-3">
        <h2 className="h5 mb-3 d-flex align-items-center gap-2">
          {icon ? <i className={`bi ${icon}`} aria-hidden="true" /> : null}
          <span>{t(titleKey)}</span>
        </h2>
        <div className="list-group shadow-sm">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end ?? true} className={sidebarClass}>
              <span className="d-flex align-items-center gap-2">
                {item.icon ? <i className={`bi ${item.icon}`} aria-hidden="true" /> : null}
                <span>{t(item.labelKey)}</span>
              </span>
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
