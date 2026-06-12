package me.cocoblue.chzzkeventtodiscord.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenRepository;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code AuthControllerTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 26ef2c1.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTests {
  private static final MockWebServer MOCK_WEB_SERVER = new MockWebServer();
  private static final String CHANNEL_ID = "channel-revoke-test";

  static {
    try {
      MOCK_WEB_SERVER.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
  @Autowired private ChzzkChannelRepository chzzkChannelRepository;

  /**
   * {@code registerProperties}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("chzzk.oauth.token-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
    registry.add("chzzk.oauth.api-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
    registry.add("chzzk.oauth.client-id", () -> "test-client-id");
    registry.add("chzzk.oauth.client-secret", () -> "test-client-secret");
  }

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @BeforeEach
  void setUp() throws Exception {
    chzzkOAuthTokenRepository.deleteAll();
    chzzkChannelRepository.deleteAll();
    while (MOCK_WEB_SERVER.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
      // drain recorded requests left by previous tests
    }
  }

  /**
   * {@code shutdownServer}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @AfterAll
  static void shutdownServer() throws IOException {
    MOCK_WEB_SERVER.shutdown();
  }

  /**
   * {@code meEndpointReturnsPrincipalInfoWhenAuthenticated}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d252252.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void meEndpointReturnsPrincipalInfoWhenAuthenticated() throws Exception {
    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "code": 200,
                  "message": null,
                  "content": {
                    "data": [
                      {
                        "channelId": "channel-revoke-test",
                        "channelName": "Profile Channel",
                        "channelImageUrl": "https://example.test/profile.png",
                        "verifiedMark": true,
                        "followerCount": 123
                      }
                    ]
                  }
                }
                """));

    mockMvc
        .perform(get("/api/v1/auth/me").with(authentication(authenticatedUser())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.channelId").value(CHANNEL_ID))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.channelName").value("Profile Channel"))
        .andExpect(jsonPath("$.profileUrl").value("https://example.test/profile.png"));

    final RecordedRequest channelRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(channelRequest);
    assertEquals("GET", channelRequest.getMethod());
    assertNotNull(channelRequest.getPath());
    assertTrue(channelRequest.getPath().startsWith("/open/v1/channels"));
    assertTrue(channelRequest.getPath().contains("channelIds=" + CHANNEL_ID));
    assertEquals("test-client-id", channelRequest.getHeader("Client-Id"));
    assertEquals("test-client-secret", channelRequest.getHeader("Client-Secret"));
    assertEquals("streaming-alert-service/0.1.4", channelRequest.getHeader("User-Agent"));
  }

  /**
   * {@code logoutEndpointClearsJwtCookies}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void logoutEndpointClearsJwtCookies() throws Exception {
    final MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/logout").with(csrf()).with(authentication(authenticatedUser())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"))
            .andReturn();

    final String setCookie =
        String.join("\n", result.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
    assertTrue(setCookie.contains("streaming_alert_access="));
    assertTrue(setCookie.contains("streaming_alert_refresh="));
    assertTrue(setCookie.contains("Max-Age=0"));
  }

  /**
   * {@code revokeEndpointRequiresAuthentication}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d252252.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void revokeEndpointRequiresAuthentication() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/chzzk/revoke").with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  /**
   * {@code revokeEndpointCallsChzzkRevokeAndClearsJwtCookies}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void revokeEndpointCallsChzzkRevokeAndClearsJwtCookies() throws Exception {
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(CHANNEL_ID)
            .accessToken("access-token-old")
            .refreshToken("refresh-token-old")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1))
            .build());

    MOCK_WEB_SERVER.enqueue(new MockResponse().setResponseCode(204));

    final MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/chzzk/revoke")
                    .with(csrf())
                    .with(authentication(authenticatedUser())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Tokens revoked"))
            .andReturn();

    final RecordedRequest revokeRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(revokeRequest);
    assertEquals("application/json", revokeRequest.getHeader("Content-Type"));
    assertEquals("streaming-alert-service/0.1.4", revokeRequest.getHeader("User-Agent"));
    final String revokeRequestBody = revokeRequest.getBody().readUtf8();
    assertTrue(revokeRequestBody.contains("\"clientId\":\"test-client-id\""));
    assertTrue(revokeRequestBody.contains("\"clientSecret\":\"test-client-secret\""));
    assertTrue(revokeRequestBody.contains("\"token\":\"refresh-token-old\""));
    assertTrue(revokeRequestBody.contains("\"tokenTypeHint\":\"refresh_token\""));
    assertNotNull(revokeRequest.getPath());
    assertTrue(revokeRequest.getPath().endsWith("/auth/v1/token/revoke"));
    assertFalse(chzzkOAuthTokenRepository.findById(CHANNEL_ID).isPresent());

    final String setCookie =
        String.join("\n", result.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
    assertTrue(setCookie.contains("streaming_alert_access="));
    assertTrue(setCookie.contains("streaming_alert_refresh="));
    assertTrue(setCookie.contains("Max-Age=0"));
  }

  /**
   * {@code authenticatedUser}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private Authentication authenticatedUser() {
    return new UsernamePasswordAuthenticationToken(
        new ChzzkPrincipal(CHANNEL_ID, AppRole.USER),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
