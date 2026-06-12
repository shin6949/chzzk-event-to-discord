package me.cocoblue.chzzkeventtodiscord.security;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.domain.auth.RefreshTokenSessionEntity;
import me.cocoblue.chzzkeventtodiscord.domain.auth.RefreshTokenSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * {@code RefreshTokenSessionService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenSessionService {
  private static final ZoneId UTC = ZoneId.of("UTC");

  private final RefreshTokenSessionRepository refreshTokenSessionRepository;

  /**
   * {@code create}은 필요한 값을 생성합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void create(ChzzkPrincipal principal, String tokenId, Instant expiresAt) {
    refreshTokenSessionRepository.save(
        RefreshTokenSessionEntity.builder()
            .tokenId(tokenId)
            .channelId(principal.channelId())
            .role(principal.role())
            .expiresAt(ZonedDateTime.ofInstant(expiresAt, UTC))
            .build());
  }

  /**
   * {@code consume}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void consume(JwtTokenService.ParsedRefreshToken refreshToken) {
    final RefreshTokenSessionEntity session =
        refreshTokenSessionRepository
            .findByTokenIdForUpdate(refreshToken.tokenId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "refresh token session not found"));
    final ZonedDateTime now = ZonedDateTime.now(UTC);
    if (session.getRevokedAt() != null || !session.getExpiresAt().isAfter(now)) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "refresh token session is not active");
    }
    if (!Objects.equals(session.getChannelId(), refreshToken.principal().channelId())
        || session.getRole() != refreshToken.principal().role()) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "refresh token session does not match token");
    }

    session.setRevokedAt(now);
    refreshTokenSessionRepository.save(session);
  }

  /**
   * {@code revoke}은 데이터를 삭제하거나 무효화합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Transactional
  public void revoke(String tokenId) {
    if (tokenId == null) {
      return;
    }
    refreshTokenSessionRepository
        .findById(tokenId)
        .ifPresent(
            session -> {
              if (session.getRevokedAt() == null) {
                session.setRevokedAt(ZonedDateTime.now(UTC));
                refreshTokenSessionRepository.save(session);
              }
            });
  }
}
