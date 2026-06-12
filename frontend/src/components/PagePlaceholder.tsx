import type {ReactNode} from 'react';

/**
 * `PagePlaceholderProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
type PagePlaceholderProps = {
  title: string;
  description: string;
  actions?: ReactNode;
  children?: ReactNode;
};

/**
 * `PagePlaceholder`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function PagePlaceholder({title, description, actions, children}: PagePlaceholderProps) {
  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <div className="page-placeholder-header d-flex flex-column flex-md-row align-items-md-start justify-content-md-between gap-3">
          <div>
            <h1 className="h4 card-title mb-3">{title}</h1>
            <p className="card-text text-secondary mb-0">{description}</p>
          </div>
          {actions ? <div className="page-placeholder-actions d-flex flex-wrap gap-2">{actions}</div> : null}
        </div>
        {children ? <div className="mt-3">{children}</div> : null}
      </div>
    </section>
  );
}
