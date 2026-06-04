package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.dto.chzzk.ChzzkChannelDto;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.AuthCookieService;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.security.JwtTokenService;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkAuthService;
import me.cocoblue.chzzkeventtodiscord.service.chzzk.ChzzkChannelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String LOGIN_SUCCESS_REDIRECT_PATH = "/subscriptions";

    private final ChzzkAuthService chzzkAuthService;
    private final ChzzkChannelService chzzkChannelService;
    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;

    @GetMapping("/chzzk/login")
    public ResponseEntity<LoginStartResponse> startChzzkLogin(HttpServletResponse response) {
        final String state = UUID.randomUUID().toString();
        final String authorizationUrl = chzzkAuthService.buildAuthorizationUrl(state);
        authCookieService.addOAuthStateCookie(response, state);

        return ResponseEntity.ok(new LoginStartResponse(
            authorizationUrl,
            state,
            "Redirect client to CHZZK authorization URL"
        ));
    }

    @GetMapping("/chzzk/callback")
    public ResponseEntity<Void> handleChzzkCallback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        validateCallbackState(request, state);
        final ChzzkPrincipal principal = chzzkAuthService.authenticateFromCallback(code, state);
        issueAuthCookies(response, principal);
        authCookieService.clearOAuthStateCookie(response);

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, LOGIN_SUCCESS_REDIRECT_PATH)
            .build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        final ChzzkChannelDto channel = chzzkChannelService.getChannelByChannelId(principal.channelId());
        return ResponseEntity.ok(AuthUserResponse.from(principal, channel));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
        HttpServletResponse response,
        Authentication authentication
    ) {
        SecurityContextHolder.clearContext();
        authCookieService.clearAuthCookies(response);

        return ResponseEntity.ok(new LogoutResponse("Logged out"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthUserResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        final String refreshToken = authCookieService.resolveRefreshToken(request)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token is required"));
        final ChzzkPrincipal principal;
        try {
            principal = jwtTokenService.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh token", e);
        }
        authCookieService.addAccessTokenCookie(response, jwtTokenService.createAccessToken(principal));

        final ChzzkChannelDto channel = chzzkChannelService.getChannelByChannelId(principal.channelId());
        return ResponseEntity.ok(AuthUserResponse.from(principal, channel));
    }

    @PostMapping("/chzzk/revoke")
    public ResponseEntity<LogoutResponse> revoke(
        HttpServletResponse response,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        chzzkAuthService.revokeCurrentUserTokens(principal.channelId());
        SecurityContextHolder.clearContext();
        authCookieService.clearAuthCookies(response);

        return ResponseEntity.ok(new LogoutResponse("Tokens revoked"));
    }

    private ChzzkPrincipal extractPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "authentication is required");
        }

        final Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof ChzzkPrincipal chzzkPrincipal) {
            return chzzkPrincipal;
        }

        final AppRole role = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
            ? AppRole.ADMIN
            : AppRole.USER;
        return new ChzzkPrincipal(authentication.getName(), role);
    }

    private void validateCallbackState(HttpServletRequest request, String state) {
        final String expectedState = authCookieService.resolveOAuthState(request).orElse(null);
        if (!StringUtils.hasText(expectedState) || !expectedState.equals(state)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid oauth state");
        }
    }

    private void issueAuthCookies(HttpServletResponse response, ChzzkPrincipal principal) {
        authCookieService.addAccessTokenCookie(response, jwtTokenService.createAccessToken(principal));
        authCookieService.addRefreshTokenCookie(response, jwtTokenService.createRefreshToken(principal));
    }

    private record LoginStartResponse(String authorizationUrl, String state, String message) {
    }

    private record AuthUserResponse(String channelId, AppRole role, String channelName, String profileUrl) {
        private static AuthUserResponse from(ChzzkPrincipal principal, ChzzkChannelDto channel) {
            final String channelName = channel != null && StringUtils.hasText(channel.getChannelName())
                ? channel.getChannelName()
                : principal.channelId();
            final String profileUrl = channel == null ? null : channel.getChannelImageUrl();
            return new AuthUserResponse(principal.channelId(), principal.role(), channelName, profileUrl);
        }
    }

    private record LogoutResponse(String message) {
    }
}
