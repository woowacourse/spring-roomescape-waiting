package roomescape.acceptance;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.config.TestClockConfig;

@Import(TestClockConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class AcceptanceTest {

    public static final String NOW_DATE = "2026-05-02";
    public static final String FUTURE_TIME = "10:00";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        jdbcTemplate.update("INSERT INTO member (name) VALUES ('예약자')");
        jdbcTemplate.update("INSERT INTO member (name) VALUES ('예약자2')");
        jdbcTemplate.update("INSERT INTO member (name) VALUES ('예약자3')");
        jdbcTemplate.update("INSERT INTO member (name) VALUES ('예약자4')");
    }

    @AfterEach
    void afterEach() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
