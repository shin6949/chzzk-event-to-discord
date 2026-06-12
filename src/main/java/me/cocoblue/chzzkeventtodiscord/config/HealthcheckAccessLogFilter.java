package me.cocoblue.chzzkeventtodiscord.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import me.cocoblue.chzzkeventtodiscord.controller.HealthcheckController;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code HealthcheckAccessLogFilter}는 HTTP 요청 필터링 규칙을 적용합니다.
 *
 * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
 * 없음(미추적 파일, 사용자 확인: Codex 작성).
 *
 * @since unreleased after Ver.0.1.4
 */
@Component
public class HealthcheckAccessLogFilter extends OncePerRequestFilter {
  public static final String SKIP_ACCESS_LOG_ATTRIBUTE = "skipAccessLog";
  private static final Set<String> HEALTHCHECK_PATHS =
      Set.of(HealthcheckController.HEALTHCHECK_PATH, HealthcheckController.HEALTHCHECK_PATH + "/");

  /**
   * {@code doFilterInternal}은 요청 필터 체인에서 대상 요청의 로깅/필터링을 처리합니다.
   *
   * <p>Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력
   * 없음(미추적 파일, 사용자 확인: Codex 작성).
   *
   * @since unreleased after Ver.0.1.4
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (HEALTHCHECK_PATHS.contains(request.getRequestURI())) {
      request.setAttribute(SKIP_ACCESS_LOG_ATTRIBUTE, Boolean.TRUE);
    }
    filterChain.doFilter(request, response);
  }
}
