package me.cocoblue.chzzkeventtodiscord.security;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppJwtProperties jwtProperties;
    @Autowired
    private JwtTokenService jwtTokenService;
    @MockBean
    private ChzzkAuthService chzzkAuthService;
    @MockBean
    private ChzzkChannelService chzzkChannelService;

    @Test
    void chzzkLoginEndpointIsPermitAll() throws Exception {
        given(chzzkAuthService.buildAuthorizationUrl(anyString()))
            .willAnswer(invocation -> "https://chzzk.naver.com/account-interlock?state=" + invocation.getArgument(0));

        final MvcResult result = mockMvc.perform(get("/api/v1/auth/chzzk/login"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorizationUrl").value(org.hamcrest.Matchers.containsString("account-interlock")))
            .andExpect(jsonPath("$.state").isNotEmpty())
            .andReturn();

        assertNotNull(result.getResponse().getCookie(jwtProperties.getOauthStateCookieName()));
    }

    @Test
    void chzzkCallbackEndpointIsPermitAllAndCreatesJwtCookies() throws Exception {
        final LoginSession loginSession = startLogin();
        given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
            .willReturn(new ChzzkPrincipal("channel-123", AppRole.USER));
        given(chzzkChannelService.getChannelByChannelId("channel-123"))
            .willReturn(ChzzkChannelDto.builder()
                .channelId("channel-123")
                .channelName("Channel 123")
                .channelImageUrl("https://example.test/channel-123.png")
                .verifiedMark(false)
                .build());

        MvcResult callbackResult = mockMvc.perform(get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", loginSession.state())
                .cookie(loginSession.stateCookie()))
            .andExpect(status().isFound())
            .andExpect(header().string(HttpHeaders.LOCATION, "/subscriptions"))
            .andReturn();

        final Cookie accessCookie = callbackResult.getResponse().getCookie(jwtProperties.getAccessCookieName());
        final Cookie refreshCookie = callbackResult.getResponse().getCookie(jwtProperties.getRefreshCookieName());
        assertNotNull(accessCookie);
        assertNotNull(refreshCookie);

        mockMvc.perform(get("/api/v1/auth/me").cookie(accessCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value("channel-123"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.channelName").value("Channel 123"))
            .andExpect(jsonPath("$.profileUrl").value("https://example.test/channel-123.png"));
    }

    @Test
    void chzzkCallbackRejectsRequestsWithoutExpectedState() throws Exception {
        mockMvc.perform(get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", "mock-state"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void authMeEndpointRequiresAuthenticationByDesign() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutEndpointRequiresAuthenticationByDesign() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeEndpointRequiresAuthenticationByDesign() throws Exception {
        mockMvc.perform(post("/api/v1/auth/chzzk/revoke"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointIssuesAccessCookieFromRefreshJwt() throws Exception {
        given(chzzkChannelService.getChannelByChannelId("channel-refresh"))
            .willReturn(ChzzkChannelDto.builder()
                .channelId("channel-refresh")
                .channelName("Refresh Channel")
                .channelImageUrl("https://example.test/channel-refresh.png")
                .verifiedMark(false)
                .build());

        final Cookie refreshCookie = new Cookie(
            jwtProperties.getRefreshCookieName(),
            jwtTokenService.createRefreshToken(new ChzzkPrincipal("channel-refresh", AppRole.USER))
        );

        final MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value("channel-refresh"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.channelName").value("Refresh Channel"))
            .andReturn();

        assertNotNull(refreshResult.getResponse().getCookie(jwtProperties.getAccessCookieName()));
    }

    @Test
    void unauthenticatedAccessToProtectedApiReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/placeholder"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoutesRequireAdminRoleForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/placeholder").with(user("user").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void logoutReturnsOkForAuthenticatedJwt() throws Exception {
        final LoginSession loginSession = startLogin();
        given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
            .willReturn(new ChzzkPrincipal("channel-logout", AppRole.USER));

        MvcResult callbackResult = mockMvc.perform(get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", loginSession.state())
                .cookie(loginSession.stateCookie()))
            .andExpect(status().isFound())
            .andReturn();

        final Cookie accessCookie = callbackResult.getResponse().getCookie(jwtProperties.getAccessCookieName());
        assertNotNull(accessCookie);

        final MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout").cookie(accessCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"))
            .andReturn();

        final String setCookie = String.join("\n", logoutResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
        assertTrue(setCookie.contains(jwtProperties.getAccessCookieName() + "="));
        assertTrue(setCookie.contains(jwtProperties.getRefreshCookieName() + "="));
        assertTrue(setCookie.contains("Max-Age=0"));
    }

    @Test
    void subscriptionsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions"))
            .andExpect(status().isUnauthorized());
    }

    private LoginSession startLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(get("/api/v1/auth/chzzk/login"))
            .andExpect(status().isOk())
            .andReturn();
        final Cookie stateCookie = loginResult.getResponse().getCookie(jwtProperties.getOauthStateCookieName());
        assertNotNull(stateCookie);
        String state = objectMapper.readTree(loginResult.getResponse().getContentAsString())
            .get("state")
            .asText();
        return new LoginSession(stateCookie, state);
    }

    private record LoginSession(Cookie stateCookie, String state) {
    }
}
