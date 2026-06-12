import {useTranslation} from 'react-i18next';
import {PagePlaceholder} from '../../components/PagePlaceholder';

/**
 * `AdminAuditLogsPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function AdminAuditLogsPage() {
  const {t} = useTranslation();

  return <PagePlaceholder title={t('placeholders.adminAuditLogsTitle')} description={t('placeholders.adminAuditLogsDescription')} />;
}
