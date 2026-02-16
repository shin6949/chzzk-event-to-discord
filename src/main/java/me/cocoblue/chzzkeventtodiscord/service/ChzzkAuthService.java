package me.cocoblue.chzzkeventtodiscord.service;

import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import org.springframework.stereotype.Service;

@Service
public class ChzzkAuthService {

    public String buildAuthorizationUrl(String state) {
        // TODO: replace TODO placeholders with real CHZZK OAuth client configuration.
        return "https://chzzk.naver.com/account-interlock?clientId={TODO_CLIENT_ID}&redirectUri={TODO_REDIRECT_URI}&state="
            + state;
    }

    public ChzzkPrincipal authenticateFromCallback(String code, String state, String mockChannelId, AppRole mockRole) {
        // TODO: exchange authorization code for tokens at CHZZK Open API.
        // TODO: call /open/v1/users/me with the access token and map channelId.
        final AppRole resolvedRole = mockRole == null ? AppRole.USER : mockRole;
        final String resolvedChannelId = (mockChannelId == null || mockChannelId.isBlank())
            ? "todo-channel-id"
            : mockChannelId;

        return new ChzzkPrincipal(resolvedChannelId, resolvedRole);
    }
}
