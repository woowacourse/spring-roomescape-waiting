package roomescape.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.CleanUp;

@JdbcTest
@Import(CleanUp.class)
class DataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 데이터베이스_연결을_검증한다() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            boolean hasReservationTable = connection.getMetaData()
                    .getTables(null, null, "RESERVATION", null)
                    .next();
            assertThat(hasReservationTable).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}