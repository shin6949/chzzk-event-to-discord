package me.cocoblue.chzzkeventtodiscord.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import me.cocoblue.chzzkeventtodiscord.config.AppSecretProperties;
import me.cocoblue.chzzkeventtodiscord.config.ChzzkOAuthProperties;
import org.junit.jupiter.api.Test;

/**
 * {@code SecurityStartupValidatorTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
class SecurityStartupValidatorTests {

  /**
   * {@code validateSecurityConfigurationAcceptsHttpsEndpointsAndLaxCookies}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void validateSecurityConfigurationAcceptsHttpsEndpointsAndLaxCookies() {
    final SecurityStartupValidator validator =
        validator(
            "https://chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://example.com/api/v1/auth/chzzk/callback",
            false,
            "Lax");

    assertThatCode(validator::validateSecurityConfiguration).doesNotThrowAnyException();
  }

  /**
   * {@code validateSecurityConfigurationAcceptsHttpLoopbackEndpointsForLocalDevelopment}은 조건 충족 여부를
   * 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void validateSecurityConfigurationAcceptsHttpLoopbackEndpointsForLocalDevelopment() {
    final SecurityStartupValidator validator =
        validator(
            "http://localhost:3000",
            "http://127.0.0.1:8080",
            "http://[::1]:8080",
            "http://localhost:8080/api/v1/auth/chzzk/callback",
            false,
            "Lax");

    assertThatCode(validator::validateSecurityConfiguration).doesNotThrowAnyException();
  }

  /**
   * {@code validateSecurityConfigurationRejectsHttpNonLoopbackEndpoints}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void validateSecurityConfigurationRejectsHttpNonLoopbackEndpoints() {
    final SecurityStartupValidator validator =
        validator(
            "https://chzzk.naver.com",
            "http://openapi.chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://example.com/api/v1/auth/chzzk/callback",
            false,
            "Lax");

    assertThatThrownBy(validator::validateSecurityConfiguration)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("chzzk.oauth.token-base-url must use https");
  }

  /**
   * {@code validateSecurityConfigurationRejectsSameSiteNoneWithoutSecureCookie}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void validateSecurityConfigurationRejectsSameSiteNoneWithoutSecureCookie() {
    final SecurityStartupValidator validator =
        validator(
            "https://chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://example.com/api/v1/auth/chzzk/callback",
            false,
            "None");

    assertThatThrownBy(validator::validateSecurityConfiguration)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cookie-secure must be true");
  }

  /**
   * {@code validateSecurityConfigurationRejectsShortEncryptionKey}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void validateSecurityConfigurationRejectsShortEncryptionKey() {
    final AppSecretProperties secretProperties = new AppSecretProperties();
    secretProperties.setEncryptionKey("short-key");
    final AppJwtProperties jwtProperties = new AppJwtProperties();
    final ChzzkOAuthProperties oauthProperties =
        oauthProperties(
            "https://chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://openapi.chzzk.naver.com",
            "https://example.com/api/v1/auth/chzzk/callback");
    final SecurityStartupValidator validator =
        new SecurityStartupValidator(secretProperties, jwtProperties, oauthProperties);

    assertThatThrownBy(validator::validateSecurityConfiguration)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("encryption-key must be at least 32 bytes");
  }

  /**
   * {@code validator}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static SecurityStartupValidator validator(
      String authBaseUrl,
      String tokenBaseUrl,
      String apiBaseUrl,
      String redirectUri,
      boolean cookieSecure,
      String sameSite) {
    final AppSecretProperties secretProperties = new AppSecretProperties();
    secretProperties.setEncryptionKey("0123456789abcdef0123456789abcdef");
    final AppJwtProperties jwtProperties = new AppJwtProperties();
    jwtProperties.setCookieSecure(cookieSecure);
    jwtProperties.setCookieSameSite(sameSite);
    return new SecurityStartupValidator(
        secretProperties,
        jwtProperties,
        oauthProperties(authBaseUrl, tokenBaseUrl, apiBaseUrl, redirectUri));
  }

  /**
   * {@code oauthProperties}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private static ChzzkOAuthProperties oauthProperties(
      String authBaseUrl, String tokenBaseUrl, String apiBaseUrl, String redirectUri) {
    final ChzzkOAuthProperties properties = new ChzzkOAuthProperties();
    properties.setAuthBaseUrl(authBaseUrl);
    properties.setTokenBaseUrl(tokenBaseUrl);
    properties.setApiBaseUrl(apiBaseUrl);
    properties.setRedirectUri(redirectUri);
    return properties;
  }
}
