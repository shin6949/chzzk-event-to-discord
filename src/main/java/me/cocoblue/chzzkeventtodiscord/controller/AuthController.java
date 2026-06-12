package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.AuthCookieService;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.JwtTokenService;
import me.cocoblue.chzzkeventtodiscord.security.RefreshTokenSessionService;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkAuthService;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code AuthController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * d1f0cd9.
 *
 * @since unreleased after Ver.0.1.4
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private static final String LOGIN_SUCCESS_REDIRECT_PATH = "/subscriptions";

  private final ChzzkAuthService chzzkAuthService;
  private final ChzzkChannelService chzzkChannelService;
  private final JwtTokenService jwtTokenService;
  private final AuthCookieService authCookieService;
  private final RefreshTokenSessionService refreshTokenSessionService;

  /**
   * {@code startChzzkLogin}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/chzzk/login")
  public ResponseEntity<LoginStartResponse> startChzzkLogin(HttpServletResponse response) {
    final String state = UUID.randomUUID().toString();
    final String authorizationUrl = chzzkAuthService.buildAuthorizationUrl(state);
    authCookieService.addOAuthStateCookie(response, state);

    return ResponseEntity.ok(
        new LoginStartResponse(
            authorizationUrl, state, "Redirect client to CHZZK authorization URL"));
  }

  /**
   * {@code handleChzzkCallback}은 관련 처리 흐름을 실행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/chzzk/callback")
  public ResponseEntity<Void> handleChzzkCallback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      HttpServletRequest request,
      HttpServletResponse response) {
    validateCallbackState(request, state);
    final ChzzkPrincipal principal = chzzkAuthService.authenticateFromCallback(code, state);
    issueAuthCookies(response, principal);
    authCookieService.clearOAuthStateCookie(response);

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, LOGIN_SUCCESS_REDIRECT_PATH)
        .build();
  }

  /**
   * {@code me}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/me")
  public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    final ChzzkChannelDto channel =
        chzzkChannelService.getChannelByChannelId(principal.channelId());
    return ResponseEntity.ok(AuthUserResponse.from(principal, channel));
  }

  /**
   * {@code csrf}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping("/csrf")
  public ResponseEntity<CsrfResponse> csrf(CsrfToken csrfToken) {
    return ResponseEntity.ok(
        new CsrfResponse(
            csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken()));
  }

  /**
   * {@code logout}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping("/logout")
  public ResponseEntity<LogoutResponse> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    revokeRefreshSessionIfPresent(request);
    SecurityContextHolder.clearContext();
    authCookieService.clearAuthCookies(response);

    return ResponseEntity.ok(new LogoutResponse("Logged out"));
  }

  /**
   * {@code refresh}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping("/refresh")
  public ResponseEntity<AuthUserResponse> refresh(
      HttpServletRequest request, HttpServletResponse response) {
    final String refreshToken =
        authCookieService
            .resolveRefreshToken(request)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "refresh token is required"));
    final JwtTokenService.ParsedRefreshToken parsedRefreshToken;
    try {
      parsedRefreshToken = jwtTokenService.parseRefreshTokenDetails(refreshToken);
    } catch (JwtException | IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token", e);
    }
    refreshTokenSessionService.consume(parsedRefreshToken);
    final ChzzkPrincipal principal = parsedRefreshToken.principal();
    authCookieService.addAccessTokenCookie(response, jwtTokenService.createAccessToken(principal));
    issueRefreshCookie(response, principal);

    final ChzzkChannelDto channel =
        chzzkChannelService.getChannelByChannelId(principal.channelId());
    return ResponseEntity.ok(AuthUserResponse.from(principal, channel));
  }

  /**
   * {@code revoke}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostMapping("/chzzk/revoke")
  public ResponseEntity<LogoutResponse> revoke(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    final ChzzkPrincipal principal = extractPrincipal(authentication);
    chzzkAuthService.revokeCurrentUserTokens(principal.channelId());
    revokeRefreshSessionIfPresent(request);
    SecurityContextHolder.clearContext();
    authCookieService.clearAuthCookies(response);

    return ResponseEntity.ok(new LogoutResponse("Tokens revoked"));
  }

  /**
   * {@code extractPrincipal}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkPrincipal extractPrincipal(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication is required");
    }

    final Object principalObject = authentication.getPrincipal();
    if (principalObject instanceof ChzzkPrincipal chzzkPrincipal) {
      return chzzkPrincipal;
    }

    final AppRole role =
        authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
            ? AppRole.ADMIN
            : AppRole.USER;
    return new ChzzkPrincipal(authentication.getName(), role);
  }

  /**
   * {@code validateCallbackState}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void validateCallbackState(HttpServletRequest request, String state) {
    final String expectedState = authCookieService.resolveOAuthState(request).orElse(null);
    if (!StringUtils.hasText(expectedState) || !expectedState.equals(state)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid oauth state");
    }
  }

  /**
   * {@code issueAuthCookies}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private void issueAuthCookies(HttpServletResponse response, ChzzkPrincipal principal) {
    authCookieService.addAccessTokenCookie(response, jwtTokenService.createAccessToken(principal));
    issueRefreshCookie(response, principal);
  }

  /**
   * {@code issueRefreshCookie}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private void issueRefreshCookie(HttpServletResponse response, ChzzkPrincipal principal) {
    final JwtTokenService.IssuedRefreshToken refreshToken =
        jwtTokenService.issueRefreshToken(principal);
    refreshTokenSessionService.create(principal, refreshToken.tokenId(), refreshToken.expiresAt());
    authCookieService.addRefreshTokenCookie(response, refreshToken.token());
  }

  /**
   * {@code revokeRefreshSessionIfPresent}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private void revokeRefreshSessionIfPresent(HttpServletRequest request) {
    authCookieService
        .resolveRefreshToken(request)
        .ifPresent(
            refreshToken -> {
              try {
                refreshTokenSessionService.revoke(
                    jwtTokenService.parseRefreshTokenDetails(refreshToken).tokenId());
              } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid refresh cookies are cleared by the caller; there is no active session to
                // revoke.
              }
            });
  }

  /**
   * {@code LoginStartResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record LoginStartResponse(String authorizationUrl, String state, String message) {}

  /**
   * {@code CsrfResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private record CsrfResponse(String headerName, String parameterName, String token) {}

  /**
   * {@code AuthUserResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record AuthUserResponse(
      String channelId, AppRole role, String channelName, String profileUrl) {
    /**
     * {@code from}은 인증 주체와 채널 정보를 인증 사용자 응답으로 변환합니다.
     *
     * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거
     * 커밋 d1f0cd9.
     *
     * @since unreleased after Ver.0.1.4
     */
    private static AuthUserResponse from(ChzzkPrincipal principal, ChzzkChannelDto channel) {
      final String channelName =
          channel != null && StringUtils.hasText(channel.getChannelName())
              ? channel.getChannelName()
              : principal.channelId();
      final String profileUrl = channel == null ? null : channel.getChannelImageUrl();
      return new AuthUserResponse(principal.channelId(), principal.role(), channelName, profileUrl);
    }
  }

  /**
   * {@code LogoutResponse}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record LogoutResponse(String message) {}
}
