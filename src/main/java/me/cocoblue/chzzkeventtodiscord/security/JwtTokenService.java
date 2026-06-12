package me.cocoblue.chzzkeventtodiscord.security;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppJwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@code JwtTokenService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
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

  /**
   * {@code createAccessToken}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public String createAccessToken(ChzzkPrincipal principal) {
    return createToken(principal, ACCESS_TOKEN_TYPE, properties.getAccessTokenTtl(), null)
        .tokenValue();
  }

  /**
   * {@code createRefreshToken}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public String createRefreshToken(ChzzkPrincipal principal) {
    return issueRefreshToken(principal).token();
  }

  /**
   * {@code issueRefreshToken}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public IssuedRefreshToken issueRefreshToken(ChzzkPrincipal principal) {
    final String tokenId = UUID.randomUUID().toString();
    final TokenValue tokenValue =
        createToken(principal, REFRESH_TOKEN_TYPE, properties.getRefreshTokenTtl(), tokenId);
    return new IssuedRefreshToken(tokenValue.tokenValue(), tokenId, tokenValue.expiresAt());
  }

  /**
   * {@code parseAccessToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public ChzzkPrincipal parseAccessToken(String token) {
    return principalFromJwt(parseToken(token, ACCESS_TOKEN_TYPE));
  }

  /**
   * {@code parseRefreshToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  public ChzzkPrincipal parseRefreshToken(String token) {
    return parseRefreshTokenDetails(token).principal();
  }

  /**
   * {@code parseRefreshTokenDetails}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public ParsedRefreshToken parseRefreshTokenDetails(String token) {
    final Jwt jwt = parseToken(token, REFRESH_TOKEN_TYPE);
    final String tokenId = jwt.getId();
    if (!StringUtils.hasText(tokenId)) {
      throw new BadJwtException("refresh jwt id is required");
    }
    final Instant expiresAt = jwt.getExpiresAt();
    if (expiresAt == null) {
      throw new BadJwtException("refresh jwt expiration is required");
    }
    return new ParsedRefreshToken(principalFromJwt(jwt), tokenId, expiresAt);
  }

  /**
   * {@code createToken}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private TokenValue createToken(
      ChzzkPrincipal principal, String tokenType, Duration ttl, String tokenId) {
    final Instant issuedAt = Instant.now();
    final Instant expiresAt = issuedAt.plus(ttl);
    final JwtClaimsSet.Builder claimsBuilder =
        JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(principal.channelId())
            .claim(ROLE_CLAIM, principal.role().name())
            .claim(TOKEN_TYPE_CLAIM, tokenType);
    if (StringUtils.hasText(tokenId)) {
      claimsBuilder.id(tokenId);
    }
    final JwtClaimsSet claims = claimsBuilder.build();
    final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

    return new TokenValue(
        jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), expiresAt);
  }

  /**
   * {@code parseToken}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private Jwt parseToken(String token, String expectedTokenType) {
    final Jwt jwt = jwtDecoder.decode(token);
    final String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
    if (!expectedTokenType.equals(tokenType)) {
      throw new BadJwtException("invalid jwt token type");
    }
    return jwt;
  }

  /**
   * {@code principalFromJwt}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private ChzzkPrincipal principalFromJwt(Jwt jwt) {
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

  /**
   * {@code TokenValue}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private record TokenValue(String tokenValue, Instant expiresAt) {}

  /**
   * {@code IssuedRefreshToken}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public record IssuedRefreshToken(String token, String tokenId, Instant expiresAt) {}

  /**
   * {@code ParsedRefreshToken}는 불변 데이터 전달 구조를 정의합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public record ParsedRefreshToken(ChzzkPrincipal principal, String tokenId, Instant expiresAt) {}
}
