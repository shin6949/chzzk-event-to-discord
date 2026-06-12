import {useTranslation} from 'react-i18next';
import {Link} from 'react-router-dom';

/**
 * `NotFoundPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function NotFoundPage() {
  const {t} = useTranslation();

  return (
    <section className="text-center py-5">
      <h1 className="display-6">{t('notFound.title')}</h1>
      <p className="text-secondary">{t('notFound.description')}</p>
      <Link to="/" className="btn btn-outline-primary">
        {t('notFound.returnHome')}
      </Link>
    </section>
  );
}
