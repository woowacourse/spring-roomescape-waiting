package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DBConnectionTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("데이터베이스 접속이 잘 되는지 확인.")
    void connectToDB() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertConnection(Connection connection) {
        assertAll(
                () -> assertThat(connection).isNotNull(),
                () -> assertThat(connection.getCatalog()).isEqualTo("TEST"),
                () -> assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue()
        );
    }
}
