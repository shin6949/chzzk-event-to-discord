package me.cocoblue.chzzkeventtodiscord.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code HealthcheckController}는 HTTP API 요청을 받아 서비스 계층으로 위임합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@RestController
public class HealthcheckController {
  public static final String HEALTHCHECK_PATH = "/healthz";

  /**
   * {@code healthcheck}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @GetMapping({HEALTHCHECK_PATH, HEALTHCHECK_PATH + "/"})
  public ResponseEntity<Map<String, String>> healthcheck() {
    return ResponseEntity.ok(Map.of("status", "UP"));
  }
}
