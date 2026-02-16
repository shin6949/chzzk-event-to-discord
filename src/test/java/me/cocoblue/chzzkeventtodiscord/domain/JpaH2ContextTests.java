package me.cocoblue.chzzkeventtodiscord.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JpaH2ContextTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void jpaContextStartsWithH2Datasource() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            assertTrue(jdbcUrl.startsWith("jdbc:h2:"),
                () -> "Expected H2 datasource in tests, but got: " + jdbcUrl);
        }
    }
}
