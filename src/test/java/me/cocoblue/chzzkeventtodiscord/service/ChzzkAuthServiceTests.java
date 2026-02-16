package me.cocoblue.chzzkeventtodiscord.service;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelEntity;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Autowired
    private ChzzkAuthService chzzkAuthService;
    @Autowired
    private ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("chzzk.oauth.token-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("chzzk.oauth.api-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("chzzk.oauth.auth-base-url", () -> "https://chzzk.naver.com");
        registry.add("chzzk.oauth.client-id", () -> "test-client-id");
        registry.add("chzzk.oauth.client-secret", () -> "test-client-secret");
        registry.add("chzzk.oauth.redirect-uri", () -> "https://example.test/callback");
    }

    @BeforeEach
    void setUp() throws Exception {
        chzzkOAuthTokenRepository.deleteAll();
        chzzkChannelRepository.deleteAll();
        while (MOCK_WEB_SERVER.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
            // drain recorded requests left by previous tests
        }
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        MOCK_WEB_SERVER.shutdown();
    }

    @Test
    void authenticateFromCallbackExchangesTokenAndMapsIdentity() throws Exception {
        MOCK_WEB_SERVER.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "access_token": "access-token-123",
                  "refresh_token": "refresh-token-123",
                  "token_type": "Bearer",
                  "scope": "user.read",
                  "expires_in": 3600,
                  "refresh_token_expires_in": 7200
                }
                """));
        MOCK_WEB_SERVER.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "content": {
                    "channelId": "channel-abc",
                    "channelName": "Channel ABC"
                  }
                }
                """));

        final ChzzkPrincipal principal = chzzkAuthService.authenticateFromCallback("code-123", "state-123");

        assertEquals("channel-abc", principal.channelId());
        assertEquals(AppRole.USER, principal.role());

        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById("channel-abc").orElseThrow();
        assertEquals("access-token-123", tokenEntity.getAccessToken());
        assertEquals("refresh-token-123", tokenEntity.getRefreshToken());
        assertEquals("Bearer", tokenEntity.getTokenType());
        assertEquals("user.read", tokenEntity.getScope());
        assertNotNull(tokenEntity.getAccessTokenExpiresAt());
        assertNotNull(tokenEntity.getRefreshTokenExpiresAt());

        final ChzzkChannelEntity channelEntity = chzzkChannelRepository.findById("channel-abc").orElseThrow();
        assertEquals("Channel ABC", channelEntity.getChannelName());

        final RecordedRequest tokenRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(tokenRequest);
        assertEquals("POST", tokenRequest.getMethod());
        assertEquals("/auth/v1/token", tokenRequest.getPath());
        final String tokenBody = tokenRequest.getBody().readUtf8();
        assertTrue(tokenBody.contains("grantType=authorization_code"));
        assertTrue(tokenBody.contains("code=code-123"));
        assertTrue(tokenBody.contains("state=state-123"));
        assertTrue(tokenBody.contains("clientId=test-client-id"));

        final RecordedRequest userMeRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(userMeRequest);
        assertEquals("GET", userMeRequest.getMethod());
        assertEquals("/open/v1/users/me", userMeRequest.getPath());
        assertEquals("Bearer access-token-123", userMeRequest.getHeader("Authorization"));
    }

    @Test
    void buildAuthorizationUrlUsesConfiguredValues() {
        final String authorizationUrl = chzzkAuthService.buildAuthorizationUrl("state-xyz");

        assertTrue(authorizationUrl.startsWith("https://chzzk.naver.com/account-interlock"));
        assertTrue(authorizationUrl.contains("clientId=test-client-id"));
        assertTrue(authorizationUrl.contains("redirectUri="));
        assertTrue(authorizationUrl.contains("state=state-xyz"));
    }

    @Test
    void getValidAccessTokenRefreshesWhenAccessTokenExpired() throws Exception {
        final String channelId = "channel-refresh-test";
        chzzkOAuthTokenRepository.save(ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .refreshToken("refresh-token-abc")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1))
            .build());

        MOCK_WEB_SERVER.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "access_token": "refreshed-access-token",
                  "refresh_token": "refreshed-refresh-token",
                  "token_type": "Bearer",
                  "scope": "user.read",
                  "expires_in": 3600,
                  "refresh_token_expires_in": 7200
                }
                """));

        final String accessToken = chzzkAuthService.getValidAccessToken(channelId);

        assertEquals("refreshed-access-token", accessToken);

        final ChzzkOAuthTokenEntity tokenEntity = chzzkOAuthTokenRepository.findById(channelId).orElseThrow();
        assertEquals("refreshed-access-token", tokenEntity.getAccessToken());
        assertEquals("refreshed-refresh-token", tokenEntity.getRefreshToken());
        assertNotNull(tokenEntity.getAccessTokenExpiresAt());
        assertNotNull(tokenEntity.getRefreshTokenExpiresAt());

        final RecordedRequest refreshRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(refreshRequest);
        assertEquals("POST", refreshRequest.getMethod());
        assertEquals("/auth/v1/token", refreshRequest.getPath());
        final String refreshBody = refreshRequest.getBody().readUtf8();
        assertTrue(refreshBody.contains("grantType=refresh_token"));
        assertTrue(refreshBody.contains("refreshToken=refresh-token-abc"));
        assertTrue(refreshBody.contains("clientId=test-client-id"));
    }

    @Test
    void getValidAccessTokenThrowsUnauthorizedWhenRefreshUnavailable() throws Exception {
        final String channelId = "channel-missing-refresh";
        chzzkOAuthTokenRepository.save(ChzzkOAuthTokenEntity.builder()
            .channelId(channelId)
            .accessToken("old-access-token")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1))
            .refreshTokenExpiresAt(null)
            .build());

        final ResponseStatusException responseStatusException =
            assertThrows(ResponseStatusException.class, () -> chzzkAuthService.getValidAccessToken(channelId));

        assertEquals(401, responseStatusException.getStatusCode().value());
    }
}
