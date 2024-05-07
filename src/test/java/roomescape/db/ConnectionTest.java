package roomescape.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DataSource dataSource;

    @Test
    @DisplayName("데이터베이스 Connection에 성공한다.")
    void isConnectionSuccess() {
        try (Connection connection = jdbcTemplate.getDataSource()
                .getConnection()
        ) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("test");
            assertThat(connection.getMetaData()
                               .getTables(null, null, "reservation", null)
                               .next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 Connection에 실패했습니다. : " + e);
        }
    }

}
