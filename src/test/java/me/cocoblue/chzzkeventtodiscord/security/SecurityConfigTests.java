package me.cocoblue.chzzkeventtodiscord.security;

import me.cocoblue.chzzkeventtodiscord.service.ChzzkAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ChzzkAuthService chzzkAuthService;

    @Test
    void chzzkLoginEndpointIsPermitAll() throws Exception {
        given(chzzkAuthService.buildAuthorizationUrl(anyString()))
            .willAnswer(invocation -> "https://chzzk.naver.com/account-interlock?state=" + invocation.getArgument(0));

        mockMvc.perform(get("/api/v1/auth/chzzk/login"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorizationUrl").value(org.hamcrest.Matchers.containsString("account-interlock")))
            .andExpect(jsonPath("$.state").isNotEmpty());
    }

    @Test
    void chzzkCallbackEndpointIsPermitAllAndCreatesAuthenticatedSession() throws Exception {
        given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
            .willReturn(new ChzzkPrincipal("channel-123", AppRole.USER));

        MvcResult callbackResult = mockMvc.perform(get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", "mock-state"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value("channel-123"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andReturn();

        MockHttpSession session = (MockHttpSession) callbackResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/api/v1/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.channelId").value("channel-123"))
            .andExpect(jsonPath("$.role").value("USER"));
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
    void logoutReturnsOkForAuthenticatedSession() throws Exception {
        given(chzzkAuthService.authenticateFromCallback(anyString(), anyString()))
            .willReturn(new ChzzkPrincipal("channel-logout", AppRole.USER));

        MvcResult callbackResult = mockMvc.perform(get("/api/v1/auth/chzzk/callback")
                .param("code", "mock-auth-code")
                .param("state", "mock-state"))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpSession session = (MockHttpSession) callbackResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));
    }

    @Test
    void subscriptionsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions"))
            .andExpect(status().isUnauthorized());
    }
}
