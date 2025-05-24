package roomescape.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractServiceIntegrationTest {

    protected final Clock clock = Clock.fixed(Instant.parse("2025-05-08T13:00:00Z"), ZoneId.of("Asia/Seoul"));
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetAutoIncrement() {
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1;");
    }
}
