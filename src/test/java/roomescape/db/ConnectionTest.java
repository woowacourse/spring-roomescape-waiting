package roomescape.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConnectionTest {

    public static final String TEST_DATABASE_NAME = "testtest";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("데이터베이스 Connection에 성공한다.")
    void isConnectionSuccess() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo(TEST_DATABASE_NAME);
            assertThat(connection.getMetaData().getTables(null, null, "reservation", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 Connection에 실패했습니다. : " + e);
        }
    }
}
