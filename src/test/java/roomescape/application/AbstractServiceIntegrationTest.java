package roomescape.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public abstract class AbstractServiceIntegrationTest {

    @Autowired
    protected Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetAutoIncrement() {
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
    }

    @TestConfiguration
    public static class TestContextConfiguration {

        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2025-05-08T13:00:00Z"), ZoneId.of("Asia/Seoul"));
        }
    }
}
