package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("DB connection 연결 테스트")
    @Test
    void connection() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
        } catch (SQLException exception) {
            throw new RuntimeException("connection을 찾을 수 없습니다.");
        }
    }

    @DisplayName("DB 조회 테스트")
    @Test
    void databaseName() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
        } catch (SQLException exception) {
            throw new RuntimeException("database를 찾을 수 없습니다.");
        }

    }

    @DisplayName("Table 조회 테스트")
    @Test
    void tableName() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException exception) {
            throw new RuntimeException("table을 찾을 수 없습니다.");
        }

    }
}
