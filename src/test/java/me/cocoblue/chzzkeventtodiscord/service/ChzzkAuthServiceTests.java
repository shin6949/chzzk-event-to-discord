package me.cocoblue.chzzkeventtodiscord.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenRepository;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.SecretEncryptionService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code ChzzkAuthServiceTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 7fec3f1.
 *
 * @since unreleased after Ver.0.1.4
 */
@SpringBootTest
@ActiveProfiles("test")
class ChzzkAuthServiceTests {
  private static final MockWebServer MOCK_WEB_SERVER = new MockWebServer();

  static {
    try {
      MOCK_WEB_SERVER.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Autowired private ChzzkAuthService chzzkAuthService;
  @Autowired private ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
  @Autowired private ChzzkChannelRepository chzzkChannelRepository;
  @Autowired private SecretEncryptionService secretEncryptionService;

  /**
   * {@code registerProperties}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("chzzk.oauth.token-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
    registry.add("chzzk.oauth.api-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
    registry.add("chzzk.oauth.auth-base-url", () -> "https://chzzk.naver.com");
    registry.add("chzzk.oauth.client-id", () -> "test-client-id");
    registry.add("chzzk.oauth.client-secret", () -> "test-client-secret");
    registry.add("chzzk.oauth.redirect-uri", () -> "https://example.test/callback");
  }

  /**
   * {@code setUp}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
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
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @AfterAll
  static void shutdownServer() throws IOException {
    MOCK_WEB_SERVER.shutdown();
  }

  /**
   * {@code authenticateFromCallbackExchangesTokenAndMapsIdentity}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void authenticateFromCallbackExchangesTokenAndMapsIdentity() throws Exception {
    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "code": 200,
                  "message": null,
                  "content": {
                    "accessToken": "access-token-123",
                    "refreshToken": "refresh-token-123",
                    "tokenType": "Bearer",
                    "scope": "user.read",
                    "expiresIn": 3600,
                    "refreshTokenExpiresIn": 7200
                  }
                }
                """));
    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "content": {
                    "channelId": "channel-abc",
                    "channelName": "Channel ABC"
                  }
                }
                """));

    final ChzzkPrincipal principal =
        chzzkAuthService.authenticateFromCallback("code-123", "state-123");

    assertEquals("channel-abc", principal.channelId());
    assertEquals(AppRole.USER, principal.role());

    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository.findById("channel-abc").orElseThrow();
    assertTrue(secretEncryptionService.isEncrypted(tokenEntity.getAccessToken()));
    assertTrue(secretEncryptionService.isEncrypted(tokenEntity.getRefreshToken()));
    assertEquals(
        "access-token-123", secretEncryptionService.decryptIfNeeded(tokenEntity.getAccessToken()));
    assertEquals(
        "refresh-token-123",
        secretEncryptionService.decryptIfNeeded(tokenEntity.getRefreshToken()));
    assertEquals("Bearer", tokenEntity.getTokenType());
    assertEquals("user.read", tokenEntity.getScope());
    assertNotNull(tokenEntity.getAccessTokenExpiresAt());
    assertNotNull(tokenEntity.getRefreshTokenExpiresAt());

    final ChzzkChannelEntity channelEntity =
        chzzkChannelRepository.findById("channel-abc").orElseThrow();
    assertEquals("Channel ABC", channelEntity.getChannelName());

    final RecordedRequest tokenRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(tokenRequest);
    assertEquals("POST", tokenRequest.getMethod());
    assertEquals("/auth/v1/token", tokenRequest.getPath());
    assertEquals("application/json", tokenRequest.getHeader("Content-Type"));
    assertEquals("streaming-alert-service/0.1.4", tokenRequest.getHeader("User-Agent"));
    final String tokenBody = tokenRequest.getBody().readUtf8();
    assertTrue(tokenBody.contains("\"grantType\":\"authorization_code\""));
    assertTrue(tokenBody.contains("\"code\":\"code-123\""));
    assertTrue(tokenBody.contains("\"state\":\"state-123\""));
    assertTrue(tokenBody.contains("\"clientId\":\"test-client-id\""));
    assertFalse(tokenBody.contains("redirectUri"));

    final RecordedRequest userMeRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(userMeRequest);
    assertEquals("GET", userMeRequest.getMethod());
    assertEquals("/open/v1/users/me", userMeRequest.getPath());
    assertEquals("Bearer access-token-123", userMeRequest.getHeader("Authorization"));
    assertEquals("streaming-alert-service/0.1.4", userMeRequest.getHeader("User-Agent"));
  }

  /**
   * {@code buildAuthorizationUrlUsesConfiguredValues}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 22:05:36 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 7fec3f1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void buildAuthorizationUrlUsesConfiguredValues() {
    final String authorizationUrl = chzzkAuthService.buildAuthorizationUrl("state-xyz");

    assertTrue(authorizationUrl.startsWith("https://chzzk.naver.com/account-interlock"));
    assertTrue(authorizationUrl.contains("clientId=test-client-id"));
    assertTrue(authorizationUrl.contains("redirectUri="));
    assertTrue(authorizationUrl.contains("state=state-xyz"));
  }

  /**
   * {@code getValidAccessTokenRefreshesWhenAccessTokenExpired}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void getValidAccessTokenRefreshesWhenAccessTokenExpired() throws Exception {
    final String channelId = "channel-refresh-test";
    final ZonedDateTime oldAccessExpiresAt = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1);
    final ZonedDateTime oldRefreshExpiresAt = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1);
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .refreshToken("refresh-token-abc")
            .tokenType("OldBearer")
            .scope("old.scope")
            .accessTokenExpiresAt(oldAccessExpiresAt)
            .refreshTokenExpiresAt(oldRefreshExpiresAt)
            .build());

    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "code": 200,
                  "message": null,
                  "content": {
                    "accessToken": "refreshed-access-token",
                    "refreshToken": "refreshed-refresh-token",
                    "tokenType": "Bearer",
                    "scope": "user.read.refreshed",
                    "expiresIn": 3600,
                    "refreshTokenExpiresIn": 7200
                  }
                }
                """));

    final String accessToken = chzzkAuthService.getValidAccessToken(channelId);

    assertEquals("refreshed-access-token", accessToken);

    final ChzzkOAuthTokenEntity tokenEntity =
        chzzkOAuthTokenRepository.findById(channelId).orElseThrow();
    assertTrue(secretEncryptionService.isEncrypted(tokenEntity.getAccessToken()));
    assertTrue(secretEncryptionService.isEncrypted(tokenEntity.getRefreshToken()));
    assertEquals(
        "refreshed-access-token",
        secretEncryptionService.decryptIfNeeded(tokenEntity.getAccessToken()));
    assertEquals(
        "refreshed-refresh-token",
        secretEncryptionService.decryptIfNeeded(tokenEntity.getRefreshToken()));
    assertEquals("Bearer", tokenEntity.getTokenType());
    assertEquals("user.read.refreshed", tokenEntity.getScope());
    assertNotNull(tokenEntity.getAccessTokenExpiresAt());
    assertNotNull(tokenEntity.getRefreshTokenExpiresAt());
    assertTrue(tokenEntity.getAccessTokenExpiresAt().isAfter(oldAccessExpiresAt));
    assertTrue(tokenEntity.getRefreshTokenExpiresAt().isAfter(oldRefreshExpiresAt));

    final RecordedRequest refreshRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(refreshRequest);
    assertEquals("POST", refreshRequest.getMethod());
    assertEquals("/auth/v1/token", refreshRequest.getPath());
    assertEquals("application/json", refreshRequest.getHeader("Content-Type"));
    assertEquals("streaming-alert-service/0.1.4", refreshRequest.getHeader("User-Agent"));
    final String refreshBody = refreshRequest.getBody().readUtf8();
    assertTrue(refreshBody.contains("\"grantType\":\"refresh_token\""));
    assertTrue(refreshBody.contains("\"refreshToken\":\"refresh-token-abc\""));
    assertTrue(refreshBody.contains("\"clientId\":\"test-client-id\""));
  }

  /**
   * {@code getValidAccessTokenReturnsPlaintextAndMigratesStoredPlaintextToken}은 필요한 데이터를 조회하거나
   * 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void getValidAccessTokenReturnsPlaintextAndMigratesStoredPlaintextToken() {
    final String channelId = "channel-valid-plaintext";
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("valid-access-token")
            .refreshToken("valid-refresh-token")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1))
            .build());

    final String accessToken = chzzkAuthService.getValidAccessToken(channelId);

    assertEquals("valid-access-token", accessToken);
    final ChzzkOAuthTokenEntity persisted =
        chzzkOAuthTokenRepository.findById(channelId).orElseThrow();
    assertTrue(secretEncryptionService.isEncrypted(persisted.getAccessToken()));
    assertTrue(secretEncryptionService.isEncrypted(persisted.getRefreshToken()));
    assertEquals(
        "valid-access-token", secretEncryptionService.decryptIfNeeded(persisted.getAccessToken()));
    assertEquals(
        "valid-refresh-token",
        secretEncryptionService.decryptIfNeeded(persisted.getRefreshToken()));
  }

  @Test
  /**
   * {@code getValidAccessTokenEncryptsExistingRefreshTokenWhenRefreshResponseDoesNotRotateIt}은
   * refresh token이 회전하지 않을 때 기존 refresh token 암호화를 검증합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  void getValidAccessTokenEncryptsExistingRefreshTokenWhenRefreshResponseDoesNotRotateIt()
      throws Exception {
    final String channelId = "channel-refresh-without-rotation";
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .refreshToken("still-valid-refresh-token")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1))
            .build());

    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "content": {
                    "accessToken": "rotated-access-token",
                    "tokenType": "Bearer",
                    "scope": "user.read",
                    "expiresIn": 3600
                  }
                }
                """));

    final String accessToken = chzzkAuthService.getValidAccessToken(channelId);

    assertEquals("rotated-access-token", accessToken);
    final ChzzkOAuthTokenEntity persisted =
        chzzkOAuthTokenRepository.findById(channelId).orElseThrow();
    assertTrue(secretEncryptionService.isEncrypted(persisted.getAccessToken()));
    assertTrue(secretEncryptionService.isEncrypted(persisted.getRefreshToken()));
    assertEquals(
        "rotated-access-token",
        secretEncryptionService.decryptIfNeeded(persisted.getAccessToken()));
    assertEquals(
        "still-valid-refresh-token",
        secretEncryptionService.decryptIfNeeded(persisted.getRefreshToken()));

    final RecordedRequest refreshRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(refreshRequest);
    assertTrue(
        refreshRequest
            .getBody()
            .readUtf8()
            .contains("\"refreshToken\":\"still-valid-refresh-token\""));
  }

  /**
   * {@code getValidAccessTokenThrowsUnauthorizedWhenRefreshUnavailable}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 23:26:29 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 26ef2c1.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void getValidAccessTokenThrowsUnauthorizedWhenRefreshUnavailable() throws Exception {
    final String channelId = "channel-missing-refresh";
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))
            .refreshTokenExpiresAt(null)
            .build());

    final ResponseStatusException responseStatusException =
        assertThrows(
            ResponseStatusException.class, () -> chzzkAuthService.getValidAccessToken(channelId));

    assertEquals(401, responseStatusException.getStatusCode().value());
    assertNull(MOCK_WEB_SERVER.takeRequest(200, TimeUnit.MILLISECONDS));
  }

  /**
   * {@code getValidAccessTokenReturnsBadGatewayWhenRefreshEndpointFails}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d252252.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void getValidAccessTokenReturnsBadGatewayWhenRefreshEndpointFails() throws Exception {
    final String channelId = "channel-refresh-failure";
    chzzkOAuthTokenRepository.save(
        ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .refreshToken("refresh-token-failure")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1))
            .build());

    MOCK_WEB_SERVER.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .addHeader("Content-Type", "application/json")
            .setBody(
                """
                {
                  "message": "oauth server down"
                }
                """));

    final ResponseStatusException responseStatusException =
        assertThrows(
            ResponseStatusException.class, () -> chzzkAuthService.getValidAccessToken(channelId));
    assertEquals(502, responseStatusException.getStatusCode().value());

    final ChzzkOAuthTokenEntity persisted =
        chzzkOAuthTokenRepository.findById(channelId).orElseThrow();
    assertEquals("old-access-token", persisted.getAccessToken());
    assertEquals("refresh-token-failure", persisted.getRefreshToken());

    final RecordedRequest refreshRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
    assertNotNull(refreshRequest);
    assertEquals("/auth/v1/token", refreshRequest.getPath());
  }
}
