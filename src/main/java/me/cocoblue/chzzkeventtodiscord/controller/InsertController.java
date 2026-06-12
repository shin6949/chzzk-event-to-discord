package me.cocoblue.chzzkeventtodiscord.controller;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.chzzkeventtodiscord.config.AppLegacyFormInsertProperties;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertRequestDto;
import me.cocoblue.chzzkeventtodiscord.dto.FormInsertResponseDto;
import me.cocoblue.chzzkeventtodiscord.service.FormInsertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * {@code InsertController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
 *
 * @since Ver.0.1
 */
@Log4j2
@RestController
@RequestMapping("/form")
@RequiredArgsConstructor
public class InsertController {
  private static final String BEARER_PREFIX = "Bearer ";
  private static final int MIN_TOKEN_BYTES = 32;

  private final FormInsertService formInsertService;
  private final AppLegacyFormInsertProperties legacyFormInsertProperties;

  @Value("${app.insert-password:null}")
  private String insertPassword;

  /**
   * {@code validateLegacyTokenConfiguration}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @PostConstruct
  void validateLegacyTokenConfiguration() {
    if (!legacyFormInsertProperties.isEnabled()) {
      return;
    }
    if (!StringUtils.hasText(insertPassword)
        || "null".equals(insertPassword)
        || insertPassword.getBytes(StandardCharsets.UTF_8).length < MIN_TOKEN_BYTES) {
      throw new IllegalStateException(
          "app.insert-password must be at least 32 bytes when legacy form insert is enabled");
    }
  }

  /**
   * {@code insertForm}은 데이터를 저장하거나 갱신합니다.
   *
   * <p>Git 이력: 생성 2024-03-04 02:28:28 +0900, 작성자 shin6949, 작성 버전 Ver.0.1, 근거 커밋 db09ddc.
   *
   * @since Ver.0.1
   */
  @PostMapping(value = {"/insert", "/insert/"})
  public ResponseEntity<FormInsertResponseDto> insertForm(
      @RequestHeader(value = "Authorization", required = false) String password,
      @Valid @RequestBody FormInsertRequestDto formInsertRequestDto) {
    log.info("Form Insert Request Received.");
    if (!legacyFormInsertProperties.isEnabled()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    if (!matchesLegacyToken(password)) {
      log.warn("Invalid Authorization Token Received. Do not process more.");
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    FormInsertResponseDto formInsertResponseDTO =
        formInsertService.insertForm(formInsertRequestDto);
    return new ResponseEntity<>(formInsertResponseDTO, HttpStatus.OK);
  }

  /**
   * {@code matchesLegacyToken}은 조건 충족 여부를 판단합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  private boolean matchesLegacyToken(String authorizationHeader) {
    if (!StringUtils.hasText(authorizationHeader)
        || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      return false;
    }
    final byte[] actual =
        authorizationHeader.substring(BEARER_PREFIX.length()).getBytes(StandardCharsets.UTF_8);
    final byte[] expected = insertPassword.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(actual, expected);
  }
}
