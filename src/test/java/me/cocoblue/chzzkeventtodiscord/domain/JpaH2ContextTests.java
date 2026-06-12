package me.cocoblue.chzzkeventtodiscord.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code JpaH2ContextTests}는 관련 도메인 책임을 캡슐화합니다.
 *
 * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
 * d1f0cd9.
 *
 * @since unreleased after Ver.0.1.4
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JpaH2ContextTests {

  @Autowired private DataSource dataSource;

  /**
   * {@code jpaContextStartsWithH2Datasource}은 해당 클래스의 세부 동작을 수행합니다.
   *
   * <p>Git 이력: 생성 2026-02-16 18:32:00 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋
   * d1f0cd9.
   *
   * @since unreleased after Ver.0.1.4
   */
  @Test
  void jpaContextStartsWithH2Datasource() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      String jdbcUrl = connection.getMetaData().getURL();
      assertTrue(
          jdbcUrl.startsWith("jdbc:h2:"),
          () -> "Expected H2 datasource in tests, but got: " + jdbcUrl);
    }
  }
}
