package me.cocoblue.chzzkeventtodiscord.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import me.cocoblue.chzzkeventtodiscord.config.AppSecretProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * {@code SecretEncryptionService}는 도메인 비즈니스 로직을 수행합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Service
@RequiredArgsConstructor
public class SecretEncryptionService {
  private static final String PREFIX = "enc:v1:";
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final int MIN_KEY_BYTES = 32;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final AppSecretProperties properties;

  /**
   * {@code encrypt}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public String encrypt(String plaintext) {
    if (!StringUtils.hasText(plaintext)) {
      return plaintext;
    }
    if (isEncrypted(plaintext)) {
      return plaintext;
    }

    try {
      final byte[] iv = new byte[IV_BYTES];
      SECURE_RANDOM.nextBytes(iv);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, resolveKey(), new GCMParameterSpec(TAG_BITS, iv));
      final byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      final ByteBuffer encoded = ByteBuffer.allocate(iv.length + ciphertext.length);
      encoded.put(iv);
      encoded.put(ciphertext);
      return PREFIX + Base64.getEncoder().encodeToString(encoded.array());
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("failed to encrypt secret", exception);
    }
  }

  /**
   * {@code decryptIfNeeded}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public String decryptIfNeeded(String value) {
    if (!isEncrypted(value)) {
      return value;
    }

    try {
      final byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
      if (payload.length <= IV_BYTES) {
        throw new IllegalArgumentException("encrypted secret payload is too short");
      }
      final byte[] iv = Arrays.copyOfRange(payload, 0, IV_BYTES);
      final byte[] ciphertext = Arrays.copyOfRange(payload, IV_BYTES, payload.length);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, resolveKey(), new GCMParameterSpec(TAG_BITS, iv));
      return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException exception) {
      throw new IllegalStateException("failed to decrypt secret", exception);
    }
  }

  /**
   * {@code isEncrypted}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  public boolean isEncrypted(String value) {
    return value != null && value.startsWith(PREFIX);
  }

  /**
   * {@code resolveKey}은 필요한 데이터를 조회하거나 해석합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private SecretKeySpec resolveKey() throws GeneralSecurityException {
    final String configuredKey = properties.getEncryptionKey();
    if (!StringUtils.hasText(configuredKey)) {
      throw new IllegalStateException("app.secret.encryption-key must not be empty");
    }

    final byte[] keyBytes = configuredKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < MIN_KEY_BYTES) {
      throw new IllegalStateException("app.secret.encryption-key must be at least 32 bytes");
    }
    return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(keyBytes), "AES");
  }
}
