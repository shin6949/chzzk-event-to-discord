import {useTranslation} from 'react-i18next';

/**
 * `ConfirmDeleteModalProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type ConfirmDeleteModalProps = {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  busy?: boolean;
  onCancel: () => void;
  onConfirm: () => void;
};

/**
 * `ConfirmDeleteModal`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function ConfirmDeleteModal({
  open,
  title,
  message,
  confirmLabel,
  busy = false,
  onCancel,
  onConfirm,
}: ConfirmDeleteModalProps) {
  const {t} = useTranslation();

  if (!open) {
    return null;
  }

  return (
    <>
      <div className="modal fade show" role="dialog" aria-modal="true" aria-labelledby="confirmDeleteTitle" tabIndex={-1} style={{display: 'block'}}>
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content shadow">
            <div className="modal-header">
              <h1 className="modal-title fs-5" id="confirmDeleteTitle">
                {title}
              </h1>
              <button type="button" className="btn-close" aria-label={t('deleteModal.cancelAria')} onClick={onCancel} disabled={busy} />
            </div>
            <div className="modal-body">
              <p className="mb-0">{message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onCancel} disabled={busy}>
                {t('common.cancel')}
              </button>
              <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={busy}>
                {busy ? t('common.deleting') : confirmLabel ?? t('common.delete')}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show" />
    </>
  );
}
