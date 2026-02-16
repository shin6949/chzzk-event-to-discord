package me.cocoblue.chzzkeventtodiscord.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.flyway.enabled=true")
class FlywayMigrationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsRunAgainstH2AndCreateExpectedSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            final String jdbcUrl = connection.getMetaData().getURL();
            assertTrue(jdbcUrl.startsWith("jdbc:h2:"),
                () -> "Expected H2 datasource in tests, but got: " + jdbcUrl);
        }

        final Integer migrationHistoryRows = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = '1' AND \"success\" = TRUE",
            Integer.class
        );
        final Integer oauthTokenTableRows = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHZZK_OAUTH_TOKEN'",
            Integer.class
        );

        assertEquals(1, migrationHistoryRows);
        assertEquals(1, oauthTokenTableRows);
    }
}
