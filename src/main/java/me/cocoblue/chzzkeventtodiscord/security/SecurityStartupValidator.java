package me.cocoblue.chzzkeventtodiscord.security;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import me.cocoblue.chzzkeventtodiscord.config.AppSecretProperties;
import me.cocoblue.chzzkeventtodiscord.config.ChzzkOAuthProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * {@code SecurityStartupValidator}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Component
@RequiredArgsConstructor
public class SecurityStartupValidator {
  private static final int MIN_SECRET_BYTES = 32;
  private static final Set<String> ALLOWED_COOKIE_SAME_SITE_VALUES =
      Set.of("strict", "lax", "none");

  private final AppSecretProperties appSecretProperties;
  private final AppJwtProperties appJwtProperties;
  private final ChzzkOAuthProperties chzzkOAuthProperties;

  @Value("${app.is-test:false}")
  private boolean isTest;

  /**
   * {@code validateSecurityConfiguration}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostConstruct
  void validateSecurityConfiguration() {
    if (!StringUtils.hasText(appSecretProperties.getEncryptionKey())
        || appSecretProperties.getEncryptionKey().getBytes(StandardCharsets.UTF_8).length
            < MIN_SECRET_BYTES) {
      throw new IllegalStateException("app.secret.encryption-key must be at least 32 bytes");
    }
    validateCookieAttributes();
    validateOAuthEndpoint("chzzk.oauth.auth-base-url", chzzkOAuthProperties.getAuthBaseUrl());
    validateOAuthEndpoint("chzzk.oauth.token-base-url", chzzkOAuthProperties.getTokenBaseUrl());
    validateOAuthEndpoint("chzzk.oauth.api-base-url", chzzkOAuthProperties.getApiBaseUrl());
    validateOAuthEndpoint("chzzk.oauth.redirect-uri", chzzkOAuthProperties.getRedirectUri());
  }

  /**
   * {@code validateCookieAttributes}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private void validateCookieAttributes() {
    final String sameSite = appJwtProperties.getCookieSameSite();
    final String normalizedSameSite = sameSite == null ? "" : sameSite.toLowerCase(Locale.ROOT);
    if (!ALLOWED_COOKIE_SAME_SITE_VALUES.contains(normalizedSameSite)) {
      throw new IllegalStateException(
          "app.auth.jwt.cookie-same-site must be one of Strict, Lax, or None");
    }
    if ("none".equals(normalizedSameSite) && !appJwtProperties.isCookieSecure()) {
      throw new IllegalStateException("app.auth.jwt.cookie-secure must be true when SameSite=None");
    }
  }

  /**
   * {@code validateOAuthEndpoint}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private void validateOAuthEndpoint(String propertyName, String value) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalStateException(propertyName + " must not be empty");
    }

    final URI uri;
    try {
      uri = new URI(value.trim());
    } catch (URISyntaxException exception) {
      throw new IllegalStateException(propertyName + " must be a valid URI", exception);
    }
    if (!StringUtils.hasText(uri.getScheme())
        || !StringUtils.hasText(uri.getHost())
        || StringUtils.hasText(uri.getUserInfo())) {
      throw new IllegalStateException(
          propertyName + " must include scheme and host and must not include credentials");
    }
    if ("https".equalsIgnoreCase(uri.getScheme())) {
      return;
    }
    if ("http".equalsIgnoreCase(uri.getScheme()) && (isTest || isLoopbackHost(uri.getHost()))) {
      return;
    }

    throw new IllegalStateException(
        propertyName + " must use https except for test or loopback endpoints");
  }

  /**
   * {@code isLoopbackHost}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private boolean isLoopbackHost(String host) {
    final String normalizedHost = host.toLowerCase(Locale.ROOT);
    return "localhost".equals(normalizedHost)
        || "127.0.0.1".equals(normalizedHost)
        || "::1".equals(normalizedHost)
        || "[::1]".equals(normalizedHost);
  }
}
