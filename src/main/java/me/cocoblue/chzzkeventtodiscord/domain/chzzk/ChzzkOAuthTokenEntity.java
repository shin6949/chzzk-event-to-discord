package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * {@code ChzzkOAuthTokenEntity}는 데이터베이스 테이블 매핑을 표현합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "chzzk_oauth_token")
public class ChzzkOAuthTokenEntity {
  @Id
  @Column(name = "channel_id", nullable = false, length = 100)
  private String channelId;

  @Column(name = "access_token", nullable = false, length = 5000)
  private String accessToken;

  @Column(name = "refresh_token", length = 5000)
  private String refreshToken;

  @Column(name = "token_type", length = 30)
  private String tokenType;

  @Column(name = "scope", length = 1000)
  private String scope;

  @Column(name = "access_token_expires_at")
  private ZonedDateTime accessTokenExpiresAt;

  @Column(name = "refresh_token_expires_at")
  private ZonedDateTime refreshTokenExpiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private ZonedDateTime updatedAt;
}
