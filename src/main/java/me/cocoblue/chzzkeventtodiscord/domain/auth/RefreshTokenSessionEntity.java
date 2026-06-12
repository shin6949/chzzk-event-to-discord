package me.cocoblue.chzzkeventtodiscord.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * {@code RefreshTokenSessionEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "refresh_token_session")
public class RefreshTokenSessionEntity {
  @Id
  @Column(name = "token_id", nullable = false, length = 64)
  private String tokenId;

  @Column(name = "channel_id", nullable = false, length = 100)
  private String channelId;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private AppRole role;

  @Column(name = "expires_at", nullable = false)
  private ZonedDateTime expiresAt;

  @Column(name = "revoked_at")
  private ZonedDateTime revokedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private ZonedDateTime updatedAt;
}
