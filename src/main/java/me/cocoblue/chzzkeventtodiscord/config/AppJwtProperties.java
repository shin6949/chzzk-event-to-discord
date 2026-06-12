package me.cocoblue.chzzkeventtodiscord.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {@code AppJwtProperties}는 Spring 설정 값을 바인딩합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth.jwt")
public class AppJwtProperties {
  private String secret = "local-development-jwt-secret-change-me-32-bytes-minimum";
  private String issuer = "streaming-alert-service";
  private Duration accessTokenTtl = Duration.ofHours(1);
  private Duration refreshTokenTtl = Duration.ofDays(30);
  private Duration oauthStateTtl = Duration.ofMinutes(5);
  private String accessCookieName = "streaming_alert_access";
  private String refreshCookieName = "streaming_alert_refresh";
  private String oauthStateCookieName = "streaming_alert_oauth_state";
  private boolean cookieSecure = false;
  private String cookieSameSite = "Lax";
}
