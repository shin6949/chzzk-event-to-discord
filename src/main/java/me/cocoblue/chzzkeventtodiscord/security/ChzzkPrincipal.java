package me.cocoblue.chzzkeventtodiscord.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * {@code ChzzkPrincipal}는 인증 주체 정보를 표현합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * d1f0cd9.
 *
 * @since unreleased after Ver.0.1.4
 */
public record ChzzkPrincipal(String channelId, AppRole role) implements Principal, Serializable {
  /**
   * {@code getName}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Override
  public String getName() {
    return channelId;
  }

  /**
   * {@code authorities}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  public List<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }
}
