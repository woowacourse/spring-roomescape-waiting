package roomescape;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DataBaseConnectionTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    private void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("데이터베이스 연결")
    @Test
    void dataBaseConnection() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            Assertions.assertAll(
                    () -> assertThat(connection).isNotNull(),
                    () -> assertThat(isTableExist(connection, "RESERVATION")).isTrue(),
                    () -> assertThat(isTableExist(connection, "RESERVATION_TIME")).isTrue(),
                    () -> assertThat(isTableExist(connection, "THEME")).isTrue(),
                    () -> assertThat(isTableExist(connection, "MEMBER")).isTrue()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTableExist(Connection connection, String tableName) throws SQLException {
        return connection.getMetaData().getTables(null, null, tableName, null).next();
    }
}
