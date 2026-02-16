package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.security.AppRole;
import me.cocoblue.chzzkeventtodiscord.security.ChzzkPrincipal;
import me.cocoblue.chzzkeventtodiscord.service.ChzzkAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ChzzkAuthService chzzkAuthService;

    @GetMapping("/chzzk/login")
    public ResponseEntity<LoginStartResponse> startChzzkLogin() {
        final String state = UUID.randomUUID().toString();
        final String authorizationUrl = chzzkAuthService.buildAuthorizationUrl(state);

        return ResponseEntity.ok(new LoginStartResponse(
            authorizationUrl,
            state,
            "TODO: redirect client to CHZZK authorization URL"
        ));
    }

    @GetMapping("/chzzk/callback")
    public ResponseEntity<AuthUserResponse> handleChzzkCallback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        HttpServletRequest request
    ) {
        final ChzzkPrincipal principal = chzzkAuthService.authenticateFromCallback(code, state);

        final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true)
            .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok(AuthUserResponse.from(principal));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(AuthUserResponse.from(extractPrincipal(authentication)));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.ok(new LogoutResponse("Logged out"));
    }

    @PostMapping("/chzzk/revoke")
    public ResponseEntity<LogoutResponse> revoke(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        final ChzzkPrincipal principal = extractPrincipal(authentication);
        chzzkAuthService.revokeCurrentUserTokens(principal.channelId());
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return ResponseEntity.ok(new LogoutResponse("Tokens revoked"));
    }

    private ChzzkPrincipal extractPrincipal(Authentication authentication) {
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

    private record LoginStartResponse(String authorizationUrl, String state, String message) {
    }

    private record AuthUserResponse(String channelId, AppRole role) {
        private static AuthUserResponse from(ChzzkPrincipal principal) {
            return new AuthUserResponse(principal.channelId(), principal.role());
        }
    }

    private record LogoutResponse(String message) {
    }
}
