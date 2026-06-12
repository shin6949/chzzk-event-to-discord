package me.cocoblue.chzzkeventtodiscord.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

/**
 * {@code JwtConfig}는 Spring Bean과 애플리케이션 설정을 구성합니다.
 *
 * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * 3c42b97.
 *
 * @since unreleased after Ver.0.1.4
 */
@Configuration
public class JwtConfig {
  private static final String LEGACY_DEFAULT_SECRET =
      "local-development-jwt-secret-change-me-32-bytes-minimum";
  private static final int MIN_SECRET_BYTES = 32;

  /**
   * {@code appJwtSecretKey}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Bean
  public SecretKey appJwtSecretKey(AppJwtProperties properties) {
    final byte[] secretBytes = resolveSecretBytes(properties.getSecret());
    return new SecretKeySpec(secretBytes, "HmacSHA256");
  }

  /**
   * {@code appJwtEncoder}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Bean
  public JwtEncoder appJwtEncoder(SecretKey appJwtSecretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(appJwtSecretKey));
  }

  /**
   * {@code appJwtDecoder}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Bean
  public JwtDecoder appJwtDecoder(SecretKey appJwtSecretKey, AppJwtProperties properties) {
    final NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(appJwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
    return decoder;
  }

  /**
   * {@code resolveSecretBytes}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * 3c42b97.
   *
   * @since unreleased after Ver.0.1.4
   */
  private byte[] resolveSecretBytes(String secret) {
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("app.auth.jwt.secret must not be empty");
    }
    if (LEGACY_DEFAULT_SECRET.equals(secret)) {
      throw new IllegalStateException("app.auth.jwt.secret must not use the legacy default value");
    }

    final byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException("app.auth.jwt.secret must be at least 32 bytes");
    }
    return bytes;
  }
}
