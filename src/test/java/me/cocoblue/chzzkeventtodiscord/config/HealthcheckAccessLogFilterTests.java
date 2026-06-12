package me.cocoblue.chzzkeventtodiscord.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * {@code HealthcheckAccessLogFilterTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
class HealthcheckAccessLogFilterTests {

  private final HealthcheckAccessLogFilter filter = new HealthcheckAccessLogFilter();

  /**
   * {@code marksHealthcheckRequestsToSkipTomcatAccessLog}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void marksHealthcheckRequestsToSkipTomcatAccessLog() throws ServletException, IOException {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/healthz");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(request.getAttribute(HealthcheckAccessLogFilter.SKIP_ACCESS_LOG_ATTRIBUTE))
        .isEqualTo(Boolean.TRUE);
  }

  /**
   * {@code leavesNormalRequestsAvailableForTomcatAccessLog}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void leavesNormalRequestsAvailableForTomcatAccessLog() throws ServletException, IOException {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(request.getAttribute(HealthcheckAccessLogFilter.SKIP_ACCESS_LOG_ATTRIBUTE)).isNull();
  }
}
