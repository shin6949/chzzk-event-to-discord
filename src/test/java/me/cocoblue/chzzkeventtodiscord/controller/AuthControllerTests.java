package me.cocoblue.chzzkeventtodiscord.controller;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.core.context.SecurityContextHolder.createEmptyContext;
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

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("chzzk.oauth.token-base-url", () -> MOCK_WEB_SERVER.url("/").toString());
        registry.add("chzzk.oauth.client-id", () -> "test-client-id");
        registry.add("chzzk.oauth.client-secret", () -> "test-client-secret");
    }

    @BeforeEach
    void setUp() throws Exception {
        chzzkOAuthTokenRepository.deleteAll();
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
        final MockHttpSession session = new MockHttpSession();
        final var context = createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            new ChzzkPrincipal(CHANNEL_ID, AppRole.USER),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mockMvc.perform(get("/api/v1/auth/me")
                .session(session)
                .with(authentication(context.getAuthentication())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value(CHANNEL_ID))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void logoutEndpointClearsSession() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final var context = createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            new ChzzkPrincipal(CHANNEL_ID, AppRole.USER),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mockMvc.perform(post("/api/v1/auth/logout")
                .session(session)
                .with(authentication(context.getAuthentication())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));

        mockMvc.perform(get("/api/v1/auth/me").session(session))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/chzzk/revoke"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeEndpointCallsChzzkRevokeAndClearsSession() throws Exception {
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

        final MockHttpSession session = new MockHttpSession();
        final var context = createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            new ChzzkPrincipal(CHANNEL_ID, AppRole.USER),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mockMvc.perform(post("/api/v1/auth/chzzk/revoke")
                .session(session)
                .with(authentication(context.getAuthentication())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Tokens revoked"));

        final RecordedRequest revokeRequest = MOCK_WEB_SERVER.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(revokeRequest);
        final String revokeRequestBody = revokeRequest.getBody().readUtf8();
        assertTrue(revokeRequestBody.contains("clientId=test-client-id"));
        assertTrue(revokeRequestBody.contains("clientSecret=test-client-secret"));
        assertTrue(revokeRequestBody.contains("refreshToken=refresh-token-old"));
        assertNotNull(revokeRequest.getPath());
        assertTrue(revokeRequest.getPath().endsWith("/auth/v1/token/revoke"));
        assertFalse(chzzkOAuthTokenRepository.findById(CHANNEL_ID).isPresent());

        mockMvc.perform(get("/api/v1/auth/me").session(session))
            .andExpect(status().isUnauthorized());
    }
}
