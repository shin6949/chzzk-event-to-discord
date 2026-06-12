package me.cocoblue.chzzkeventtodiscord.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkAuthService;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * {@code SecurityConfigTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * d1f0cd9.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppJwtProperties jwtProperties;
  @Autowired private JwtTokenService jwtTokenService;
  @Autowired private RefreshTokenSessionService refreshTokenSessionService;
  @MockitoBean private ChzzkAuthService chzzkAuthService;
  @MockitoBean private ChzzkChannelService chzzkChannelService;

  /**
   * {@code chzzkLoginEndpointIsPermitAll}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void chzzkLoginEndpointIsPermitAll() throws Exception {
    given(chzzkAuthService.buildAuthorizationUrl(anyString()))
        .willAnswer(
            invocation ->
                "https://chzzk.naver.com/account-interlock?state=" + invocation.getArgument(0));

    final MvcResult result =
        mockMvc
            .perform(get("/api/v1/auth/chzzk/login"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.authorizationUrl")
                    .value(org.hamcrest.Matchers.containsString("account-interlock")))
            .andExpect(jsonPath("$.state").isNotEmpty())
            .andReturn();

    assertNotNull(result.getResponse().getCookie(jwtProperties.getOauthStateCookieName()));
  }

  /**
   * {@code chzzkCallbackEndpointIsPermitAllAndCreatesJwtCookies}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void chzzkCallbackEndpointIsPermitAllAndCreatesJwtCookies() throws Exception {
    final LoginSession loginSession = startLogin();
    given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
        .willReturn(new ChzzkPrincipal("channel-123", AppRole.USER));
    given(chzzkChannelService.getChannelByChannelId("channel-123"))
        .willReturn(
            ChzzkChannelDto.builder()
                .channelId("channel-123")
                .channelName("Channel 123")
                .channelImageUrl("https://example.test/channel-123.png")
                .verifiedMark(false)
                .build());

    MvcResult callbackResult =
        mockMvc
            .perform(
                get("/api/v1/auth/chzzk/callback")
                    .param("code", "mock-auth-code")
                    .param("state", loginSession.state())
                    .cookie(loginSession.stateCookie()))
            .andExpect(status().isFound())
            .andExpect(header().string(HttpHeaders.LOCATION, "/subscriptions"))
            .andReturn();

    final Cookie accessCookie =
        callbackResult.getResponse().getCookie(jwtProperties.getAccessCookieName());
    final Cookie refreshCookie =
        callbackResult.getResponse().getCookie(jwtProperties.getRefreshCookieName());
    assertNotNull(accessCookie);
    assertNotNull(refreshCookie);

    mockMvc
        .perform(get("/api/v1/auth/me").cookie(accessCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.channelId").value("channel-123"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.channelName").value("Channel 123"))
        .andExpect(jsonPath("$.profileUrl").value("https://example.test/channel-123.png"));
  }

  /**
   * {@code chzzkCallbackRejectsRequestsWithoutExpectedState}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void chzzkCallbackRejectsRequestsWithoutExpectedState() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", "mock-state"))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code authMeEndpointRequiresAuthenticationByDesign}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void authMeEndpointRequiresAuthenticationByDesign() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  /**
   * {@code healthcheckEndpointIsPermitAll}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void healthcheckEndpointIsPermitAll() throws Exception {
    mockMvc
        .perform(get("/healthz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  /**
   * {@code logoutEndpointRequiresAuthenticationByDesign}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void logoutEndpointRequiresAuthenticationByDesign() throws Exception {
    mockMvc.perform(post("/api/v1/auth/logout").with(csrf())).andExpect(status().isUnauthorized());
  }

  /**
   * {@code revokeEndpointRequiresAuthenticationByDesign}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void revokeEndpointRequiresAuthenticationByDesign() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/chzzk/revoke").with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  /**
   * {@code refreshEndpointIssuesAccessCookieFromRefreshJwt}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void refreshEndpointIssuesAccessCookieFromRefreshJwt() throws Exception {
    given(chzzkChannelService.getChannelByChannelId("channel-refresh"))
        .willReturn(
            ChzzkChannelDto.builder()
                .channelId("channel-refresh")
                .channelName("Refresh Channel")
                .channelImageUrl("https://example.test/channel-refresh.png")
                .verifiedMark(false)
                .build());

    final ChzzkPrincipal principal = new ChzzkPrincipal("channel-refresh", AppRole.USER);
    final JwtTokenService.IssuedRefreshToken issuedRefreshToken =
        jwtTokenService.issueRefreshToken(principal);
    refreshTokenSessionService.create(
        principal, issuedRefreshToken.tokenId(), issuedRefreshToken.expiresAt());
    final Cookie refreshCookie =
        new Cookie(jwtProperties.getRefreshCookieName(), issuedRefreshToken.token());

    final MvcResult refreshResult =
        mockMvc
            .perform(post("/api/v1/auth/refresh").cookie(refreshCookie).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value("channel-refresh"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.channelName").value("Refresh Channel"))
            .andReturn();

    assertNotNull(refreshResult.getResponse().getCookie(jwtProperties.getAccessCookieName()));
    assertNotNull(refreshResult.getResponse().getCookie(jwtProperties.getRefreshCookieName()));

    mockMvc
        .perform(post("/api/v1/auth/refresh").cookie(refreshCookie).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  /**
   * {@code unauthenticatedAccessToProtectedApiReturnsUnauthorized}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void unauthenticatedAccessToProtectedApiReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/admin/placeholder")).andExpect(status().isUnauthorized());
  }

  /**
   * {@code adminRoutesRequireAdminRoleForAuthenticatedUser}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void adminRoutesRequireAdminRoleForAuthenticatedUser() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/placeholder").with(user("user").roles("USER")))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code logoutReturnsOkForAuthenticatedJwt}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void logoutReturnsOkForAuthenticatedJwt() throws Exception {
    final LoginSession loginSession = startLogin();
    given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
        .willReturn(new ChzzkPrincipal("channel-logout", AppRole.USER));

    MvcResult callbackResult =
        mockMvc
            .perform(
                get("/api/v1/auth/chzzk/callback")
                    .param("code", "mock-auth-code")
                    .param("state", loginSession.state())
                    .cookie(loginSession.stateCookie()))
            .andExpect(status().isFound())
            .andReturn();

    final Cookie accessCookie =
        callbackResult.getResponse().getCookie(jwtProperties.getAccessCookieName());
    assertNotNull(accessCookie);

    final MvcResult logoutResult =
        mockMvc
            .perform(post("/api/v1/auth/logout").cookie(accessCookie).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"))
            .andReturn();

    final String setCookie =
        String.join("\n", logoutResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
    assertTrue(setCookie.contains(jwtProperties.getAccessCookieName() + "="));
    assertTrue(setCookie.contains(jwtProperties.getRefreshCookieName() + "="));
    assertTrue(setCookie.contains("Max-Age=0"));
  }

  /**
   * {@code subscriptionsEndpointRequiresAuthentication}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void subscriptionsEndpointRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/subscriptions")).andExpect(status().isUnauthorized());
  }

  /**
   * {@code mutatingApiRequestsRequireCsrfToken}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void mutatingApiRequestsRequireCsrfToken() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/logout").with(user("channel-1").roles("USER")))
        .andExpect(status().isForbidden());
  }

  /**
   * {@code startLogin}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private LoginSession startLogin() throws Exception {
    MvcResult loginResult =
        mockMvc.perform(get("/api/v1/auth/chzzk/login")).andExpect(status().isOk()).andReturn();
    final Cookie stateCookie =
        loginResult.getResponse().getCookie(jwtProperties.getOauthStateCookieName());
    assertNotNull(stateCookie);
    String state =
        objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("state").asText();
    return new LoginSession(stateCookie, state);
  }

  /**
   * {@code LoginSession}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private record LoginSession(Cookie stateCookie, String state) {}
}
