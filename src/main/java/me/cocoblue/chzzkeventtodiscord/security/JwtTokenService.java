package me.cocoblue.chzzkeventtodiscord.security;

import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AppJwtProperties properties;

    public String createAccessToken(ChzzkPrincipal principal) {
        return createToken(principal, ACCESS_TOKEN_TYPE, properties.getAccessTokenTtl());
    }

    public String createRefreshToken(ChzzkPrincipal principal) {
        return createToken(principal, REFRESH_TOKEN_TYPE, properties.getRefreshTokenTtl());
    }

    public ChzzkPrincipal parseAccessToken(String token) {
        return parseToken(token, ACCESS_TOKEN_TYPE);
    }

    public ChzzkPrincipal parseRefreshToken(String token) {
        return parseToken(token, REFRESH_TOKEN_TYPE);
    }

    private String createToken(ChzzkPrincipal principal, String tokenType, Duration ttl) {
        final Instant issuedAt = Instant.now();
        final JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(ttl))
            .subject(principal.channelId())
            .claim(ROLE_CLAIM, principal.role().name())
            .claim(TOKEN_TYPE_CLAIM, tokenType)
            .build();
        final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
            .type("JWT")
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private ChzzkPrincipal parseToken(String token, String expectedTokenType) {
        final Jwt jwt = jwtDecoder.decode(token);
        final String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        if (!expectedTokenType.equals(tokenType)) {
            throw new BadJwtException("invalid jwt token type");
        }

        final String channelId = jwt.getSubject();
        if (!StringUtils.hasText(channelId)) {
            throw new BadJwtException("jwt subject is required");
        }

        final String roleValue = jwt.getClaimAsString(ROLE_CLAIM);
        try {
            return new ChzzkPrincipal(channelId, AppRole.valueOf(roleValue));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadJwtException("invalid jwt role", e);
        }
    }
}
