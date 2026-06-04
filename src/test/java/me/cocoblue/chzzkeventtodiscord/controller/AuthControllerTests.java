package me.cocoblue.chzzkeventtodiscord.controller;

import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenEntity;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkOAuthTokenRepository;
import me.cocoblue.chzzkeventtodiscord.domain.chzzk.ChzzkChannelRepository;
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

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ChzzkOAuthTokenRepository chzzkOAuthTokenRepository;
    @Autowired
    private ChzzkChannelRepository chzzkChannelRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("chzzk.oauth.token-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("chzzk.oauth.api-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("chzzk.oauth.client-id", () -> "test-client-id");
        registry.add("chzzk.oauth.client-secret", () -> "test-client-secret");
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
    void meEndpointReturnsPrincipalInfoWhenAuthenticated() throws Exception {
        MOCK_WEB_SERVER.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody("""
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

        mockMvc.perform(get("/api/v1/auth/me")
                .with(authentication(authenticatedUser())))
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
        assertEquals("chzzk-event-to-discord/0.1.4", channelRequest.getHeader("User-Agent"));
    }

    @Test
    void logoutEndpointClearsJwtCookies() throws Exception {
        final MvcResult result = mockMvc.perform(post("/api/v1/auth/logout")
                .with(authentication(authenticatedUser())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"))
            .andReturn();

        final String setCookie = String.join("\n", result.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
        assertTrue(setCookie.contains("chzzk_app_access="));
        assertTrue(setCookie.contains("chzzk_app_refresh="));
        assertTrue(setCookie.contains("Max-Age=0"));
    }

    @Test
    void revokeEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/chzzk/revoke"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeEndpointCallsChzzkRevokeAndClearsJwtCookies() throws Exception {
        chzzkOAuthTokenRepository.save(ChzzkOAuthTokenEntity.builder()
            .channelId(CHANNEL_ID)
            .accessToken("access-token-old")
            .refreshToken("refresh-token-old")
            .tokenType("Bearer")
            .scope("user.read")
            .accessTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10))
            .refreshTokenExpiresAt(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1))
            .build());

        MOCK_WEB_SERVER.enqueue(new MockResponse().setResponseCode(204));

        final MvcResult result = mockMvc.perform(post("/api/v1/auth/chzzk/revoke")
                .with(authentication(authenticatedUser())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Tokens revoked"))
            .andReturn();

        final RecordedRequest revokeRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(revokeRequest);
        assertEquals("application/json", revokeRequest.getHeader("Content-Type"));
        assertEquals("chzzk-event-to-discord/0.1.4", revokeRequest.getHeader("User-Agent"));
        final String revokeRequestBody = revokeRequest.getBody().readUtf8();
        assertTrue(revokeRequestBody.contains("\"clientId\":\"test-client-id\""));
        assertTrue(revokeRequestBody.contains("\"clientSecret\":\"test-client-secret\""));
        assertTrue(revokeRequestBody.contains("\"token\":\"refresh-token-old\""));
        assertTrue(revokeRequestBody.contains("\"tokenTypeHint\":\"refresh_token\""));
        assertNotNull(revokeRequest.getPath());
        assertTrue(revokeRequest.getPath().endsWith("/auth/v1/token/revoke"));
        assertFalse(chzzkOAuthTokenRepository.findById(CHANNEL_ID).isPresent());

        final String setCookie = String.join("\n", result.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
        assertTrue(setCookie.contains("chzzk_app_access="));
        assertTrue(setCookie.contains("chzzk_app_refresh="));
        assertTrue(setCookie.contains("Max-Age=0"));
    }

    private Authentication authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(
            new ChzzkPrincipal(CHANNEL_ID, AppRole.USER),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
