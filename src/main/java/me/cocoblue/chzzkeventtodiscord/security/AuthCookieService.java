package me.cocoblue.chzzkeventtodiscord.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * {@code AuthCookieService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Service
@RequiredArgsConstructor
public class AuthCookieService {
  private static final String ROOT_PATH = "/";
  private static final String AUTH_PATH = "/api/v1/auth";
  private static final String CHZZK_AUTH_PATH = "/api/v1/auth/chzzk";

  private final AppJwtProperties properties;

  /**
   * {@code resolveAccessToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public Optional<String> resolveAccessToken(HttpServletRequest request) {
    return resolveCookieValue(request, properties.getAccessCookieName());
  }

  /**
   * {@code resolveRefreshToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public Optional<String> resolveRefreshToken(HttpServletRequest request) {
    return resolveCookieValue(request, properties.getRefreshCookieName());
  }

  /**
   * {@code resolveOAuthState}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public Optional<String> resolveOAuthState(HttpServletRequest request) {
    return resolveCookieValue(request, properties.getOauthStateCookieName());
  }

  /**
   * {@code addAccessTokenCookie}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void addAccessTokenCookie(HttpServletResponse response, String token) {
    addCookie(
        response,
        buildCookie(
            properties.getAccessCookieName(), token, properties.getAccessTokenTtl(), ROOT_PATH));
  }

  /**
   * {@code addRefreshTokenCookie}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void addRefreshTokenCookie(HttpServletResponse response, String token) {
    addCookie(
        response,
        buildCookie(
            properties.getRefreshCookieName(), token, properties.getRefreshTokenTtl(), AUTH_PATH));
  }

  /**
   * {@code addOAuthStateCookie}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void addOAuthStateCookie(HttpServletResponse response, String state) {
    addCookie(
        response,
        buildCookie(
            properties.getOauthStateCookieName(),
            state,
            properties.getOauthStateTtl(),
            CHZZK_AUTH_PATH));
  }

  /**
   * {@code clearAuthCookies}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void clearAuthCookies(HttpServletResponse response) {
    addCookie(response, buildExpiredCookie(properties.getAccessCookieName(), ROOT_PATH));
    addCookie(response, buildExpiredCookie(properties.getRefreshCookieName(), AUTH_PATH));
  }

  /**
   * {@code clearOAuthStateCookie}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public void clearOAuthStateCookie(HttpServletResponse response) {
    addCookie(response, buildExpiredCookie(properties.getOauthStateCookieName(), CHZZK_AUTH_PATH));
  }

  /**
   * {@code resolveCookieValue}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private Optional<String> resolveCookieValue(HttpServletRequest request, String name) {
    final Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  /**
   * {@code buildCookie}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ResponseCookie buildCookie(String name, String value, Duration maxAge, String path) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .sameSite(properties.getCookieSameSite())
        .path(path)
        .maxAge(maxAge)
        .build();
  }

  /**
   * {@code buildExpiredCookie}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ResponseCookie buildExpiredCookie(String name, String path) {
    return buildCookie(name, "", Duration.ZERO, path);
  }

  /**
   * {@code addCookie}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
