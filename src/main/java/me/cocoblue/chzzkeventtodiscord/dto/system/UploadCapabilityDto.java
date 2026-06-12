package me.cocoblue.chzzkeventtodiscord.dto.system;

import java.time.Instant;
import me.cocoblue.chzzkeventtodiscord.service.S3StorageAvailabilitySnapshot;

/**
 * {@code UploadCapabilityDto}는 API 요청/응답 데이터 전송 구조를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
public record UploadCapabilityDto(boolean enabled, String reason, Instant checkedAt) {
  /**
   * {@code fromStorageSnapshot}은 다른 데이터 형식으로 변환합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public static UploadCapabilityDto fromStorageSnapshot(S3StorageAvailabilitySnapshot snapshot) {
    return new UploadCapabilityDto(snapshot.available(), snapshot.reason(), snapshot.checkedAt());
  }
}
